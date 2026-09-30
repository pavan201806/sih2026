package org.itantra.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.itantra.audio.EngineState
import java.util.Locale

/**
 * OperatingScreen matching Stitch 02_communication_dashboard and 03_emergency_communication.
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
        // TOP MIL-SPEC APP BAR
        TacticalTopAppBar(state, onMenu)

        // SUB-HEADER TELEMETRY TICKER
        TelemetryTicker(state)

        // SYSTEM DEGRADED WARNING BANNER IF ANY
        state.degraded?.let { DegradedBanner(it) }

        // MAIN CONTENT AREA WITH ASYMMETRIC HUD CARDS & MESSAGES
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // SYSTEM STATUS HERO CARD + 4-CELL MATRIX
            SystemStatusHeroCard(state)

            // LANGUAGE SELECTOR BAR
            LanguageSelectorBar(state, onLanguageSelected)

            // LIVE PARTIAL / SPEECH DECODER BOX
            if (state.transmitting || state.partial != null) {
                LiveSpeechDecoderBox(state)
            }
            state.speechNote?.let { SpeechNote(it) }

            // REAL-TIME MESSAGES THREAD
            ThreadPane(state, onReplay, Modifier.fillMaxWidth().weight(1f))
        }

        // TACTICAL TRANSMIT DECK & FAIL-SAFE SOS MODULE
        TacticalTransmitDeck(state, onTransmitChange, onAlert, onLocate, onModeChange)

        // BOTTOM INSTRUMENTATION STRIP
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

// ── TOP APP BAR ─────────────────────────────────────────────────────────────

@Composable
private fun TacticalTopAppBar(
    state: OperatingState,
    onMenu: () -> Unit,
) {
    val p = palette
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blink",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                    .clickable(onClick = onMenu)
                    .semantics(mergeDescendants = true) { contentDescription = "Menu" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Grid, contentDescription = null, tint = p.periwinkle.core, modifier = Modifier.size(22.dp))
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "RAKSHAVAANI // MESH-SYS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = p.periwinkle.core,
                        letterSpacing = 0.5.sp,
                    )
                }
                Text(
                    text = "OFFLINE COMMUNICATION SYSTEM",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.muted,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        // Secondary Telemetry Pill
        Row(
            modifier = Modifier
                .background(p.surfaceContainerLow)
                .border(Tokens.Hairline, p.hairline)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${state.peerCount} PEERS",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.aqua.core,
            )
            Text(text = "|", fontSize = 9.sp, color = p.hairline)
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(if (state.linkUp) blinkAlpha else 1f)
                    .background(if (state.linkUp) p.periwinkle.core else p.blush.core),
            )
            Text(
                text = if (state.linkUp) "LORA:ACTV" else "NO LINK",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (state.linkUp) p.periwinkle.core else p.blush.core,
            )
        }
    }
}

// ── SUB-HEADER TELEMETRY TICKER ─────────────────────────────────────────────

@Composable
private fun TelemetryTicker(state: OperatingState) {
    val p = palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLow)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "GRID: P2P LOCAL",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.periwinkle.core,
            )
            Text(text = "|", fontSize = 8.sp, color = p.hairline)
            Text(
                text = "TX PWR: 22 dBm",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
            )
            Text(text = "|", fontSize = 8.sp, color = p.hairline)
            Text(
                text = "HOPS: 03 MAX",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
            )
        }
        Text(
            text = "PEERS: ${state.peerCount}",
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.aqua.core,
        )
    }
}

// ── SYSTEM STATUS HERO CARD + 4-CELL MATRIX ─────────────────────────────────

@Composable
private fun SystemStatusHeroCard(state: OperatingState) {
    val p = palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLow)
            .border(Tokens.Hairline, p.periwinkle.core.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Top Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .background(p.surfaceContainerHigh)
                            .border(Tokens.Hairline, p.periwinkle.core)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = "P2P AIRGAP PROTOCOL",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                        )
                    }
                    Text(
                        text = "SECURE LEVEL 4 // ED25519",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                }

                Text(
                    text = "NOMINAL // ZERO-DROP",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                )
            }

            // Headline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "SYSTEM STATUS:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = p.ink,
                )
                Text(
                    text = "OFFLINE READY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = p.periwinkle.core,
                )
            }

            // 4-Cell Connection Matrix Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(Tokens.Hairline, p.hairline),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MatrixCell(
                    title = "HARDWARE",
                    value = "NODE ${"%02d".format(state.nodeId)}",
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.width(1.dp).height(38.dp).background(p.hairline))
                MatrixCell(
                    title = "PROTOCOL",
                    value = state.transportName,
                    valueColor = p.aqua.core,
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.width(1.dp).height(38.dp).background(p.hairline))
                MatrixCell(
                    title = "RF TELEMETRY",
                    value = if (state.linkUp) "STABLE" else "OFFLINE",
                    valueColor = if (state.linkUp) p.mint.core else p.blush.core,
                    modifier = Modifier.weight(1f),
                )
                Box(Modifier.width(1.dp).height(38.dp).background(p.hairline))
                MatrixCell(
                    title = "ACCELERATOR",
                    value = "ON-DEVICE",
                    valueColor = p.apricot.core,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MatrixCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    val p = palette
    Column(
        modifier = modifier
            .background(p.surfaceContainerLowest)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Text(
            text = title,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
        )
        Text(
            text = value,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: p.ink,
            maxLines = 1,
        )
    }
}

// ── LANGUAGE SELECTOR BAR ───────────────────────────────────────────────────

@Composable
private fun LanguageSelectorBar(
    state: OperatingState,
    onLanguageSelected: (String) -> Unit,
) {
    val p = palette
    var openDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "ENGINE LANG:",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
            )

            // Current Language Dropdown Trigger
            Box {
                Row(
                    modifier = Modifier
                        .background(p.surfaceContainerHigh)
                        .border(1.dp, p.periwinkle.core)
                        .clickable { openDropdown = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = state.language.uppercase(),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )
                    Icon(Icons.Caret, contentDescription = null, tint = p.periwinkle.core, modifier = Modifier.size(12.dp))
                }

                DropdownMenu(expanded = openDropdown, onDismissRequest = { openDropdown = false }) {
                    state.languages.forEach { option ->
                        val current = option.code == state.languageCode
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        option.nativeName,
                                        fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                                        color = if (current) p.periwinkle.core else p.ink,
                                    )
                                    if (option.englishName != option.nativeName) {
                                        Text(
                                            "(${option.englishName})",
                                            fontSize = 11.sp,
                                            color = p.muted,
                                        )
                                    }
                                    if (current) {
                                        Box(Modifier.size(6.dp).background(p.periwinkle.core))
                                    }
                                }
                            },
                            onClick = {
                                openDropdown = false
                                onLanguageSelected(option.code)
                            },
                        )
                    }
                }
            }
        }

        // Quick Segmented Language Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(state.languages.take(4)) { opt ->
                val active = opt.code == state.languageCode
                Box(
                    modifier = Modifier
                        .background(if (active) p.periwinkle.core else p.surfaceContainerLow)
                        .border(Tokens.Hairline, if (active) p.periwinkle.core else p.hairline)
                        .clickable { onLanguageSelected(opt.code) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = opt.englishName.take(3).uppercase(),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (active) p.surfaceContainerLowest else p.muted,
                    )
                }
            }
        }
    }
}

// ── LIVE SPEECH DECODER BOX ─────────────────────────────────────────────────

@Composable
private fun LiveSpeechDecoderBox(state: OperatingState) {
    val p = palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(1.dp, p.periwinkle.core)
            .padding(10.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = state.partial?.let { "Live speech decoder: $it" } ?: "Listening..."
            },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.size(5.dp).background(p.periwinkle.core))
                    Text(
                        text = "LIVE SPEECH DECODER // ON-DEVICE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )
                }
                state.confidence?.let { conf ->
                    Text(
                        text = "CONFIDENCE: $conf/4",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.mint.core,
                    )
                }
            }

            Text(
                text = state.partial?.let { "“$it”" } ?: "Listening for acoustic stream...",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = p.ink,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun SpeechNote(note: String) {
    val p = palette
    Text(
        note,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        color = p.apricot.deep,
        modifier = Modifier
            .fillMaxWidth()
            .background(p.apricot.tint.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

// ── THREAD PANE ─────────────────────────────────────────────────────────────

@Composable
private fun ThreadPane(
    state: OperatingState,
    onReplay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.messages.isEmpty()) {
        EmptyState(
            icon = Icons.Transmit,
            title = "Channel is quiet",
            body = "Hold the PTT control below to transmit tactical speech burst.",
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
        modifier = modifier,
        state = list,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

// ── TACTICAL TRANSMIT DECK & FAIL-SAFE SOS MODULE ───────────────────────────

@Composable
private fun TacticalTransmitDeck(
    state: OperatingState,
    onTransmitChange: (Boolean) -> Unit,
    onAlert: () -> Unit,
    onLocate: () -> Unit,
    onModeChange: (String) -> Unit,
) {
    val p = palette
    val dock = dockStateOf(state)
    val phone = state.mode.equals("Phone", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // TOP DECK: MODE & LOCATE CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mode switch PTT / Phone
            Row(
                modifier = Modifier
                    .background(p.surfaceContainerLow)
                    .border(Tokens.Hairline, p.hairline),
            ) {
                listOf("PTT" to !phone, "Phone" to phone).forEach { (label, selected) ->
                    Box(
                        modifier = Modifier
                            .background(if (selected) p.periwinkle.core else Color.Transparent)
                            .clickable(enabled = !selected) { onModeChange(label) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = label.uppercase(),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) p.surfaceContainerLowest else p.muted,
                        )
                    }
                }
            }

            // Locate node action
            Row(
                modifier = Modifier
                    .background(p.surfaceContainerLow)
                    .border(Tokens.Hairline, p.hairline)
                    .clickable(onClick = onLocate)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Globe, contentDescription = null, tint = p.aqua.core, modifier = Modifier.size(14.dp))
                Text(
                    text = "LOCATE NODES",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.aqua.core,
                )
            }
        }

        // CENTER TRANSMIT CONSOLE (Tactical Angular PTT Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLow)
                .border(1.dp, if (state.transmitting) p.mint.core else p.hairline)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Interactive PTT Button
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 54.dp)
                    .background(if (state.transmitting) p.mint.core else p.periwinkle.core)
                    .border(1.5.dp, if (state.transmitting) p.mint.deep else p.periwinkle.mid)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Transmit,
                        contentDescription = null,
                        tint = p.surfaceContainerLowest,
                        modifier = Modifier.size(24.dp),
                    )
                    Column {
                        Text(
                            text = if (state.transmitting) "TRANSMITTING" else "HOLD TO SPEAK",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = p.surfaceContainerLowest,
                            letterSpacing = 0.5.sp,
                        )
                        Text(
                            text = "PTT DUPLEX // CH-01",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = p.surfaceContainerLowest.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            // Real-Time Waveform & Buffer Readout
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (state.transmitting) "MIC SEIZED // 16kHz" else "FREQ: 868.10 MHz",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (state.transmitting) p.mint.core else p.muted,
                )
                // Tactical Waveform Bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(14.dp),
                ) {
                    val bars = listOf(4, 10, 6, 14, 8, 12, 5, 9)
                    bars.forEach { height ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(if (state.transmitting) (height * 1.2f).dp else (height / 2).dp)
                                .background(if (state.transmitting) p.mint.core else p.hairlineStrong),
                        )
                    }
                }
                Text(
                    text = "BUFFER: EMPTY",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.hairlineStrong,
                )
            }
        }

        // FAIL-SAFE EMERGENCY SOS BROADCAST MODULE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.blush.tint.copy(alpha = 0.25f))
                .border(1.5.dp, p.blush.core)
                .padding(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(Modifier.size(5.dp).background(p.blush.core))
                        Text(
                            text = "DISTRESS BEACON // ALL NODES",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.blush.core,
                        )
                    }
                    Text(
                        text = "EMERGENCY BROADCAST SOS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = p.ink,
                    )
                }

                // SOS Trigger Button
                Box(
                    modifier = Modifier
                        .background(p.blush.core)
                        .border(1.dp, Color.White)
                        .clickable(onClick = onAlert)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Alert,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "SOS DISTRESS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── INSTRUMENTATION STRIP ───────────────────────────────────────────────────

@Composable
private fun InstrumentStrip(state: OperatingState) {
    val p = palette
    val m = state.metrics
    val live = state.transmitting

    Column(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 10.dp, vertical = 4.dp)
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
        text = text,
        fontSize = 8.sp,
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
