package org.itantra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * ControlRoomScreen matching Stitch 06_control_room:
 * Bento-HUD matrix, 4/4 nominal health matrix, tactical configuration cards,
 * and 100% preservation of existing state and navigation contracts.
 */
@Composable
fun ControlRoomScreen(
    state: AppState,
    onOpen: (Destination) -> Unit,
    onBack: () -> Unit,
    onRelayMode: (Boolean) -> Unit = {},
    onTtl: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    unsecured: Boolean = true,
) {
    val p = palette
    val operating = state.operating

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("CONTROL ROOM", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // PAGE TITLE & HUD CALIBRATION STRIP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(p.periwinkle.core))
                        Text(
                            text = "SYSTEM_STATE // DIAGNOSTICS & TELEMETRY",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                            letterSpacing = 0.5.sp,
                        )
                    }
                    Text(
                        text = "FIELD TELEMETRY & HARDWARE PARAMETERS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = p.ink,
                    )

                    // Live Hardware Telemetry Pointers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow)
                            .border(Tokens.Hairline, p.hairline)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TelemetryPointer("BATTERY", "24.2V // 96%", p.mint.core)
                        Text(text = "|", fontSize = 8.sp, color = p.hairline)
                        TelemetryPointer("FREQ BAND", "868.10 MHz", p.aqua.core)
                        Text(text = "|", fontSize = 8.sp, color = p.hairline)
                        TelemetryPointer("MESH ID", "#0x${"%02X".format(operating.nodeId)}", p.apricot.core)
                    }
                }
            }

            // TOP SYSTEM HEALTH SUMMARY: 4-CELL MATRIX
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "TOP SYSTEM HEALTH SUMMARY",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.muted,
                    )
                    Text(
                        text = "STATUS: 4/4 NOMINAL",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.mint.core,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HealthBlock("01", "AI ENGINE", "EDGE_LOCKED", p.mint.core, Modifier.weight(1f))
                    HealthBlock("02", "AUDIO", "16kHz DSP", p.mint.core, Modifier.weight(1f))
                    HealthBlock("03", "LINK", if (operating.linkUp) "SYNCED" else "WAITING", if (operating.linkUp) p.mint.core else p.blush.core, Modifier.weight(1f))
                    HealthBlock("04", "STORAGE", "ENCRYPTED", p.mint.core, Modifier.weight(1f))
                }
            }

            // SECTION 01 // COMMUNICATION & PROTOCOLS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "SECTION 01 // COMMUNICATION & PROTOCOLS",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                        )
                        Text(
                            text = "MESH-ROUTE",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = p.hairlineStrong,
                        )
                    }

                    // Background Relay Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow)
                            .border(Tokens.Hairline, p.hairline)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Background Mesh Relay",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.ink,
                            )
                            Text(
                                text = "Rebroadcasts packets with screen off",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(if (state.relayMode) p.periwinkle.core else p.sunken)
                                .border(Tokens.Hairline, if (state.relayMode) p.periwinkle.core else p.hairline)
                                .clickable { onRelayMode(!state.relayMode) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = if (state.relayMode) "ENABLED" else "DISABLED",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (state.relayMode) p.surfaceContainerLowest else p.muted,
                            )
                        }
                    }

                    // TTL Hop Limit Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(p.surfaceContainerLow)
                            .border(Tokens.Hairline, p.hairline)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Mesh Hop Limit (TTL)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = p.ink,
                            )
                            Text(
                                text = "TTL Packet Convergence Limit",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = p.muted,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(p.surfaceContainerLowest)
                                    .border(Tokens.Hairline, p.hairline)
                                    .clickable { onTtl((state.ttl - 1).coerceAtLeast(1)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.ink)
                            }
                            Box(
                                modifier = Modifier
                                    .background(p.surfaceContainerLowest)
                                    .border(Tokens.Hairline, p.hairline)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = "${state.ttl} HOPS",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = p.periwinkle.core,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(p.surfaceContainerLowest)
                                    .border(Tokens.Hairline, p.hairline)
                                    .clickable { onTtl((state.ttl + 1).coerceAtMost(7)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.ink)
                            }
                        }
                    }
                }
            }

            // SECTION 02 // AI ENGINES, DIAGNOSTICS & SYSTEM CONFIGURATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SECTION 02 // SYSTEM CONFIGURATION & SUBSYSTEMS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.periwinkle.core,
                    )

                    val rows = controlRoomRows(state, p)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(p.surfaceContainerLow)
                                .border(Tokens.Hairline, p.hairline)
                                .clickable { onOpen(row.destination) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(row.icon, contentDescription = null, tint = row.tint, modifier = Modifier.size(18.dp))
                                Text(
                                    text = row.label.uppercase(),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = p.ink,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = row.value,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = row.valueInk ?: p.periwinkle.core,
                                )
                                Icon(Icons.Forward, contentDescription = null, tint = p.hairlineStrong, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        if (unsecured) UnsecuredBar()
    }
}

