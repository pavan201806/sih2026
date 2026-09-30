package org.itantra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.itantra.audio.EngineState
import java.util.Locale

/**
 * OperatingScreen restyled to match Stitch 02_communication_dashboard and 03_emergency_communication.
 * Preserves 100% of the underlying communication architecture, STT/TTS engine, and state.
 */
@Composable
fun OperatingScreen(
    state: OperatingState,
    onTransmitChange: (Boolean) -> Unit,
    onAlert: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onMenu: () -> Unit,
    onReplay: (String) -> Unit = {},
    onModeChange: (String) -> Unit = {},
    onLocate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val p = palette
    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        ChromeHeader(state, onMenu, onLanguageSelected, onModeChange)

        state.degraded?.let { DegradedBanner(it) }

        ThreadPane(state, onReplay, Modifier.fillMaxWidth().weight(1f))

        if (state.transmitting || state.partial != null) PartialStrip(state)
        state.speechNote?.let { SpeechNote(it) }

        Dock(state, onTransmitChange, onAlert, onReplay, onLocate)
        InstrumentStrip(state)
    }
}

/** Everything the screen needs, and nothing about how it was obtained. */
data class OperatingState(
    val unitName: String,
    val nodeId: Int,
    val peerCount: Int,
    val linkUp: Boolean,
    val transportName: String,
    val mode: String,
    val audience: String,
    val language: String,
    val languageCode: String = "",
    val languages: List<LanguageOption> = emptyList(),
    val transmitting: Boolean = false,
    val listening: Boolean = false,
    val partial: String? = null,
    val confidence: Int? = null,
    val level: Float = 0f,
    val speechNote: String? = null,
    val messages: List<LoggedMessage> = emptyList(),
    val degraded: EngineState.Degraded.Reason? = null,
    val metrics: BandFMetrics = BandFMetrics(),
    val queued: Int = 0,
    val speakingFrom: String? = null,
    val units: List<UnitInfo> = emptyList(),
    val openLinePaused: Boolean = false,
)

/** The instrument strip's numbers, from the utterance that just happened. */
data class BandFMetrics(
    val sttMillis: Long? = null,
    val linkMillis: Long? = null,
    val ttsMillis: Long? = null,
    val totalMillis: Long? = null,
    val realTimeFactor: Double? = null,
    val cpuCores: Double? = null,
    val cpuCoreCount: Int? = null,
    val lastFrameBytes: Int? = null,
    val audioMillis: Long? = null,
) {
    val compressionRatio: Int?
        get() {
            val bytes = lastFrameBytes?.takeIf { it > 0 } ?: return null
            val audio = audioMillis?.takeIf { it > 0 }?.let { it * BYTES_PER_SECOND / 1000.0 }
            return Math.round((audio ?: RAW_AUDIO_BYTES) / bytes).toInt()
        }

    private companion object {
        const val RAW_AUDIO_BYTES = 96_000.0
        const val BYTES_PER_SECOND = 32_000
    }
}

// ── chrome ───────────────────────────────────────────────────────────────────

@Composable
private fun ChromeHeader(
    state: OperatingState,
    onMenu: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onModeChange: (String) -> Unit,
) {
    val p = palette
    Column(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = Tokens.StatusBand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .sizeIn(minWidth = Tokens.TouchTarget, minHeight = Tokens.TouchTarget)
                    .clickable(onClick = onMenu)
                    .semantics(mergeDescendants = true) { contentDescription = "Menu" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Grid, contentDescription = null, tint = p.periwinkle.core, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    state.unitName,
                    fontSize = Tokens.Body,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    "NODE ${"%02d".format(state.nodeId)} // ${state.transportName.uppercase()}",
                    fontSize = Tokens.Instrument,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
            }
            Pill(
                text = "${state.peerCount} UNITS",
                fill = p.aqua.tint,
                border = p.aqua.mid,
                ink = p.aqua.deep,
            )
            LinkPill(state)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeSegments(state.mode, onModeChange)
            Spacer(Modifier.weight(1f))
            LanguageChip(state, onLanguageSelected)
        }
    }
}

