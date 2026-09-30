package org.itantra.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
 * Tactical Splash Screen matching Stitch 01_splash_launch:
 * Zero-radius layout geometry, mil-spec telemetry header, tactical geometric branding,
 * and real loading progress feedback.
 */
@Composable
fun SplashScreen(
    loading: String?,
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    val p = palette

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.ground)
            .padding(Tokens.ScreenMargin)
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
                contentDescription =
                    loading?.let { "Starting. $it" } ?: "Starting. Loading speech models."
            },
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
                    text = "SYS://BOOT_INIT",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                    letterSpacing = 1.sp,
                )
            }
            Text(
                text = "AIRGAP SECURE",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.mint.core,
            )
        }

        // CENTER BRANDING & PROGRESS BLOCK
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Tactical Geometric Hex/Square Crest
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(p.surfaceContainerLowest)
                    .border(2.dp, p.periwinkle.core, shape = Tokens.ZeroShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(p.periwinkle.tint)
                        .border(1.5.dp, p.periwinkle.mid, shape = Tokens.ZeroShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(p.periwinkle.core),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "RAKSHAVAANI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = p.ink,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "OFFLINE SPEECH & MESH COMMUNICATION SYSTEM",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(32.dp))

            // Loading Progress Bar
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
                            text = loading ?: "Initializing local speech runtime",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.ink,
                        )
                        progress?.let {
                            Text(
                                text = "${(it * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.periwinkle.core,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(p.sunken)
                            .border(Tokens.Hairline, p.hairline),
                    ) {
                        val frac = progress?.coerceIn(0f, 1f) ?: 1f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(frac)
                                .height(6.dp)
                                .background(p.periwinkle.core),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Three Key System Figures
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricCard(title = "52 B", label = "Per Sentence", modifier = Modifier.weight(1f))
                MetricCard(title = "10", label = "Languages", modifier = Modifier.weight(1f))
                MetricCard(title = "1600×", label = "Compression", modifier = Modifier.weight(1f))
            }
        }

        // BOTTOM SUBSYSTEM STATUS
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
                    text = "ON-DEVICE ASR + TTS",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
                Text(
                    text = "ZERO INTERNET REQUIRED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.periwinkle.core,
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Box(
        modifier = modifier
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = p.periwinkle.core,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
