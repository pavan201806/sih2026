package org.itantra.app.platform

import android.content.Context
import android.os.StatFs
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext

/**
 * Downloads and installs real model files directly to the device's [ModelStore] locations.
 *
 * Implements streaming HTTPS downloads directly to disk, live byte-accurate progress,
 * storage space verification, SHA-256 checksum verification, Piper voice metadata stamping,
 * atomic file staging (.download -> final), cancellation, and removal.
 */
object ModelDownloader {
    private const val TAG = "ModelDownloader"
    private const val BUFFER_SIZE = 64 * 1024
    private const val MIN_STORAGE_MARGIN_BYTES = 25L * 1024 * 1024 // 25 MB safety margin

    /**
     * Live download progress information for a language pack.
     */
    data class Progress(
        val languageCode: String,
        val currentFileName: String,
        val currentFileKind: String, // "recogniser" or "voice"
        val fileIndex: Int,
        val totalFiles: Int,
        val downloadedBytesForFile: Long,
        val totalBytesForFile: Long,
        val totalDownloadedBytes: Long,
        val totalPackBytes: Long,
        val bytesPerSecond: Long,
        val fileFraction: Float,
        val totalFraction: Float,
        val stage: Stage,
        val errorMessage: String? = null,
    )

    enum class Stage {
        IDLE,
        CHECKING_STORAGE,
        CONNECTING,
        DOWNLOADING,
        VERIFYING_CHECKSUM,
        PREPARING_METADATA,
        COMPLETED,
        FAILED,
        CANCELLED,
    }

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val progressFlows = ConcurrentHashMap<String, MutableStateFlow<Progress>>()

    /**
     * Get or create a StateFlow for a language's download progress.
     */
    fun getProgressFlow(languageCode: String): StateFlow<Progress> {
        return progressFlows.getOrPut(languageCode) {
            MutableStateFlow(createIdleProgress(languageCode))
        }.asStateFlow()
    }

    private fun updateProgress(progress: Progress) {
        val flow = progressFlows.getOrPut(progress.languageCode) {
            MutableStateFlow(progress)
        }
        flow.value = progress
    }

    private fun createIdleProgress(languageCode: String): Progress {
        return Progress(
            languageCode = languageCode,
            currentFileName = "",
            currentFileKind = "",
            fileIndex = 0,
            totalFiles = 0,
            downloadedBytesForFile = 0L,
            totalBytesForFile = 0L,
            totalDownloadedBytes = 0L,
            totalPackBytes = 0L,
            bytesPerSecond = 0L,
            fileFraction = 0f,
            totalFraction = 0f,
            stage = Stage.IDLE,
        )
    }

    /**
     * Returns the available free bytes on the device storage where models are kept.
     */
    fun getAvailableStorageBytes(context: Context): Long {
        val root = context.getExternalFilesDir(null) ?: context.filesDir
        return runCatching {
            val stat = StatFs(root.path)
            stat.availableBlocksLong * stat.blockSizeLong
        }.getOrDefault(0L)
    }

    /**
     * Check if a specific language is currently downloading.
     */
    fun isDownloading(languageCode: String): Boolean {
        return activeJobs[languageCode]?.isActive == true
    }

    /**
     * Cancel an ongoing download for a language.
     */
    fun cancelDownload(languageCode: String) {
        activeJobs[languageCode]?.cancel()
        activeJobs.remove(languageCode)
        updateProgress(
            createIdleProgress(languageCode).copy(
                stage = Stage.CANCELLED,
                errorMessage = "Download cancelled by user",
            )
        )
    }

