package org.itantra.app.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 * AI Model Setup Screen.
 *
 * Provides real-time downloading, verification, and installation of offline language models
 * directly to [ModelStore] destination paths. No simulated progress.
 */
@Composable
fun ModelSetupScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val p = palette
    val scope = rememberCoroutineScope()

    // Trigger to refresh disk state upon install/remove
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
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "AI Model Setup",
                    fontSize = Tokens.Headline,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    text = "$fullyInstalledCount of ${languages.size} Installed",
                    fontSize = Tokens.Instrument,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (fullyInstalledCount > 0) p.mint.deep else p.muted,
                    modifier = Modifier
                        .background(
                            if (fullyInstalledCount > 0) p.mint.tint else p.sunken,
                            RoundedCornerShape(Tokens.RadiusInset),
                        )
                        .border(
                            Tokens.Hairline,
                            if (fullyInstalledCount > 0) p.mint.mid else p.hairline,
                            RoundedCornerShape(Tokens.RadiusInset),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Text(
                text = "Download offline language models for on-device speech recognition and voice synthesis.",
                fontSize = Tokens.BodySmall,
                color = p.muted,
                lineHeight = Tokens.BodySmall * 1.35f,
            )
        }

        // Section: Language Packs
        SectionHeader(
            title = "AVAILABLE LANGUAGE PACKS",
            countText = "$fullyInstalledCount ready",
            family = if (fullyInstalledCount > 0) p.mint else p.sky,
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

        // Storage & Offline Summary Section
        StorageStatusCard(
            installedBytes = totalInstalledBytes,
            availableBytes = availableStorageBytes,
            installedCount = fullyInstalledCount,
            totalLanguages = languages.size,
        )

        // Continue Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Tokens.SecondaryAction)
                .background(p.ink, RoundedCornerShape(Tokens.RadiusCard))
                .clickable(onClick = onContinue)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Continue to RakshaVaani operating screen"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Forward,
                contentDescription = null,
                tint = p.onAccent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Continue to RakshaVaani",
                fontSize = Tokens.Body,
                fontWeight = FontWeight.Bold,
                color = p.onAccent,
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

/**
 * Card representing a real language pack with live download progress and installation status.
 */
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

    val borderColor = when {
        isReady -> p.mint.mid
        isDownloading -> p.sky.mid
        isFailed -> p.crimson.mid
        else -> p.hairline
    }

    val borderWidth = if (isReady || isDownloading || isFailed) Tokens.SignalBorder else Tokens.Hairline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.paper, shape)
            .border(borderWidth, borderColor, shape)
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${pack.englishName} language pack. " +
                    "STT: ${if (pack.isSttInstalled) "Installed" else "Available"}. " +
                    "TTS: ${if (!pack.hasTts) "Not Available" else if (pack.isTtsInstalled) "Installed" else "Available"}. " +
                    "Total size: ${formatBytes(pack.totalBytes)}. " +
                    "Status: ${if (isReady) "Installed and verified" else if (isDownloading) "Downloading" else "Not installed"}."
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Row: Code Badge + Language Names + Total Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isReady) p.mint.tint else p.periwinkle.tint,
                        RoundedCornerShape(Tokens.RadiusControl),
                    )
                    .border(
                        Tokens.Hairline,
                        if (isReady) p.mint.mid else p.periwinkle.mid,
                        RoundedCornerShape(Tokens.RadiusControl),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = pack.languageCode.uppercase(),
                    fontSize = Tokens.Label,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) p.mint.deep else p.periwinkle.deep,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = pack.englishName,
                        fontSize = Tokens.Subtitle,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                    )
                    if (pack.nativeName != pack.englishName) {
                        Text(
                            text = "· ${pack.nativeName}",
                            fontSize = Tokens.Status,
                            fontWeight = FontWeight.Medium,
                            lineHeight = Tokens.Status * Tokens.INDIC_LINE_HEIGHT,
                            color = p.muted,
                        )
                    }
                }
                Text(
                    text = "Script: ${pack.scriptName}",
                    fontSize = Tokens.Instrument,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = p.muted,
                )
            }

            // Total Size Badge
            Text(
                text = formatBytes(pack.totalBytes),
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.ink,
                modifier = Modifier
                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusInset))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        // Middle Row: Capabilities (STT and TTS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // STT Chip
            CapabilityStatusChip(
                title = "STT (Speech Recognition)",
                detail = "IndicConformer INT8 (${formatBytes(pack.sttBytes)})",
                isAvailable = pack.hasStt,
                isInstalled = pack.isSttInstalled,
                modifier = Modifier.weight(1f),
            )

            // TTS Chip
            CapabilityStatusChip(
                title = "TTS (Voice Synthesis)",
                detail = if (pack.hasTts) "Offline Voice (${formatBytes(pack.ttsBytes)})" else "Voice not in catalog",
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
                    .background(p.sky.tint, RoundedCornerShape(Tokens.RadiusControl))
                    .border(Tokens.Hairline, p.sky.mid, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        fontSize = Tokens.Label,
                        fontWeight = FontWeight.SemiBold,
                        color = p.sky.deep,
                    )
                    Text(
                        text = "${(progressState.totalFraction * 100).toInt()}%",
                        fontSize = Tokens.Label,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.sky.deep,
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(p.paper, RoundedCornerShape(Tokens.RadiusPill)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction.coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .background(p.sky.core, RoundedCornerShape(Tokens.RadiusPill)),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${formatBytes(progressState.totalDownloadedBytes)} of ${formatBytes(progressState.totalPackBytes)}",
                        fontSize = Tokens.Instrument,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                    if (progressState.bytesPerSecond > 0) {
                        Text(
                            text = formatSpeed(progressState.bytesPerSecond),
                            fontSize = Tokens.Instrument,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = p.sky.deep,
                        )
                    }
                }
            }
        }

        // Error message if failed
        if (isFailed && progressState.errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.crimson.tint, RoundedCornerShape(Tokens.RadiusControl))
                    .border(Tokens.Hairline, p.crimson.mid, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(8.dp).background(p.crimson.core, CircleShape))
                Text(
                    text = progressState.errorMessage ?: "Installation failed",
                    fontSize = Tokens.Label,
                    fontWeight = FontWeight.Medium,
                    color = p.crimson.deep,
                )
            }
        }

        // Confirmation dialog for Remove
        AnimatedVisibility(visible = showRemoveConfirm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Remove ${pack.englishName} language model files from device?",
                    fontSize = Tokens.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = p.ink,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .background(p.crimson.tint, RoundedCornerShape(Tokens.RadiusInset))
                            .border(Tokens.Hairline, p.crimson.mid, RoundedCornerShape(Tokens.RadiusInset))
                            .clickable {
                                showRemoveConfirm = false
                                onRemoveClick()
                            }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Confirm Remove",
                            fontSize = Tokens.Label,
                            fontWeight = FontWeight.Bold,
                            color = p.crimson.deep,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .background(p.paper, RoundedCornerShape(Tokens.RadiusInset))
                            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusInset))
                            .clickable { showRemoveConfirm = false }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = Tokens.Label,
                            fontWeight = FontWeight.Medium,
                            color = p.ink,
                        )
                    }
                }
            }
        }

        // Action Buttons
        if (!showRemoveConfirm) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    isDownloading -> {
                        // Cancel Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(p.paper, RoundedCornerShape(Tokens.RadiusControl))
                                .border(Tokens.Hairline, p.crimson.mid, RoundedCornerShape(Tokens.RadiusControl))
                                .clickable(onClick = onCancelClick)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Cancel Download",
                                fontSize = Tokens.BodySmall,
                                fontWeight = FontWeight.Bold,
                                color = p.crimson.deep,
                            )
                        }
                    }

                    isReady -> {
                        // Installed Status Badge + Remove Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .background(p.mint.tint, RoundedCornerShape(Tokens.RadiusControl))
                                .border(Tokens.Hairline, p.mint.mid, RoundedCornerShape(Tokens.RadiusControl))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Tick,
                                contentDescription = null,
                                tint = p.mint.core,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Installed & Ready",
                                fontSize = Tokens.BodySmall,
                                fontWeight = FontWeight.Bold,
                                color = p.mint.deep,
                            )
                        }

                        // Remove Button
                        Row(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .background(p.paper, RoundedCornerShape(Tokens.RadiusControl))
                                .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                                .clickable { showRemoveConfirm = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Remove",
                                fontSize = Tokens.BodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = p.crimson.deep,
                            )
                        }
                    }

                    isFailed -> {
                        // Retry Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusControl))
                                .clickable(onClick = onInstallClick)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Replay,
                                contentDescription = null,
                                tint = p.onAccent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Retry Download",
                                fontSize = Tokens.BodySmall,
                                fontWeight = FontWeight.Bold,
                                color = p.onAccent,
                            )
                        }
                    }

                    else -> {
                        // Install Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusControl))
                                .clickable(onClick = onInstallClick)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Download,
                                contentDescription = null,
                                tint = p.onAccent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Install ${pack.englishName} (${formatBytes(pack.totalBytes)})",
                                fontSize = Tokens.BodySmall,
                                fontWeight = FontWeight.Bold,
                                color = p.onAccent,
                            )
                        }
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
    val bg = when {
        isInstalled -> p.mint.tint
        isAvailable -> p.sunken
        else -> p.sunken
    }
    val border = when {
        isInstalled -> p.mint.mid
        isAvailable -> p.hairline
        else -> p.hairline
    }
    val textInk = when {
        isInstalled -> p.mint.deep
        isAvailable -> p.ink
        else -> p.muted
    }

    Column(
        modifier = modifier
            .background(bg, RoundedCornerShape(Tokens.RadiusControl))
            .border(Tokens.Hairline, border, RoundedCornerShape(Tokens.RadiusControl))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isInstalled) p.mint.core else if (isAvailable) p.sky.core else p.muted, CircleShape),
            )
            Text(
                text = title,
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = textInk,
            )
        }
        Text(
            text = if (isInstalled) "✓ Installed" else detail,
            fontSize = Tokens.Instrument,
            color = if (isInstalled) p.mint.deep else p.muted,
        )
    }
}

