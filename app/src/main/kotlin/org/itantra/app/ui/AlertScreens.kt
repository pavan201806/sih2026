package org.itantra.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The six template alerts. One byte of payload; 21 bytes on the wire, authenticated. */
enum class AlertTemplate(val code: Int, val label: String) {
    MEDICAL(1, "Medical"),
    FIRE(2, "Fire"),
    FLOOD(3, "Flood"),
    EVACUATE(4, "Evacuate"),
    EXTRACT(5, "Extract"),
    ALL_CLEAR(6, "All clear"),
    ;

    val icon: ImageVector
        get() =
            when (this) {
                MEDICAL -> Icons.Medical
                FIRE -> Icons.Fire
                FLOOD -> Icons.Flood
                EVACUATE -> Icons.Evacuate
                EXTRACT -> Icons.Extract
                ALL_CLEAR -> Icons.AllClear
            }

    val isGoodNews: Boolean get() = this == ALL_CLEAR
}

// ── BOARD 12: COMPOSE ALERT / EMERGENCY CHANNEL ─────────────────────────────

@Composable
fun AlertComposeScreen(
    onTemplate: (AlertTemplate) -> Unit,
    onHoldToSpeak: (Boolean) -> Unit,
    attachPosition: Boolean,
    onAttachPositionChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("EMERGENCY COMMS CONSOLE", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // SUB-STRIP: FIELD MODE & CRYPTO STATUS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(6.dp).background(p.blush.core))
                    Text(
                        text = "TACTICAL AUDIO CONSOLE // CH-01",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.blush.core,
                    )
                }
                Text(
                    text = "0% INTERNET (OFFLINE)",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.blush.core,
                )
            }

            // TEMPLATE CARDS GRID (3 rows of 2)
            Text(
                text = "AUTHENTICATED TEMPLATE DISPATCH (21 B AIRGAP PKT)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
                letterSpacing = 0.5.sp,
            )

            AlertTemplate.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { template ->
                        TemplateTile(template, Modifier.weight(1f)) { onTemplate(template) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(4.dp))

            // 5-STAGE SIGNAL CHAIN PIPELINE
            SignalChainPipeline()
        }

        // TACTICAL DOCK WITH HOLD-TO-SPEAK CONTROL
        DockShelf(hint = "HOLD TO SPEAK CUSTOM TACTICAL ALERT") {
            GpsFlank(attachPosition, onAttachPositionChange)
            HoldToSpeakCircle(onHoldToSpeak)
            SquareFlank(Icons.Cross, "CANCEL", enabled = true, onClick = onBack)
        }
    }
}

@Composable
private fun TemplateTile(
    template: AlertTemplate,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val p = palette
    val family = if (template.isGoodNews) p.mint else p.blush
    Column(
        modifier
            .heightIn(min = 88.dp)
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, if (template.isGoodNews) family.core else p.hairline)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .semantics(mergeDescendants = true) { contentDescription = template.label },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(template.icon, contentDescription = null, tint = family.core, modifier = Modifier.size(28.dp))
            Text(
                text = if (template.isGoodNews) "[ALL-CLEAR]" else "[URGENT]",
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                color = family.core,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                template.label.uppercase(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (template.isGoodNews) family.deep else p.ink,
            )
            if (template.isGoodNews) {
                Box(Modifier.size(5.dp).background(family.core))
            }
        }
    }
}