    /**
     * Downloads and installs all missing components (ASR and TTS) for the given language.
     *
     * @param context Application context
     * @param languageCode ISO code (e.g. "en", "hi", "te")
     * @return true if installation succeeded and passed full validation
     */
    suspend fun installLanguage(
        context: Context,
        languageCode: String,
        onProgressUpdate: ((Progress) -> Unit)? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        val index = InstallIndex(context)
        val store = ModelStore(context)
        val modelsRoot = File(context.getExternalFilesDir(null), "models")

        // 1. Ensure bundled small artefacts (tokens.txt, config.json) and espeak-ng-data are expanded first
        SmallArtefacts(context).ensure()
        EspeakData(context).ensure()

        val allItems = index.downloadableFor(languageCode)
        if (allItems.isEmpty()) {
            val errorProgress = createIdleProgress(languageCode).copy(
                stage = Stage.FAILED,
                errorMessage = "No downloadable model configuration found for $languageCode",
            )
            updateProgress(errorProgress)
            onProgressUpdate?.invoke(errorProgress)
            return@withContext false
        }

        // Determine which files actually need to be downloaded
        val itemsToDownload = allItems.filter { item ->
            val targetFile = File(modelsRoot, item.install)
            val isInstalled = when (item.kind) {
                "recogniser" -> store.hasPack(languageCode) && targetFile.isFile && targetFile.length() >= item.bytes
                "voice" -> store.hasVoice(languageCode) && targetFile.isFile && targetFile.length() >= item.bytes
                else -> targetFile.isFile && targetFile.length() >= item.bytes
            }
            !isInstalled
        }

        if (itemsToDownload.isEmpty()) {
            // Already fully installed and valid
            val completedProgress = createIdleProgress(languageCode).copy(
                stage = Stage.COMPLETED,
                totalFraction = 1.0f,
                fileFraction = 1.0f,
            )
            updateProgress(completedProgress)
            onProgressUpdate?.invoke(completedProgress)
            return@withContext true
        }

        // 2. Verify sufficient storage space
        val totalRequiredBytes = itemsToDownload.sumOf { it.bytes }
        val availableBytes = getAvailableStorageBytes(context)

        updateProgress(
            createIdleProgress(languageCode).copy(
                stage = Stage.CHECKING_STORAGE,
                totalPackBytes = totalRequiredBytes,
            )
        )

        if (availableBytes < totalRequiredBytes + MIN_STORAGE_MARGIN_BYTES) {
            val requiredMb = totalRequiredBytes / (1024 * 1024)
            val availableMb = availableBytes / (1024 * 1024)
            val errorProgress = createIdleProgress(languageCode).copy(
                stage = Stage.FAILED,
                errorMessage = "Insufficient storage space. Required: ${requiredMb} MB, Available: ${availableMb} MB",
            )
            updateProgress(errorProgress)
            onProgressUpdate?.invoke(errorProgress)
            return@withContext false
        }

        // 3. Register current coroutine job for cancellation support
        val currentJob = coroutineContext[Job]
        if (currentJob != null) {
            activeJobs[languageCode] = currentJob
        }

        var cumulativeDownloadedBytes = 0L

        try {
            for ((itemIndex, item) in itemsToDownload.withIndex()) {
                coroutineContext.ensureActive()

                val targetFile = File(modelsRoot, item.install)
                val targetDir = targetFile.parentFile ?: modelsRoot
                targetDir.mkdirs()

                val tempFile = File(targetDir, targetFile.name + ".download")
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                val itemFileName = targetFile.name

                var progress = Progress(
                    languageCode = languageCode,
                    currentFileName = itemFileName,
                    currentFileKind = item.kind,
                    fileIndex = itemIndex + 1,
                    totalFiles = itemsToDownload.size,
                    downloadedBytesForFile = 0L,
                    totalBytesForFile = item.bytes,
                    totalDownloadedBytes = cumulativeDownloadedBytes,
                    totalPackBytes = totalRequiredBytes,
                    bytesPerSecond = 0L,
                    fileFraction = 0f,
                    totalFraction = (cumulativeDownloadedBytes.toFloat() / totalRequiredBytes.toFloat()).coerceIn(0f, 1f),
                    stage = Stage.CONNECTING,
                )
                updateProgress(progress)
                onProgressUpdate?.invoke(progress)

                // 4. Download file stream directly to tempFile
                val digest = MessageDigest.getInstance("SHA-256")
                var downloadedForThisFile = 0L
                var lastSpeedCalcTime = System.currentTimeMillis()
                var bytesSinceLastSpeedCalc = 0L
                var currentBps = 0L

                val (downloadStream, _) = openDownloadStreamWithRedirects(item.url)
                downloadStream.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        progress = progress.copy(stage = Stage.DOWNLOADING)
                        updateProgress(progress)
                        onProgressUpdate?.invoke(progress)

                        while (true) {
                            coroutineContext.ensureActive()
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) break

                            outputStream.write(buffer, 0, bytesRead)
                            digest.update(buffer, 0, bytesRead)
                            downloadedForThisFile += bytesRead
                            bytesSinceLastSpeedCalc += bytesRead

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastSpeedCalcTime
                            if (elapsed >= 300) {
                                currentBps = (bytesSinceLastSpeedCalc * 1000L) / elapsed
                                lastSpeedCalcTime = now
                                bytesSinceLastSpeedCalc = 0L

                                val overallDownloaded = cumulativeDownloadedBytes + downloadedForThisFile
                                val fileFraction = (downloadedForThisFile.toFloat() / item.bytes.toFloat()).coerceIn(0f, 1f)
                                val totalFraction = (overallDownloaded.toFloat() / totalRequiredBytes.toFloat()).coerceIn(0f, 1f)

                                progress = progress.copy(
                                    downloadedBytesForFile = downloadedForThisFile,
                                    totalDownloadedBytes = overallDownloaded,
                                    bytesPerSecond = currentBps,
                                    fileFraction = fileFraction,
                                    totalFraction = totalFraction,
                                    stage = Stage.DOWNLOADING,
                                )
                                updateProgress(progress)
                                onProgressUpdate?.invoke(progress)
                            }
                        }
                    }
                }