@Composable
private fun StorageStatusCard(
    installedBytes: Long,
    availableBytes: Long,
    installedCount: Int,
    totalLanguages: Int,
) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusTile)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.paper, shape)
            .border(Tokens.Hairline, p.hairline, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Storage & Offline Operation",
            fontSize = Tokens.Subtitle,
            fontWeight = FontWeight.Bold,
            color = p.ink,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatBytes(installedBytes),
                    fontSize = Tokens.Callout,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    text = "Installed ($installedCount of $totalLanguages packs)",
                    fontSize = Tokens.Instrument,
                    color = p.muted,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatBytes(availableBytes),
                    fontSize = Tokens.Callout,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.mint.deep,
                )
                Text(
                    text = "Available Storage",
                    fontSize = Tokens.Instrument,
                    color = p.muted,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(p.sunken))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(p.butter.core, CircleShape),
            )
            Text(
                text = "Once downloaded, all models run completely offline on-device.",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = p.muted,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    countText: String,
    family: ItantraPalette.Family,
) {
    val p = palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = Tokens.Instrument,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.muted,
        )
        Text(
            text = countText,
            fontSize = Tokens.Instrument,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = family.deep,
            modifier = Modifier
                .background(family.tint, RoundedCornerShape(Tokens.RadiusInset))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/**
 * Derives available language pack metadata from the project's real [InstallIndex] and [ModelStore].
 */
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
        bytes >= 1024L * 1024L * 1024L -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> "%.0f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1024L * 1024L -> "%.1f MB/s".format(bytesPerSecond / (1024.0 * 1024.0))
        bytesPerSecond >= 1024L -> "%.0f KB/s".format(bytesPerSecond / 1024.0)
        else -> "$bytesPerSecond B/s"
    }
}
