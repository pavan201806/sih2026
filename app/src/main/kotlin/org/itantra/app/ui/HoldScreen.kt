package org.itantra.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tactical Hold Gate matching Stitch 01_splash_launch:
 * Restyles the hold-to-open gesture with high-contrast tactical styling,
 * zero-radius geometry, and accurate hold progress indicator.
 */
@Composable
fun HoldScreen(
    onOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    val reduced = reducedMotion
    val haptics = LocalHapticFeedback.current
    val opened = rememberUpdatedState(onOpened)

    var pressing by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }

    LaunchedEffect(pressing) {
        if (done) return@LaunchedEffect
        if (pressing) {
            val remaining = ((1f - progress.value) * HOLD_MILLIS).toInt().coerceAtLeast(1)
            progress.animateTo(1f, tween(remaining, easing = LinearEasing))
            if (progress.value >= 1f) {
                done = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (!reduced) burst.animateTo(1f, tween(BURST_MILLIS, easing = Ease.Standard))
                opened.value()
            }
        } else {
            progress.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
        }
    }

    val t = progress.value
    val lean = if (reduced) 1f else 1f + LEAN * t
    val ringColour = lerp(p.periwinkle.core, p.mint.core, t)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .graphicsLayer { alpha = 1f - burst.value }
            .padding(Tokens.ScreenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // TOP TELEMETRY STRIP
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
                Box(modifier = Modifier.size(6.dp).background(p.periwinkle.core))
                Text(
                    text = "SYS://ENGAGE_GATE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                    letterSpacing = 1.sp,
                )
            }
            Text(
                text = "LEVEL 4 AIR-GAP",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.mint.core,
            )
        }

        // CENTER TACTICAL ENGAGE MODULE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "RAKSHAVAANI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = p.ink,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "TACTICAL FIELD CONSOLE",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(40.dp))

            // Hold Ring & Center Interactive Button
            Box(Modifier.size(RING), contentAlignment = Alignment.Center) {
                // Outer Track
                Canvas(Modifier.size(RING)) {
                    val stroke = 2.dp.toPx()
                    drawCircle(
                        color = p.sunken,
                        style = Stroke(width = stroke),
                    )
                }

                // Dynamic Hold Progress
                Canvas(Modifier.size(RING)) {
                    val stroke = RING_STROKE.toPx()
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    if (t > 0f) {
                        drawArc(
                            color = ringColour,
                            startAngle = -90f,
                            sweepAngle = 360f * t,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Square),
                        )
                    }
                }

                // Interactive Center Control
                Box(
                    modifier = Modifier
                        .size(Tokens.TransmitCircle)
                        .graphicsLayer {
                            scaleX = lean
                            scaleY = lean
                        }
                        .background(if (done) p.mint.core else if (pressing) p.periwinkle.mid else p.periwinkle.core)
                        .border(2.dp, if (done) p.mint.core else p.onAccent, shape = Tokens.ZeroShape)
                        .pointerInput(done) {
                            if (done) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    pressing = true
                                    tryAwaitRelease()
                                    pressing = false
                                },
                            )
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Hold to engage console"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Transmit,
                            contentDescription = null,
                            tint = p.onAccent,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (pressing && !done) "ENGAGING" else "ENGAGE",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = p.onAccent,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = if (pressing && !done) "KEEP HOLDING" else "HOLD TO ENGAGE CONSOLE",
                fontSize = Tokens.Body,
                fontWeight = FontWeight.Bold,
                color = if (pressing) p.periwinkle.core else p.ink,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (pressing && !done) secondsLeft(t) else "3.0s secure hold lockout gate",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = p.muted,
                textAlign = TextAlign.Center,
            )
        }

        // BOTTOM STATUS FOOTER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FIELD MODE: ARMED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
                Text(
                    text = "OFFLINE LINK READY",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                )
            }
        }
    }
}

private fun secondsLeft(t: Float): String {
    val left = ((1f - t) * HOLD_MILLIS / 1000f)
    return String.format(java.util.Locale.ROOT, "%.1f s", left.coerceAtLeast(0f))
}

private const val HOLD_MILLIS = 3_000
private const val BURST_MILLIS = 380
private const val LEAN = 0.06f
private val RING = 180.dp
private val RING_STROKE = 4.dp