@Composable
private fun Pill(
    text: String,
    fill: Color,
    border: Color?,
    ink: Color,
    leading: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Tokens.RadiusPill)
    Row(
        modifier
            .background(fill, shape)
            .then(if (border != null) Modifier.border(Tokens.Hairline, border, shape) else Modifier)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading?.invoke()
        Text(text, fontSize = Tokens.Label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ink)
    }
}

@Composable
private fun LinkPill(state: OperatingState) {
    val p = palette
    val family = if (state.linkUp) p.mint else p.blush
    Pill(
        text = if (state.linkUp) "LINK OK" else "NO LINK",
        fill = family.tint,
        border = family.mid,
        ink = family.deep,
        leading = {
            Box(
                Modifier
                    .size(7.dp)
                    .then(if (state.linkUp) Modifier.alpha(pulseAlpha()) else Modifier)
                    .background(family.core, CircleShape),
            )
        },
        modifier =
            Modifier.semantics(mergeDescendants = true) {
                contentDescription =
                    if (state.linkUp) {
                        "Link up over ${state.transportName}, ${state.peerCount} units"
                    } else {
                        "No link. ${state.queued} messages waiting."
                    }
            },
    )
}

@Composable
private fun ModeSegments(
    mode: String,
    onModeChange: (String) -> Unit,
) {
    val p = palette
    val phone = mode.equals("Phone", ignoreCase = true)
    Row(
        Modifier
            .background(p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusPill))
            .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusPill))
            .padding(2.dp)
            .semantics { contentDescription = if (phone) "Open line mode" else "Push to talk mode" },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf("PTT" to !phone, "Phone" to phone).forEach { (label, selected) ->
            Text(
                label,
                fontSize = Tokens.Label,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) p.onAccent else p.muted,
                modifier =
                    Modifier
                        .then(
                            if (selected) {
                                Modifier.background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusPill))
                            } else {
                                Modifier
                            },
                        )
                        .clickable(enabled = !selected) { onModeChange(label) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics {
                            contentDescription =
                                if (label == "PTT") "Switch to push to talk" else "Switch to the open line"
                        },
            )
        }
    }
}

@Composable
private fun LanguageChip(
    state: OperatingState,
    onLanguageSelected: (String) -> Unit,
) {
    val p = palette
    var open by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Tokens.RadiusPill)
    Box {
        Box(
            Modifier
                .heightIn(min = Tokens.TouchTarget)
                .clickable { open = true }
                .semantics(mergeDescendants = true) {
                    contentDescription = "Language ${state.language}. Change."
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                Modifier
                    .heightIn(min = 36.dp)
                    .background(p.surfaceContainerLow, shape)
                    .border(Tokens.Hairline, p.periwinkle.core, shape)
                    .padding(start = 10.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    state.language,
                    fontSize = Tokens.Callout,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                    lineHeight = Tokens.Callout * Tokens.INDIC_LINE_HEIGHT,
                )
                Icon(Icons.Caret, contentDescription = null, tint = p.periwinkle.core, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            state.languages.forEach { option ->
                val current = option.code == state.languageCode
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                option.nativeName,
                                fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                                color = if (current) p.periwinkle.core else p.ink,
                            )
                            if (option.englishName != option.nativeName) {
                                Text(
                                    "(${option.englishName})",
                                    fontSize = Tokens.Label,
                                    color = p.muted,
                                )
                            }
                            if (current) {
                                Box(Modifier.size(6.dp).background(p.periwinkle.core, CircleShape))
                            }
                        }
                    },
                    onClick = {
                        open = false
                        onLanguageSelected(option.code)
                    },
                )
            }
        }
    }
}

// ── thread ───────────────────────────────────────────────────────────────────