                // 5. Verify File Integrity (Size and SHA-256)
                progress = progress.copy(stage = Stage.VERIFYING_CHECKSUM, bytesPerSecond = 0L)
                updateProgress(progress)
                onProgressUpdate?.invoke(progress)

                if (tempFile.length() != item.bytes) {
                    tempFile.delete()
                    throw IOException(
                        "File size verification failed for ${item.install}. Expected ${item.bytes} bytes, downloaded ${tempFile.length()} bytes."
                    )
                }

                val actualHash = digest.digest().joinToString("") { "%02x".format(it) }.lowercase()
                val expectedHash = item.sha256.lowercase()
                if (actualHash != expectedHash) {
                    tempFile.delete()
                    throw IOException(
                        "Checksum verification failed for ${item.install}. Checksum mismatch."
                    )
                }

                // 6. Voice metadata preparation if applicable
                if (item.kind == "voice") {
                    progress = progress.copy(stage = Stage.PREPARING_METADATA)
                    updateProgress(progress)
                    onProgressUpdate?.invoke(progress)

                    // Stamp Piper voice with sample_rate metadata required by sherpa-onnx
                    PiperVoiceMetadata.ensure(tempFile)

                    // If config.json is present, ensure tokens.txt is generated
                    val configJson = File(targetDir, "config.json")
                    if (configJson.isFile) {
                        writeVoiceTokensFromConfig(configJson)
                    }
                }

                // 7. Atomic installation move
                Files.move(
                    tempFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )

                cumulativeDownloadedBytes += item.bytes
            }

            // 8. Final overall validation
            val hasValidAsr = store.hasPack(languageCode)
            val hasVoiceAvailableInRepo = allItems.any { it.kind == "voice" }
            val hasValidTts = if (hasVoiceAvailableInRepo) store.hasVoice(languageCode) else true

            if (hasValidAsr && hasValidTts) {
                val completedProgress = Progress(
                    languageCode = languageCode,
                    currentFileName = "",
                    currentFileKind = "",
                    fileIndex = itemsToDownload.size,
                    totalFiles = itemsToDownload.size,
                    downloadedBytesForFile = totalRequiredBytes,
                    totalBytesForFile = totalRequiredBytes,
                    totalDownloadedBytes = totalRequiredBytes,
                    totalPackBytes = totalRequiredBytes,
                    bytesPerSecond = 0L,
                    fileFraction = 1.0f,
                    totalFraction = 1.0f,
                    stage = Stage.COMPLETED,
                )
                updateProgress(completedProgress)
                onProgressUpdate?.invoke(completedProgress)
                activeJobs.remove(languageCode)
                return@withContext true
            } else {
                val msg = when {
                    !hasValidAsr -> "ASR model validation failed after install"
                    !hasValidTts -> "TTS voice validation failed after install"
                    else -> "Model validation failed"
                }
                val failedProgress = createIdleProgress(languageCode).copy(
                    stage = Stage.FAILED,
                    errorMessage = msg,
                )
                updateProgress(failedProgress)
                onProgressUpdate?.invoke(failedProgress)
                activeJobs.remove(languageCode)
                return@withContext false
            }

        } catch (e: CancellationException) {
            Log.i(TAG, "Download cancelled for $languageCode")
            cleanupTempFiles(modelsRoot, itemsToDownload)
            val cancelledProgress = createIdleProgress(languageCode).copy(
                stage = Stage.CANCELLED,
                errorMessage = "Download cancelled",
            )
            updateProgress(cancelledProgress)
            onProgressUpdate?.invoke(cancelledProgress)
            activeJobs.remove(languageCode)
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for $languageCode", e)
            cleanupTempFiles(modelsRoot, itemsToDownload)
            val failedProgress = createIdleProgress(languageCode).copy(
                stage = Stage.FAILED,
                errorMessage = e.message ?: "Download connection failed",
            )
            updateProgress(failedProgress)
            onProgressUpdate?.invoke(failedProgress)
            activeJobs.remove(languageCode)
            return@withContext false
        }
    }

    /**
     * Safely deletes all temporary .download files for given items.
     */
    private fun cleanupTempFiles(modelsRoot: File, items: List<InstallIndex.Item>) {
        items.forEach { item ->
            val target = File(modelsRoot, item.install)
            val temp = File(target.parentFile, target.name + ".download")
            if (temp.exists()) {
                runCatching { temp.delete() }
            }
        }
    }

    /**
     * Removes an installed language pack from disk.
     */
    fun removeLanguage(context: Context, languageCode: String): Boolean {
        val store = ModelStore(context)
        val deletedAsr = store.delete(languageCode, "recogniser")
        val deletedTts = store.delete(languageCode, "voice")

        val idleProgress = createIdleProgress(languageCode)
        updateProgress(idleProgress)

        return deletedAsr || deletedTts
    }

    /**
     * Follows HTTP/HTTPS redirects up to maxRedirects to open an input stream.
     */
    private fun openDownloadStreamWithRedirects(
        urlString: String,
        maxRedirects: Int = 5,
    ): Pair<InputStream, Long> {
        var currentUrl = urlString
        for (redirect in 0 until maxRedirects) {
            val url = URL(currentUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 60_000
                setRequestProperty("User-Agent", "RakshaVaani/1.0 (Android; Offline-AI)")
                setRequestProperty("Accept-Encoding", "identity")
                instanceFollowRedirects = true
            }

            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                    ?: throw IOException("HTTP $code redirect with no Location header")
                connection.disconnect()
                currentUrl = location
                continue
            }

            if (code !in 200..299) {
                connection.disconnect()
                throw IOException("HTTP error $code ${connection.responseMessage} from $currentUrl")
            }

            val length = connection.contentLengthLong
            return connection.inputStream to length
        }
        throw IOException("Too many HTTP redirects ($maxRedirects) for $urlString")
    }

    /**
     * Generates voice tokens from Piper config.json.
     */
    private fun writeVoiceTokensFromConfig(config: File) {
        runCatching {
            val map = JSONObject(config.readText()).getJSONObject("phoneme_id_map")
            val out = StringBuilder()
            for (phoneme in map.keys()) {
                val ids = map.getJSONArray(phoneme)
                if (ids.length() != 1) return@runCatching
                out.append(phoneme).append(' ').append(ids.getInt(0)).append('\n')
            }
            File(config.parentFile, "tokens.txt").writeText(out.toString())
        }
    }
}
