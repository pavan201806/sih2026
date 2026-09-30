package org.itantra.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.itantra.app.platform.InstallIndex
import org.itantra.app.platform.ModelDownloader
import org.itantra.app.platform.ModelStore

/**
 * Metadata and current installation status for a language pack.
 */
data class LanguagePackInfo(
    val languageCode: String,
    val englishName: String,
    val nativeName: String,
    val scriptName: String,
    val hasStt: Boolean,
    val hasTts: Boolean,
    val sttBytes: Long,
    val ttsBytes: Long,
    val isSttInstalled: Boolean,
    val isTtsInstalled: Boolean,
) {
    val totalBytes: Long get() = sttBytes + ttsBytes
    val isFullyInstalled: Boolean get() = isSttInstalled && (!hasTts || isTtsInstalled)
}

/**
 * ModelSetupScreen restyled with Stitch 05_offline_ai_models design:
 * Sourced directly from ui-reference/05_offline_ai_models with tactical storage gauge,
 * zero-radius card geometry, and 100% real ModelDownloader/ModelStore execution.
 */
@Composable
fun ModelSetupScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val p = palette
    val scope = rememberCoroutineScope()

    var diskRevision by remember { mutableIntStateOf(0) }

    val languages = remember(diskRevision) {
        getAvailableLanguagePacks(context)
    }

    val availableStorageBytes = remember(diskRevision) {
        ModelDownloader.getAvailableStorageBytes(context)
    }

    val totalInstalledBytes = remember(diskRevision) {
        val store = ModelStore(context)
        store.installedPacks().sumOf { it.bytes }
    }

    val fullyInstalledCount = languages.count { it.isFullyInstalled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.ground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // TOP HUD HEADER & TELEMETRY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(12.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(p.periwinkle.core))
                        Text(
                            text = "HUD DIRECTORY // SYS_MODELS_05",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                            letterSpacing = 1.sp,
                        )
                    }
                    Text(
                        text = "$fullyInstalledCount OF ${languages.size} READY",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.mint.core,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "OFFLINE AI MODELS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = p.ink,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "ON-DEVICE SPEECH ASR & TTS // AIR-GAPPED RUNTIME",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
            }
        }

        // STORAGE TELEMETRY & NAND ALLOCATION GAUGE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(12.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "STORAGE TELEMETRY & NAND ALLOCATION",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                    )
                    Text(
                        text = "AVAIL: ${formatBytes(availableStorageBytes)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )
                }
                Spacer(Modifier.height(8.dp))

                val totalCapacity = (totalInstalledBytes + availableStorageBytes).coerceAtLeast(1L)
                val usedRatio = (totalInstalledBytes.toFloat() / totalCapacity.toFloat()).coerceIn(0.02f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(p.sunken)
                        .border(Tokens.Hairline, p.hairline),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(usedRatio)
                            .height(8.dp)
                            .background(p.periwinkle.core),
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "ALLOCATED: ${formatBytes(totalInstalledBytes)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                    Text(
                        text = "CORE INFERENCE RUNTIME: ON-DEVICE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.mint.core,
                    )
                }
            }
        }

        // LANGUAGE PACKS SECTION HEADER
        Text(
            text = "SYNCHRONIZED VOICE CODECS & LEXICONS",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.muted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 2.dp),
        )

        languages.forEach { pack ->
            RealLanguageModelCard(
                pack = pack,
                onInstallClick = {
                    scope.launch {
                        val success = ModelDownloader.installLanguage(context, pack.languageCode)
                        if (success) {
                            diskRevision++
                        }
                    }
                },
                onCancelClick = {
                    ModelDownloader.cancelDownload(pack.languageCode)
                },
                onRemoveClick = {
                    ModelDownloader.removeLanguage(context, pack.languageCode)
                    diskRevision++
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // CONTINUE BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(p.periwinkle.core)
                .border(2.dp, p.periwinkle.mid, shape = Tokens.ZeroShape)
                .clickable { onContinue() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CONTINUE TO RAKSHAVAANI ›",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = p.onAccent,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun RealLanguageModelCard(
    pack: LanguagePackInfo,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusTile)
    val progressState by ModelDownloader.getProgressFlow(pack.languageCode).collectAsState()

    var showRemoveConfirm by remember { mutableStateOf(false) }

    val isDownloading = progressState.stage == ModelDownloader.Stage.DOWNLOADING ||
        progressState.stage == ModelDownloader.Stage.CONNECTING ||
        progressState.stage == ModelDownloader.Stage.CHECKING_STORAGE ||
        progressState.stage == ModelDownloader.Stage.VERIFYING_CHECKSUM ||
        progressState.stage == ModelDownloader.Stage.PREPARING_METADATA

    val isReady = pack.isFullyInstalled && !isDownloading
    val isFailed = progressState.stage == ModelDownloader.Stage.FAILED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, if (isReady) p.periwinkle.core else p.hairline, shape)
            .padding(12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${pack.englishName} language pack. " +
                    "STT: ${if (pack.isSttInstalled) "Installed" else "Available"}. " +
                    "TTS: ${if (!pack.hasTts) "Not Available" else if (pack.isTtsInstalled) "Installed" else "Available"}. " +
                    "Total size: ${formatBytes(pack.totalBytes)}. " +
                    "Status: ${if (isReady) "Installed and verified" else if (isDownloading) "Downloading" else "Not installed"}."
            },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row: Code Badge + Language Names + Total Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(if (isReady) p.mint.tint else p.surfaceContainerLow)
                        .border(Tokens.Hairline, if (isReady) p.mint.mid else p.hairline),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = pack.languageCode.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isReady) p.mint.deep else p.ink,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = pack.englishName.uppercase(),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.ink,
                        )
                        if (pack.nativeName != pack.englishName) {
                            Text(
                                text = "· ${pack.nativeName}",
                                fontSize = 12.sp,
                                color = p.muted,
                            )
                        }
                    }
                    Text(
                        text = "Script: ${pack.scriptName} // Footprint: ${formatBytes(pack.totalBytes)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                }

                if (isReady) {
                    Box(
                        modifier = Modifier
                            .background(p.mint.tint)
                            .border(Tokens.Hairline, p.mint.mid)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "READY",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.mint.deep,
                        )
                    }
                }
            }

            // Middle Row: Capabilities (STT and TTS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CapabilityStatusChip(
                    title = "STT RECOGNITION",
                    detail = if (pack.hasStt) "IndicConformer INT8 (${formatBytes(pack.sttBytes)})" else "Not in catalog",
                    isAvailable = pack.hasStt,
                    isInstalled = pack.isSttInstalled,
                    modifier = Modifier.weight(1f),
                )

                CapabilityStatusChip(
                    title = "TTS SYNTHESIS",
                    detail = if (pack.hasTts) "Offline VITS (${formatBytes(pack.ttsBytes)})" else "Not in catalog",
                    isAvailable = pack.hasTts,
                    isInstalled = pack.isTtsInstalled,
                    modifier = Modifier.weight(1f),
                )
            }

            // Live Download Progress Section
            if (isDownloading) {
                val animatedFraction by animateFloatAsState(
                    targetValue = progressState.totalFraction,
                    label = "real_download_progress",
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(p.sky.tint)
                        .border(Tokens.Hairline, p.sky.mid)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = when (progressState.stage) {
                                ModelDownloader.Stage.CHECKING_STORAGE -> "Verifying storage space..."
                                ModelDownloader.Stage.CONNECTING -> "Connecting to server..."
                                ModelDownloader.Stage.DOWNLOADING -> "Downloading ${progressState.currentFileName} (${progressState.fileIndex}/${progressState.totalFiles})"
                                ModelDownloader.Stage.VERIFYING_CHECKSUM -> "Verifying SHA-256 checksum..."
                                ModelDownloader.Stage.PREPARING_METADATA -> "Preparing voice metadata..."
                                else -> "Installing..."
                            },
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.sky.deep,
                        )
                        Text(
                            text = "${(animatedFraction * 100).toInt()}%",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.sky.deep,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(p.surfaceContainerLowest)
                            .border(Tokens.Hairline, p.hairline),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedFraction.coerceIn(0.01f, 1f))
                                .height(6.dp)
                                .background(p.periwinkle.core),
                        )
                    }

                    Text(
                        text = "${formatBytes(progressState.totalDownloadedBytes)} / ${formatBytes(progressState.totalPackBytes)}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                }
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isDownloading) {
                    Box(
                        modifier = Modifier
                            .background(p.blush.tint)
                            .border(Tokens.Hairline, p.blush.core)
                            .clickable(onClick = onCancelClick)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "CANCEL DOWNLOAD",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.blush.deep,
                        )
                    }
                } else if (isReady) {
                    if (showRemoveConfirm) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(p.sunken)
                                    .border(Tokens.Hairline, p.hairline)
                                    .clickable { showRemoveConfirm = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text("CANCEL", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.ink)
                            }
                            Box(
                                modifier = Modifier
                                    .background(p.blush.core)
                                    .clickable {
                                        showRemoveConfirm = false
                                        onRemoveClick()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text("CONFIRM DELETE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(p.surfaceContainerLowest)
                                .border(Tokens.Hairline, p.hairline)
                                .clickable { showRemoveConfirm = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text("REMOVE FROM DISK", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(p.periwinkle.core)
                            .border(Tokens.Hairline, p.periwinkle.mid)
                            .clickable(onClick = onInstallClick)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "DOWNLOAD & INSTALL (${formatBytes(pack.totalBytes)})",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.onAccent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityStatusChip(
    title: String,
    detail: String,
    isAvailable: Boolean,
    isInstalled: Boolean,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Box(
        modifier = modifier
            .background(p.surfaceContainerLow)
            .border(Tokens.Hairline, p.hairline)
            .padding(6.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isInstalled) p.mint.core else if (isAvailable) p.apricot.core else p.muted, CircleShape),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = detail,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
                maxLines = 1,
            )
        }
    }
}

private fun getAvailableLanguagePacks(context: android.content.Context): List<LanguagePackInfo> {
    val store = ModelStore(context)
    val index = InstallIndex(context)

    val definitions = listOf(
        Triple("en", "English" to "English", "Latin"),
        Triple("hi", "Hindi" to "हिन्दी", "Devanagari"),
        Triple("te", "Telugu" to "తెలుగు", "Telugu"),
        Triple("bn", "Bengali" to "বাংলা", "Bengali"),
        Triple("mr", "Marathi" to "मराठी", "Devanagari"),
        Triple("ta", "Tamil" to "தமிழ்", "Tamil"),
        Triple("gu", "Gujarati" to "ગુજરાતી", "Gujarati"),
        Triple("kn", "Kannada" to "ಕನ್ನಡ", "Kannada"),
        Triple("ml", "Malayalam" to "മലയാളം", "Malayalam"),
        Triple("or", "Odia" to "ଓଡ଼ିଆ", "Odia"),
    )

    return definitions.map { (code, names, script) ->
        val items = index.downloadableFor(code)
        val asrItem = items.firstOrNull { it.kind == "recogniser" }
        val ttsItem = items.firstOrNull { it.kind == "voice" }

        val hasStt = asrItem != null
        val hasTts = ttsItem != null

        val sttBytes = asrItem?.bytes ?: 0L
        val ttsBytes = ttsItem?.bytes ?: 0L

        val isSttInstalled = store.hasPack(code)
        val isTtsInstalled = if (hasTts) store.hasVoice(code) else false

        LanguagePackInfo(
            languageCode = code,
            englishName = names.first,
            nativeName = names.second,
            scriptName = script,
            hasStt = hasStt,
            hasTts = hasTts,
            sttBytes = sttBytes,
            ttsBytes = ttsBytes,
            isSttInstalled = isSttInstalled,
            isTtsInstalled = isTtsInstalled,
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L * 1024L -> "%.1f GB".format(java.util.Locale.ROOT, bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> "%.0f MB".format(java.util.Locale.ROOT, bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> "%.0f KB".format(java.util.Locale.ROOT, bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1024L * 1024L -> "%.1f MB/s".format(java.util.Locale.ROOT, bytesPerSecond / (1024.0 * 1024.0))
        bytesPerSecond >= 1024L -> "%.0f KB/s".format(java.util.Locale.ROOT, bytesPerSecond / 1024.0)
        else -> "$bytesPerSecond B/s"
    }
}