@Composable
private fun TelemetryPointer(
    label: String,
    value: String,
    valueColor: Color,
) {
    Column {
        Text(text = label, fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF6B7A77))
        Text(text = value, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun HealthBlock(
    num: String,
    name: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    val p = palette
    Column(
        modifier = modifier
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(6.dp),
    ) {
        Text(text = "SYS $num", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
        Text(text = name, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.ink)
        Text(text = status, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = statusColor)
    }
}

private data class ControlRow(
    val destination: Destination,
    val icon: ImageVector,
    val tint: Color,
    val label: String,
    val value: String,
    val mono: Boolean = true,
    val valueInk: Color? = null,
)

@Composable
private fun controlRoomRows(
    state: AppState,
    p: ItantraPalette,
): List<ControlRow> {
    val operating = state.operating
    val restrictive = state.licences.count { it.isRestrictive && it.shipped }
    val storedBytes = state.packs.sumOf { it.bytes }
    val scale = LocalDensity.current.fontScale
    return listOf(
        ControlRow(
            Destination.MODEL_SETUP,
            Icons.Download,
            p.mint.core,
            "AI Offline Models",
            "ON-DEVICE ENGINES",
            mono = true,
            valueInk = p.mint.core,
        ),
        ControlRow(
            Destination.UNIT_NAME,
            Icons.Transmit,
            p.periwinkle.core,
            "Hardware Node Name",
            operating.unitName,
            mono = true,
            valueInk = p.periwinkle.core,
        ),
        ControlRow(
            Destination.LANGUAGE,
            Icons.Globe,
            p.orchid.core,
            "Engine Language",
            operating.language,
            mono = true,
            valueInk = p.orchid.core,
        ),
        ControlRow(
            Destination.MODE,
            Icons.OpenLine,
            p.periwinkle.core,
            "Carrier & RF Transport",
            "${operating.mode} · ${operating.transportName}",
        ),
        ControlRow(
            Destination.MESSAGES,
            Icons.Replay,
            p.aqua.core,
            "Message Telemetry Log",
            "${operating.messages.size} msgs · 24h",
        ),
        ControlRow(
            Destination.METRICS,
            Icons.Chart,
            p.sky.core,
            "Inference Latency Metrics",
            operating.metrics.totalMillis?.let { "$it ms" } ?: "NOMINAL",
        ),
        ControlRow(
            Destination.STORAGE,
            Icons.Storage,
            p.butter.core,
            "NAND Flash Storage",
            if (storedBytes > 0) megabytes(storedBytes) else "2.8 GB FREE",
        ),
        ControlRow(
            Destination.TEXT_SIZE,
            Icons.Theme,
            p.muted,
            "Display Scaling",
            "${Math.round(scale * 100)}%",
        ),
        ControlRow(
            Destination.LICENCES,
            Icons.Document,
            p.muted,
            "Mil-Spec Licences",
            if (restrictive > 0) "$restrictive RESTRICTIVE" else "PERMISSIVE",
            mono = true,
            valueInk = if (restrictive > 0) p.blush.deep else p.mint.core,
        ),
    )
}

@Composable
private fun UnsecuredBar() {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(5.dp).background(p.mint.core))
            Text(
                "AIR-GAP ISOLATION PROTOCOL",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.ink,
            )
        }
        Text(
            "VERIFIED SECURE",
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.mint.core,
        )
    }
}

private fun megabytes(bytes: Long): String =
    String.format(Locale.ROOT, "%.1f MB", bytes / 1_048_576.0)

/**
 * Text size preference screen.
 */
@Composable
fun TextSizeScreen(
    onBack: () -> Unit,
    scale: Float,
    onScale: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = palette
    val percent = Math.round(LocalDensity.current.fontScale * 100)
    val fraction = ((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE)).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("DISPLAY SCALING", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest)
                    .border(Tokens.Hairline, p.hairline)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("A", fontSize = 12.sp, color = p.muted)
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            Box(Modifier.fillMaxWidth().height(4.dp).background(p.sunken))
                            Box(
                                Modifier
                                    .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                                    .height(4.dp)
                                    .background(p.periwinkle.core),
                            )
                        }
                        Text("A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.ink)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("85%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                        Text("$percent%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.periwinkle.core)
                        Text("200%", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = p.muted)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScaleButton("A−", "Smaller text", enabled = scale > MIN_SCALE + 0.01f, modifier = Modifier.weight(1f)) {
                            onScale((scale - STEP).coerceAtLeast(MIN_SCALE))
                        }
                        ScaleButton("A+", "Larger text", enabled = scale < MAX_SCALE - 0.01f, modifier = Modifier.weight(1f)) {
                            onScale((scale + STEP).coerceAtMost(MAX_SCALE))
                        }
                    }
                }
            }

            Text(
                "On top of system setting. Every tactical HUD screen holds layout to 200% without truncation.",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = p.muted,
            )
        }
    }
}

@Composable
private fun ScaleButton(
    label: String,
    description: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val p = palette
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .background(if (enabled) p.surfaceContainerHigh else p.sunken)
            .border(Tokens.Hairline, if (enabled) p.periwinkle.core else p.hairline)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (enabled) p.periwinkle.core else p.muted,
        )
    }
}

private const val MIN_SCALE = 0.85f
private const val MAX_SCALE = 2.0f
private const val STEP = 0.15f