@Composable
private fun ThreadPane(
    state: OperatingState,
    onReplay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    if (state.messages.isEmpty()) {
        EmptyState(
            icon = Icons.Transmit,
            title = "Channel is quiet",
            body = "Hold the PTT control below to transmit on this channel.",
            modifier = modifier,
        )
        return
    }
    val list = rememberLazyListState()
    val reduced = reducedMotion
    LaunchedEffect(state.messages.size) {
        val last = state.messages.lastIndex
        if (last < 0) return@LaunchedEffect
        if (reduced) list.scrollToItem(last) else list.animateScrollToItem(last)
    }
    LazyColumn(
        modifier,
        state = list,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(state.messages) { message ->
            MessageBubble(
                message = message,
                speaking = state.speakingFrom != null && state.speakingFrom == message.from,
                onReplay = onReplay,
            )
        }
    }
}

// ── the live strip ───────────────────────────────────────────────────────────

@Composable
private fun PartialStrip(state: OperatingState) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusControl)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(p.surfaceContainerLowest, shape)
            .border(Tokens.Hairline, p.periwinkle.core, shape)
            .heightIn(min = 44.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = state.partial?.let { "Heard: $it" } ?: "Listening."
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            state.partial?.let { "“$it”" } ?: "Listening …",
            fontSize = Tokens.Body,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = p.ink,
            lineHeight = Tokens.Body * Tokens.INDIC_LINE_HEIGHT,
            modifier = Modifier.weight(1f),
        )
        state.confidence?.let { level -> ConfidenceDots(level, p.periwinkle.core) }
    }
}

