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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tactical Splash Screen faithfully matching Stitch 01_splash_launch:
 * Mil-spec status breadcrumb strip, corner registration marks, tactical geometric crest,
 * distinctive wordmark with badge, technical boot diagnostic telemetry box, and footer.
 */
@Composable
fun SplashScreen(
    loading: String?,
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    val p = palette
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "blinkAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.surfaceContainerLowest)
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
                contentDescription =
                    loading?.let { "Starting. $it" } ?: "Starting. Loading speech models."
            },
    ) {
        // CORNER REGISTRATION MARKS (HUD PRAGMATISM)
        Text(
            text = "+ LAT: 00°00'00\"N",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 36.dp),
        )
        Text(
            text = "LON: 000°00'00\"E +",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 36.dp),
        )
        Text(
            text = "+ RF_MESH_CH: 04",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 36.dp),
        )
        Text(
            text = "HOST: RV-ARM64 +",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = p.hairlineStrong,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 36.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // TOP MIL-SPEC STATUS BREADCRUMB STRIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(6.dp).background(p.periwinkle.core))
                    Text(
                        text = "BOOT SEQUENCE // SECURE_ISOLATE",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = "|",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairline,
                    )
                    Text(
                        text = "AIRGAP: ENGAGED",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "FREQ: 868.10 MHz",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                    )
                    Text(
                        text = "//",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairline,
                    )
                    Text(
                        text = "RF-TX: ARMING",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )
                }
            }

            // MAIN CENTERPIECE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // TACTICAL GEOMETRIC CREST / EMBLEM
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Outer radar rings with pulse animation
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            },
                    ) {
                        drawCircle(
                            color = Color(0xFFBACAC7).copy(alpha = 0.4f),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                        drawCircle(
                            color = Color(0xFF00629F).copy(alpha = 0.25f),
                            radius = size.minDimension / 2.3f,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
                        )
                    }

                    // Quadrant corner bracket marks
                    Box(modifier = Modifier.matchParentSize()) {
                        Box(Modifier.align(Alignment.TopStart).size(8.dp).border(1.5.dp, p.periwinkle.core))
                        Box(Modifier.align(Alignment.TopEnd).size(8.dp).border(1.5.dp, p.periwinkle.core))
                        Box(Modifier.align(Alignment.BottomStart).size(8.dp).border(1.5.dp, p.periwinkle.core))
                        Box(Modifier.align(Alignment.BottomEnd).size(8.dp).border(1.5.dp, p.periwinkle.core))
                    }

                    // Center Shield / Wave Nodes Box
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .background(p.surfaceContainerLow)
                            .border(1.5.dp, p.periwinkle.core)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Background mechanical diagonal cross lines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = Color(0xFF19D3C5).copy(alpha = 0.15f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                            drawLine(
                                color = Color(0xFF19D3C5).copy(alpha = 0.15f),
                                start = Offset(size.width, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        // Top/Bottom Micro Ticks
                        Text(
                            text = "RF-ARMED",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = p.hairlineStrong,
                            modifier = Modifier.align(Alignment.TopStart),
                        )
                        Text(
                            text = "SYNC:99.8%",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )

                        // Center Icon & Title
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            // Node Grid Dots
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp),
                            ) {
                                Box(Modifier.size(3.dp).background(p.aqua.mid))
                                Box(Modifier.size(6.dp, 2.dp).background(p.periwinkle.core))
                                Box(Modifier.size(4.dp).background(p.periwinkle.core))
                                Box(Modifier.size(6.dp, 2.dp).background(p.periwinkle.core))
                                Box(Modifier.size(3.dp).background(p.aqua.mid))
                            }

                            Icon(
                                Icons.Transmit,
                                contentDescription = null,
                                tint = p.periwinkle.core,
                                modifier = Modifier.size(32.dp),
                            )

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "NODAL MESH CORE",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.periwinkle.core,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "SECURE AD-HOC P2P",
                                fontSize = 7.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.hairlineStrong,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // DISTINCTIVE WORDMARK & SUBTITLE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "RAKSHA",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = p.periwinkle.core,
                        letterSpacing = 3.sp,
                    )
                    Text(
                        text = "VAANI",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = p.aqua.core,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(p.periwinkle.core)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "// 4G-0",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.surfaceContainerLowest,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "OFFLINE EMERGENCY COMMUNICATION // ON-DEVICE PROTOCOL",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = p.muted,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("MIL-SPEC P2P", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
                    Text("•", fontSize = 8.sp, color = p.hairline)
                    Text("HARDENED STACK", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.aqua.core)
                    Text("•", fontSize = 8.sp, color = p.hairline)
                    Text("ZERO CELLULAR", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
                }

                Spacer(Modifier.height(18.dp))

                // TECHNICAL BOOT DIAGNOSTIC TELEMETRY BLOCK
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(p.surfaceContainerLow)
                        .border(Tokens.Hairline, p.hairline)
                        .padding(12.dp),
                ) {
                    // Top right chamfer badge
                    Text(
                        text = "CONSOLE_OUT // TTY0",
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.hairlineStrong,
                        modifier = Modifier.align(Alignment.TopEnd),
                    )

                    Column {
                        // Status headline with animated dot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .alpha(blinkAlpha)
                                    .background(p.periwinkle.core),
                            )
                            Text(
                                text = "STATUS: LOCAL SYSTEM INITIALIZING",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.periwinkle.core,
                                letterSpacing = 0.5.sp,
                            )
                        }

                        // Diagnostic log lines
                        DiagnosticLogLine(
                            status = "[OK]",
                            text = loading ?: "CORE KERNEL: SECURE MESH BOOT",
                            timing = "0.012s",
                        )
                        DiagnosticLogLine(
                            status = "[OK]",
                            text = "RADIO INTERFACES: WI-FI DIRECT / LORA / BLE",
                            timing = "0.048s",
                        )
                        DiagnosticLogLine(
                            status = "[OK]",
                            text = "ON-DEVICE VOSK & PIPER SPEECH PIPELINE",
                            timing = "0.119s",
                        )
                        DiagnosticLogLine(
                            status = "[OK]",
                            text = "0 EXTERNAL SERVERS REQUIRED (100% OFFLINE)",
                            timing = "VERIFIED",
                            highlight = true,
                        )

                        Spacer(Modifier.height(10.dp))

                        // Segmented buffer / loading progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "INITIALIZING BUFFER...",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                            val pct = progress?.let { "${(it * 100).toInt()}% READY" } ?: "100% READY"
                            Text(
                                text = "$pct [4096 / 4096 KB]",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.periwinkle.core,
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Segmented bar
                        val segments = 10
                        val activeSegments = progress?.let { (it * segments).toInt().coerceIn(1, segments) } ?: segments
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(p.surfaceContainerLowest)
                                .border(Tokens.Hairline, p.hairline)
                                .padding(1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            repeat(segments) { idx ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .background(if (idx < activeSegments) p.periwinkle.core else p.sunken),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // PROMPT ACTION TRIGGER / READY BADGE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(p.periwinkle.core)
                        .border(1.dp, p.periwinkle.mid)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Transmit,
                                contentDescription = null,
                                tint = p.surfaceContainerLowest,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "ENGAGE TACTICAL CONSOLE",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.surfaceContainerLowest,
                                letterSpacing = 1.sp,
                            )
                        }
                        Icon(
                            Icons.Forward,
                            contentDescription = null,
                            tint = p.surfaceContainerLowest,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "AUTO-ROUTING IN 3s",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                    Text(
                        text = "STANDBY READY █",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.aqua.core,
                    )
                }
            }

            // BOTTOM MIL-SPEC FOOTER TELEMETRY STRIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(5.dp).background(p.apricot.core))
                    Text(
                        text = "FIELD BUILD v3.4.1",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = p.ink,
                    )
                    Text(text = "//", fontSize = 8.sp, color = p.hairline)
                    Text(
                        text = "NODE ID: RV-NODE-01",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "NO CLOUD DEPENDENCY",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.aqua.core,
                    )
                    Text(text = "|", fontSize = 8.sp, color = p.hairline)
                    Text(
                        text = "ENCR: AES-GCM-256",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticLogLine(
    status: String,
    text: String,
    timing: String,
    highlight: Boolean = false,
) {
    val p = palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (highlight) {
                    Modifier
                        .background(p.periwinkle.tint.copy(alpha = 0.35f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier.padding(vertical = 1.5.dp)
                },
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Text(
                text = status,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.periwinkle.core,
            )
            Text(
                text = text,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = p.ink,
                maxLines = 1,
            )
        }
        Text(
            text = timing,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            color = if (highlight) p.periwinkle.core else p.hairlineStrong,
        )
    }
}
