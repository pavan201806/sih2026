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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * State for a single simulated model download card.
 */
enum class ModelDownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
}

data class DemoModelItem(
    val id: String,
    val languageName: String,
    val languageCode: String,
    val nativeScript: String,
    val category: String,
    val modelName: String,
    val totalSizeMb: Int,
)

/**
 * In-memory singleton state holder for the demo download simulation across screen navigations.
 */
object DemoModelSetupState {
    val items = listOf(
        // Speech Recognition (STT)
        DemoModelItem(
            id = "stt_en",
            languageName = "English",
            languageCode = "EN",
            nativeScript = "English",
            category = "Speech Recognition",
            modelName = "IndicConformer INT8",
            totalSizeMb = 189,
        ),
        DemoModelItem(
            id = "stt_hi",
            languageName = "Hindi",
            languageCode = "HI",
            nativeScript = "हिन्दी",
            category = "Speech Recognition",
            modelName = "IndicConformer INT8",
            totalSizeMb = 189,
        ),
        DemoModelItem(
            id = "stt_te",
            languageName = "Telugu",
            languageCode = "TE",
            nativeScript = "తెలుగు",
            category = "Speech Recognition",
            modelName = "IndicConformer INT8",
            totalSizeMb = 189,
        ),
        // Voice Synthesis (TTS)
        DemoModelItem(
            id = "tts_en",
            languageName = "English",
            languageCode = "EN",
            nativeScript = "English",
            category = "Voice Synthesis",
            modelName = "Offline TTS Voice",
            totalSizeMb = 63,
        ),
        DemoModelItem(
            id = "tts_hi",
            languageName = "Hindi",
            languageCode = "HI",
            nativeScript = "हिन्दी",
            category = "Voice Synthesis",
            modelName = "Offline TTS Voice",
            totalSizeMb = 63,
        ),
        DemoModelItem(
            id = "tts_te",
            languageName = "Telugu",
            languageCode = "TE",
            nativeScript = "తెలుగు",
            category = "Voice Synthesis",
            modelName = "Offline TTS Voice",
            totalSizeMb = 63,
        ),
    )

    val statuses = mutableStateMapOf<String, ModelDownloadStatus>().apply {
        items.forEach { put(it.id, ModelDownloadStatus.NOT_DOWNLOADED) }
    }

    val progresses = mutableStateMapOf<String, Float>().apply {
        items.forEach { put(it.id, 0f) }
    }

    val downloadedMbs = mutableStateMapOf<String, Int>().apply {
        items.forEach { put(it.id, 0) }
    }
}

/**
 * AI Model Setup screen for interactive hackathon demonstration.
 * Simulates offline AI model downloading and readiness for STT and TTS across Indian languages.
 */