@Composable
private fun ConfidenceDots(
    level: Int,
    colour: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.decorative()) {
        repeat(4) { index ->
            Box(
                Modifier
                    .size(6.dp)
                    .then(
                        if (index < level) {
                            Modifier.background(colour, CircleShape)
                        } else {
                            Modifier.border(1.dp, colour, CircleShape)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun SpeechNote(note: String) {
    val p = palette
    Text(
        note,
        fontSize = Tokens.Label,
        fontFamily = FontFamily.Monospace,
        color = p.apricot.deep,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

// ── the dock ─────────────────────────────────────────────────────────────────

@Composable
private fun Dock(
    state: OperatingState,
    onTransmitChange: (Boolean) -> Unit,
    onAlert: () -> Unit,
    onReplay: (String) -> Unit,
    onLocate: () -> Unit,
) {
    val p = palette
    val dock = dockStateOf(state)

    Column(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(start = Tokens.ScreenMargin, end = Tokens.ScreenMargin, top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DockHint(dock, state)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (dock == DockState.PHONE) {
                FlankButton(
                    if (state.openLinePaused) Icons.Play else Icons.Pause,
                    if (state.openLinePaused) "RESUME" else "HOLD",
                    p.butter,
                    enabled = true,
                    onClick = { onTransmitChange(true) },
                )
            } else {
                FlankButton(Icons.Alert, "ALERT", p.blush, enabled = true, strong = true, onClick = onAlert)
            }

            TransmitCircle(state, dock, onTransmitChange)

            FlankButton(
                icon = Icons.Globe,
                label = "LOCATE",
                family = p.aqua,
                enabled = true,
                onClick = onLocate,
            )
        }
    }
}

@Composable
private fun DockHint(
    dock: DockState,
    state: OperatingState,
) {
    val p = palette
    when (dock) {
        DockState.IDLE ->
            Text(
                "HOLD PTT OR VOLUME DOWN TO SPEAK",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
            )
        DockState.SEIZED ->
            Text(
                "OPENING MICROPHONE …",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.periwinkle.core,
            )
        DockState.LIVE ->
            Text(
                "SPEAK NOW // TRANSMITTING",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.mint.core,
            )
        DockState.BUSY ->
            Text(
                "CHANNEL BUSY",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.apricot.deep,
            )
        DockState.PHONE ->
            Text(
                if (state.openLinePaused) "LINE PAUSED // PRESS RESUME" else "FULL DUPLEX LINE OPEN",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.periwinkle.core,
            )
    }
}

@Composable
private fun FlankButton(
    icon: ImageVector,
    label: String,
    family: ItantraPalette.Family,
    enabled: Boolean,
    strong: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.RadiusControl)
    Column(
        Modifier
            .size(Tokens.DockFlank)
            .background(if (strong) family.core else family.tint, shape)
            .border(Tokens.Hairline, family.mid, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = label
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (strong) Color.White else family.deep, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (strong) Color.White else family.deep,
        )
    }
}

@Composable
private fun TransmitCircle(
    state: OperatingState,
    dock: DockState,
    onTransmitChange: (Boolean) -> Unit,
) {
    val p = palette
    val reduced = reducedMotion
    val active = dock == DockState.LIVE || dock == DockState.SEIZED

    Box(
        Modifier
            .size(Tokens.TransmitCircle)
            .pointerInput(dock) {
                if (dock == DockState.BUSY) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onTransmitChange(true)
                        tryAwaitRelease()
                        onTransmitChange(false)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // Outer glow halo while active
        if (active && !reduced) {
            Box(
                Modifier
                    .size(Tokens.TransmitCircle)
                    .border(2.dp, p.periwinkle.core, CircleShape),
            )
        }

        Box(
            Modifier
                .size(Tokens.TransmitCircle - 16.dp)
                .background(if (active) p.periwinkle.core else p.surfaceContainerLow, CircleShape)
                .border(2.dp, if (active) p.periwinkle.mid else p.periwinkle.core, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Transmit,
                    contentDescription = null,
                    tint = if (active) p.onAccent else p.periwinkle.core,
                    modifier = Modifier.size(36.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (active) "TRANSMIT" else "PTT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = if (active) p.onAccent else p.periwinkle.core,
                )
            }
        }
    }
}

// ── instrumentation ──────────────────────────────────────────────────────────

@Composable
private fun InstrumentStrip(state: OperatingState) {
    val p = palette
    val m = state.metrics
    val live = state.transmitting
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.InstrumentBand)
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(start = Tokens.ScreenMargin, end = Tokens.ScreenMargin, top = 6.dp, bottom = 6.dp)
            .semantics { contentDescription = spokenMetrics(m) },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (live) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Instrument(if (state.listening) "LISTENING // RAW PCM STREAM" else "HOLDING FLOOR // MIC SEIZED")
                Instrument("CPU  ${cpuLabel(m.cpuCores, m.cpuCoreCount)}")
            }
            return@Column
        }
        Instrument("STT ${ms(m.sttMillis)} // LINK ${ms(m.linkMillis)} // TTS ${ms(m.ttsMillis)}")
        Instrument(
            buildString {
                append("TOTAL ${ms(m.totalMillis)}")
                append(" // RTF ${m.realTimeFactor?.let { "%.2f".format(Locale.ROOT, it) } ?: "—"}")
                append(" // CPU ${cpuLabel(m.cpuCores, m.cpuCoreCount)}")
                m.lastFrameBytes?.let { append(" // $it B ${m.compressionRatio}×") }
            },
        )
    }
}

@Composable
private fun Instrument(text: String) {
    val p = palette
    Text(
        text,
        fontSize = Tokens.Instrument,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        color = p.sky.deep,
    )
}

private fun ms(value: Long?): String = value?.let { "$it ms" } ?: "—"

internal fun cpuLabel(
    cores: Double?,
    of: Int?,
): String {
    if (cores == null) return "—"
    val used = "%.2f".format(Locale.ROOT, cores)
    return if (of != null && of > 0) "$used/$of cores" else "$used cores"
}

internal fun spokenMetrics(m: BandFMetrics): String {
    val parts = ArrayList<String>()
    m.totalMillis?.let { parts += "Total $it milliseconds" }
    m.sttMillis?.let { parts += "Recognition $it" }
    m.linkMillis?.let { parts += "Link $it" }
    m.ttsMillis?.let { parts += "Speech $it" }
    m.lastFrameBytes?.let {
        parts += "Last frame $it bytes, ${m.compressionRatio} times smaller than audio"
    }
    m.realTimeFactor?.let { parts += "Real time factor ${"%.2f".format(Locale.ROOT, it)}" }
    m.cpuCores?.let {
        val used = "%.2f".format(Locale.ROOT, it)
        parts += m.cpuCoreCount
            ?.let { n -> "Processor $used of $n cores" }
            ?: "Processor $used cores"
    }
    if (parts.isEmpty()) return "Nothing measured yet."
    return "Latency. " + parts.joinToString(". ") + "."
}