@Composable
private fun SignalChainPipeline() {
    val p = palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ON-DEVICE SIGNAL CHAIN (ZERO CLOUD DEPENDENCY)",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.hairlineStrong,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StageChip("01", "VOICE INPUT", Modifier.weight(1f))
                StageChip("02", "LOCAL STT", Modifier.weight(1f), isPrimary = true)
                StageChip("03", "ENCODED", Modifier.weight(1f))
                StageChip("04", "P2P LINK", Modifier.weight(1f))
                StageChip("05", "REMOTE TTS", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StageChip(
    stageNum: String,
    label: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    val p = palette
    Column(
        modifier = modifier
            .background(if (isPrimary) p.surfaceContainerHigh else p.surfaceContainerLow)
            .border(Tokens.Hairline, if (isPrimary) p.periwinkle.core else p.hairline)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "STG $stageNum", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
        Text(
            text = label,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) p.periwinkle.core else p.ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun GpsFlank(
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val p = palette
    Column(
        Modifier
            .sizeIn(minWidth = Tokens.DockFlank, minHeight = Tokens.DockFlank)
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .clickable { onChange(!checked) }
            .semantics(mergeDescendants = true) {
                contentDescription = if (checked) "Attach my position, on" else "Attach my position, off"
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(if (checked) p.mint.core else Color.Transparent)
                .border(1.5.dp, if (checked) p.mint.core else p.muted),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(Icons.Tick, contentDescription = null, tint = p.onAccent, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text("GPS", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.muted)
    }
}

@Composable
private fun HoldToSpeakCircle(onHold: (Boolean) -> Unit) {
    val p = palette
    Box(
        modifier = Modifier
            .size(width = 150.dp, height = 54.dp)
            .background(p.blush.core)
            .border(1.5.dp, p.blush.deep)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHold(true)
                        tryAwaitRelease()
                        onHold(false)
                    },
                )
            }
            .semantics(mergeDescendants = true) {
                contentDescription = "Hold to speak your own alert"
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Transmit, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = "HOLD TO SPEAK",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "PTT DUPLEX // CH-01",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}

// ── BOARD 13: CONFIRM ALERT BEFORE TRANSMITTING ─────────────────────────────

@Composable
fun AlertConfirmScreen(
    text: String,
    onSpeak: (String) -> Unit,
    onRetake: () -> Unit,
    onSend: () -> Unit,
    confidence: Int = 1,
    frameBytes: Int? = null,
    language: String? = null,
    modifier: Modifier = Modifier,
) {
    val p = palette
    LaunchedEffect(text) { onSpeak(text) }

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("CHECK BEFORE SENDING", onRetake)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "WHAT THE MACHINE HEARD (ON-DEVICE STT)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
                letterSpacing = 0.5.sp,
            )

            // Recognized text card
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "“$text”",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = p.ink,
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(p.sunken))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(5.dp).background(if (confidence >= 3) p.mint.core else p.apricot.core))
                            Text(
                                text = if (confidence >= 3) "HIGH CONFIDENCE" else "LOW CONFIDENCE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (confidence >= 3) p.mint.core else p.apricot.core,
                            )
                        }
                        if (frameBytes != null || language != null) {
                            Text(
                                listOfNotNull(frameBytes?.let { "$it B" }, language).joinToString(" · "),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }
                    }
                }
            }

            // Hear it back card
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.aqua.mid)
                    .clickable { onSpeak(text) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(36.dp).background(p.aqua.tint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Play, contentDescription = null, tint = p.aqua.core, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        "HEAR IT BACK (LOCAL TTS)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.aqua.deep,
                    )
                    Text(
                        "Spoken aloud on open for verification",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                }
            }
        }

        // RETAKE / SEND EQUAL TARGETS
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ConfirmTarget(Icons.Cross, "RETAKE", filled = false, modifier = Modifier.weight(1f), onClick = onRetake)
            ConfirmTarget(Icons.Tick, "TRANSMIT SOS", filled = true, modifier = Modifier.weight(1f), onClick = onSend)
        }
    }
}

@Composable
private fun ConfirmTarget(
    icon: ImageVector,
    label: String,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val p = palette
    Column(
        modifier
            .heightIn(min = 64.dp)
            .background(if (filled) p.blush.core else p.surfaceContainerLowest)
            .border(1.dp, if (filled) p.blush.deep else p.hairline)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = label },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (filled) Color.White else p.ink,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (filled) Color.White else p.ink,
        )
    }
}

// ── BOARD 14: INCOMING ALERT SCREEN ─────────────────────────────────────────