@Composable
fun ModelSetupScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    val scope = rememberCoroutineScope()

    val sttItems = DemoModelSetupState.items.filter { it.category == "Speech Recognition" }
    val ttsItems = DemoModelSetupState.items.filter { it.category == "Voice Synthesis" }

    val readySttCount = sttItems.count { DemoModelSetupState.statuses[it.id] == ModelDownloadStatus.READY }
    val readyTtsCount = ttsItems.count { DemoModelSetupState.statuses[it.id] == ModelDownloadStatus.READY }

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
                DemoBadge(label = "Demo Setup", family = p.orchid)
            }
            Text(
                text = "Download language models for offline communication",
                fontSize = Tokens.BodySmall,
                color = p.muted,
                lineHeight = Tokens.BodySmall * 1.35f,
            )
        }

        // Section 1: Speech Recognition (STT)
        SectionHeader(
            title = "SPEECH RECOGNITION (STT)",
            countText = "$readySttCount of ${sttItems.size} ready",
            family = p.sky,
        )

        sttItems.forEach { item ->
            val status = DemoModelSetupState.statuses[item.id] ?: ModelDownloadStatus.NOT_DOWNLOADED
            val progress = DemoModelSetupState.progresses[item.id] ?: 0f
            val downloadedMb = DemoModelSetupState.downloadedMbs[item.id] ?: 0

            ModelCard(
                item = item,
                status = status,
                progress = progress,
                downloadedMb = downloadedMb,
                onDownloadClick = {
                    if (status == ModelDownloadStatus.NOT_DOWNLOADED) {
                        scope.launch {
                            simulateDownload(item.id, item.totalSizeMb)
                        }
                    }
                },
            )
        }

        Spacer(Modifier.height(4.dp))

        // Section 2: Voice Synthesis (TTS)
        SectionHeader(
            title = "VOICE SYNTHESIS (TTS)",
            countText = "$readyTtsCount of ${ttsItems.size} ready",
            family = p.mint,
        )

        ttsItems.forEach { item ->
            val status = DemoModelSetupState.statuses[item.id] ?: ModelDownloadStatus.NOT_DOWNLOADED
            val progress = DemoModelSetupState.progresses[item.id] ?: 0f
            val downloadedMb = DemoModelSetupState.downloadedMbs[item.id] ?: 0

            ModelCard(
                item = item,
                status = status,
                progress = progress,
                downloadedMb = downloadedMb,
                onDownloadClick = {
                    if (status == ModelDownloadStatus.NOT_DOWNLOADED) {
                        scope.launch {
                            simulateDownload(item.id, item.totalSizeMb)
                        }
                    }
                },
            )
        }

        Spacer(Modifier.height(6.dp))

        // Summary Section
        OfflineSummaryCard(
            sttReady = readySttCount,
            sttTotal = sttItems.size,
            ttsReady = readyTtsCount,
            ttsTotal = ttsItems.size,
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
 * Simulates progressive local download steps with smooth visual delay.
 */
private suspend fun simulateDownload(id: String, totalMb: Int) {
    DemoModelSetupState.statuses[id] = ModelDownloadStatus.DOWNLOADING
    DemoModelSetupState.progresses[id] = 0f
    DemoModelSetupState.downloadedMbs[id] = 0

    val steps = listOf(
        0.08f to 120L,
        0.22f to 180L,
        0.41f to 220L,
        0.58f to 200L,
        0.75f to 240L,
        0.89f to 190L,
        0.96f to 160L,
        1.00f to 140L,
    )

    for ((targetProgress, delayMillis) in steps) {
        delay(delayMillis)
        DemoModelSetupState.progresses[id] = targetProgress
        DemoModelSetupState.downloadedMbs[id] = (totalMb * targetProgress).toInt().coerceAtMost(totalMb)
    }

    delay(100L)
    DemoModelSetupState.statuses[id] = ModelDownloadStatus.READY
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

@Composable
private fun ModelCard(
    item: DemoModelItem,
    status: ModelDownloadStatus,
    progress: Float,
    downloadedMb: Int,
    onDownloadClick: () -> Unit,
) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusTile)
    val isReady = status == ModelDownloadStatus.READY
    val isDownloading = status == ModelDownloadStatus.DOWNLOADING

    val borderColor = when {
        isReady -> p.mint.mid
        isDownloading -> p.sky.mid
        else -> p.hairline
    }

    val borderWidth = if (isReady || isDownloading) Tokens.SignalBorder else Tokens.Hairline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.paper, shape)
            .border(borderWidth, borderColor, shape)
            .padding(16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.languageName} ${item.category}, ${item.modelName}, ${item.totalSizeMb} megabytes. Status: ${
                    when (status) {
                        ModelDownloadStatus.NOT_DOWNLOADED -> "Not downloaded"
                        ModelDownloadStatus.DOWNLOADING -> "Downloading ${(progress * 100).toInt()} percent"
                        ModelDownloadStatus.READY -> "Ready for offline use"
                    }
                }"
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Row: Language + Badge + Category Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Language Code Chip
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                    text = item.languageCode,
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
                        text = item.languageName,
                        fontSize = Tokens.Subtitle,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                    )
                    if (item.nativeScript != item.languageName) {
                        Text(
                            text = "· ${item.nativeScript}",
                            fontSize = Tokens.Status,
                            fontWeight = FontWeight.Medium,
                            lineHeight = Tokens.Status * Tokens.INDIC_LINE_HEIGHT,
                            color = p.muted,
                        )
                    }
                }
                Text(
                    text = item.category,
                    fontSize = Tokens.Instrument,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = p.muted,
                )
            }

            // Size Badge
            Text(
                text = "${item.totalSizeMb} MB",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.ink,
                modifier = Modifier
                    .background(p.sunken, RoundedCornerShape(Tokens.RadiusInset))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        // Middle Row: Model Architecture & Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isReady) p.mint.core else p.muted, CircleShape),
                )
                Text(
                    text = item.modelName,
                    fontSize = Tokens.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = p.ink,
                )
            }

            // Current Status Indicator
            when (status) {
                ModelDownloadStatus.NOT_DOWNLOADED -> {
                    Text(
                        text = "Not downloaded",
                        fontSize = Tokens.Instrument,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                }
                ModelDownloadStatus.DOWNLOADING -> {
                    Text(
                        text = "$downloadedMb MB / ${item.totalSizeMb} MB",
                        fontSize = Tokens.Instrument,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = p.sky.deep,
                    )
                }
                ModelDownloadStatus.READY -> {
                    Text(
                        text = "✓ Ready for offline use",
                        fontSize = Tokens.Instrument,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.mint.deep,
                    )
                }
            }
        }

        // Progress Bar (Visible while downloading)
        if (isDownloading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val animatedProgress by animateFloatAsState(targetValue = progress, label = "model_progress")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(p.sunken, RoundedCornerShape(Tokens.RadiusPill)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .background(p.sky.core, RoundedCornerShape(Tokens.RadiusPill)),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Downloading offline weights...",
                        fontSize = Tokens.Caption,
                        color = p.muted,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = Tokens.Caption,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.sky.deep,
                    )
                }
            }
        }

        // Action Button
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            when (status) {
                ModelDownloadStatus.NOT_DOWNLOADED -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusControl))
                            .clickable(onClick = onDownloadClick)
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
                            text = "Download",
                            fontSize = Tokens.BodySmall,
                            fontWeight = FontWeight.Bold,
                            color = p.onAccent,
                        )
                    }
                }
                ModelDownloadStatus.DOWNLOADING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .background(p.sky.tint, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, p.sky.mid, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Hourglass,
                            contentDescription = null,
                            tint = p.sky.core,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Downloading ${(progress * 100).toInt()}%",
                            fontSize = Tokens.BodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = p.sky.deep,
                        )
                    }
                }
                ModelDownloadStatus.READY -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .background(p.mint.tint, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, p.mint.mid, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Tick,
                            contentDescription = null,
                            tint = p.mint.core,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Downloaded",
                            fontSize = Tokens.BodySmall,
                            fontWeight = FontWeight.Bold,
                            color = p.mint.deep,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineSummaryCard(
    sttReady: Int,
    sttTotal: Int,
    ttsReady: Int,
    ttsTotal: Int,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Offline AI Models",
                fontSize = Tokens.Subtitle,
                fontWeight = FontWeight.Bold,
                color = p.ink,
            )
            DemoBadge(label = "Ready Status", family = p.mint)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryChip(
                count = sttReady,
                total = sttTotal,
                label = "STT models ready",
                family = p.sky,
                modifier = Modifier.weight(1f),
            )
            SummaryChip(
                count = ttsReady,
                total = ttsTotal,
                label = "TTS voices ready",
                family = p.mint,
                modifier = Modifier.weight(1f),
            )
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
                text = "Internet required only for initial model setup",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = p.muted,
            )
        }
    }
}

@Composable
private fun SummaryChip(
    count: Int,
    total: Int,
    label: String,
    family: ItantraPalette.Family,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(family.tint, RoundedCornerShape(Tokens.RadiusControl))
            .border(Tokens.Hairline, family.mid, RoundedCornerShape(Tokens.RadiusControl))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "$count of $total ready",
            fontSize = Tokens.Callout,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = family.deep,
        )
        Text(
            text = label,
            fontSize = Tokens.Instrument,
            color = family.deep,
        )
    }
}

@Composable
private fun DemoBadge(
    label: String,
    family: ItantraPalette.Family,
) {
    Text(
        text = label,
        fontSize = Tokens.Instrument,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = family.deep,
        modifier = Modifier
            .background(family.tint, RoundedCornerShape(Tokens.RadiusInset))
            .border(Tokens.Hairline, family.mid, RoundedCornerShape(Tokens.RadiusInset))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