@Composable
fun IncomingAlertScreen(
    from: String,
    text: String,
    position: String? = null,
    repeatOf: Pair<Int, Int>? = null,
    evidence: String? = null,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Column(
        modifier
            .fillMaxSize()
            .background(AlertField)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(10.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .border(3.dp, AlertRule)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Beacon Icon & Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    val t = haloProgress()
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 1f - t),
                            radius = (size.minDimension / 2f) * (0.8f + 0.4f * t),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                    Icon(
                        Icons.Alert,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "DISTRESS ALERT",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        "INBOUND DISPATCH // ${from.uppercase()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            // Message Body Container
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(2.dp, AlertRule)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 24.sp,
                    color = p.ink,
                )
                if (position != null || evidence != null) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(p.sunken))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        position?.let {
                            Text(
                                "GPS: $it",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.ink,
                            )
                        }
                        evidence?.let {
                            Text(
                                it,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }
                    }
                }
            }

            // Acknowledge Action & Repeat Indicator
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (repeatOf != null) {
                    Text(
                        "SPEAKING // REPEAT ${repeatOf.first} OF ${repeatOf.second}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .background(Color.White)
                        .clickable(onClick = onAcknowledge)
                        .semantics(mergeDescendants = true) { contentDescription = "Acknowledge" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ACKNOWLEDGE ALERT",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = AlertField,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

private val AlertField = Tokens.AlertField
private val AlertRule = Color(0xFF9F1239)

// ── BOARD 22: ALERT SELF TEST ───────────────────────────────────────────────

data class AlertTestStep(
    val label: String,
    val detail: String? = null,
    val timing: String? = null,
    val state: State = State.PASSED,
) {
    enum class State { PASSED, RUNNING, FAILED }
}

@Composable
fun AlertSelfTestScreen(
    steps: List<AlertTestStep>,
    lastRun: String?,
    verdict: String?,
    device: String?,
    onRun: (() -> Unit)?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("ALERT SUBSYSTEM TEST", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Sounds on this handset only. Nothing is transmitted over RF mesh.",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
            )
            Text(
                lastRun?.let { "LAST RUN // $it" } ?: "NOT RUN ON THIS HANDSET",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.periwinkle.core,
            )

            if (steps.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(p.surfaceContainerLowest)
                        .border(Tokens.Hairline, p.hairline),
                ) {
                    steps.forEachIndexed { index, step ->
                        if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(p.sunken))
                        StepRow(step)
                    }
                }
            }

            if (verdict != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(p.surfaceContainerLowest)
                        .border(Tokens.Hairline, p.mint.core)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.AllClear, contentDescription = null, tint = p.mint.core, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            verdict,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.mint.deep,
                        )
                        device?.let {
                            Text(it, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                        }
                    }
                }
            }
        }

        // Run Test Action
        val armed = onRun != null
        Box(
            Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(12.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(if (armed) p.blush.core else p.surfaceContainerLow)
                    .clickable(enabled = armed) { onRun?.invoke() }
                    .then(if (armed) Modifier else Modifier.alpha(0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (armed) "EXECUTE TEST DISPATCH" else "NOT WIRED IN THIS BUILD",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (armed) Color.White else p.muted,
                )
            }
        }
    }
}

@Composable
private fun StepRow(step: AlertTestStep) {
    val p = palette
    val running = step.state == AlertTestStep.State.RUNNING
    val failed = step.state == AlertTestStep.State.FAILED
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (running) p.butter.tint else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            running -> Box(Modifier.size(16.dp).border(1.5.dp, p.butter.core))
            failed -> Icon(Icons.Cross, contentDescription = null, tint = p.blush.core, modifier = Modifier.size(16.dp))
            else -> Icon(Icons.Tick, contentDescription = null, tint = p.mint.core, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(step.label, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.ink)
            step.detail?.let {
                Text(it, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.muted)
            }
        }
        Text(
            step.timing ?: "—",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
        )
    }
}

// ── SHARED HEADERS & DOCKS ──────────────────────────────────────────────────

@Composable
internal fun BackHeader(
    title: String,
    onBack: () -> Unit,
) {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                .clickable(onClick = onBack)
                .semantics(mergeDescendants = true) { contentDescription = "Back" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Back, contentDescription = null, tint = p.ink, modifier = Modifier.size(20.dp))
        }
        Text(
            title,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = p.ink,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
internal fun DockShelf(
    hint: String,
    content: @Composable () -> Unit,
) {
    val p = palette
    Column(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            hint,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.muted,
        )
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            content()
        }
    }
}

@Composable
internal fun SquareFlank(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val p = palette
    Column(
        Modifier
            .sizeIn(minWidth = Tokens.DockFlank, minHeight = Tokens.DockFlank)
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .clickable(enabled = enabled, onClick = onClick)
            .then(if (enabled) Modifier else Modifier.alpha(0.4f))
            .semantics(mergeDescendants = true) { contentDescription = label },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = p.muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.muted)
    }
}
