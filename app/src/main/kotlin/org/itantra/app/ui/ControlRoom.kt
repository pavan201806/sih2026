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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * ControlRoomScreen restyled to match Stitch 06_control_room:
 * Preserves 100% of existing state, data classes, navigation, and callbacks.
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
        BackHeader("Control Room", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            UnitHeroCard(operating, phone = state.defaultUnitName) { onOpen(Destination.UNIT_NAME) }

            RelayCard(
                on = state.relayMode,
                ttl = state.ttl,
                unitsHeard = operating.peerCount,
                onRelayMode = onRelayMode,
                onTtl = onTtl,
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusTile))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusTile)),
            ) {
                val rows = controlRoomRows(state, p)
                rows.forEachIndexed { index, row ->
                    if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(p.sunken))
                    DestinationRow(row) { onOpen(row.destination) }
                }
            }
        }

        if (unsecured) UnsecuredBar()
    }
}

/** One row of the control room: a coloured glyph, a name, and the answer. */
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
            Destination.MESSAGES,
            Icons.Replay,
            p.aqua.core,
            "Messages",
            "${operating.messages.size} · 24 h",
        ),
        ControlRow(
            Destination.LANGUAGE,
            Icons.Globe,
            p.orchid.core,
            "Language",
            operating.language,
            mono = false,
            valueInk = p.orchid.deep,
        ),
        ControlRow(
            Destination.METRICS,
            Icons.Chart,
            p.sky.core,
            "Metrics",
            operating.metrics.totalMillis?.let { "$it ms" } ?: "—",
        ),
        ControlRow(
            Destination.MODE,
            Icons.OpenLine,
            p.periwinkle.core,
            "Mode and radio",
            "${operating.mode} · ${operating.transportName}",
        ),
        ControlRow(
            Destination.STORAGE,
            Icons.Storage,
            p.butter.core,
            "Storage",
            if (storedBytes > 0) megabytes(storedBytes) else "—",
        ),
        ControlRow(
            Destination.MODEL_SETUP,
            Icons.Download,
            p.mint.core,
            "AI Model Setup",
            "Offline models",
            mono = false,
            valueInk = p.mint.deep,
        ),
        ControlRow(
            Destination.UNIT_NAME,
            Icons.Transmit,
            p.mint.core,
            "Device name",
            operating.unitName,
            mono = false,
            valueInk = p.mint.deep,
        ),
        ControlRow(
            Destination.TEXT_SIZE,
            Icons.Theme,
            p.muted,
            "Text size",
            "${Math.round(scale * 100)} %",
        ),
        ControlRow(
            Destination.LICENCES,
            Icons.Document,
            p.muted,
            "Licences",
            if (restrictive > 0) "$restrictive restrictive" else "all permissive",
            mono = false,
            valueInk = if (restrictive > 0) p.blush.deep else null,
        ),
    )
}

@Composable
private fun DestinationRow(
    row: ControlRow,
    onOpen: () -> Unit,
) {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.SecondaryAction)
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${row.label}, ${row.value}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(row.icon, contentDescription = null, tint = row.tint, modifier = Modifier.size(22.dp))
        Text(
            row.label,
            fontSize = Tokens.Body,
            fontWeight = FontWeight.Medium,
            color = p.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            row.value,
            fontSize = if (row.mono) Tokens.Label else Tokens.BodySmall,
            fontFamily = if (row.mono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (row.mono) FontWeight.Medium else FontWeight.SemiBold,
            lineHeight = Tokens.BodySmall * Tokens.INDIC_LINE_HEIGHT,
            color = row.valueInk ?: p.muted,
        )
        Icon(Icons.Forward, contentDescription = null, tint = p.hairlineStrong, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun UnitHeroCard(
    operating: OperatingState,
    phone: String,
    onEditName: () -> Unit,
) {
    val p = palette
    Column(
        Modifier
            .fillMaxWidth()
            .background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusCard))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onEditName)
                .semantics(mergeDescendants = true) {
                    contentDescription = "Device name ${operating.unitName}. Tap to change it."
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(Tokens.RadiusControl)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Transmit, contentDescription = null, tint = p.onAccent, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    operating.unitName,
                    fontSize = Tokens.Subtitle,
                    fontWeight = FontWeight.Bold,
                    color = p.onAccent,
                )
                if (phone.isNotBlank() && phone != operating.unitName) {
                    Text(
                        phone,
                        fontSize = Tokens.Instrument,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
            Text(
                "EDIT ›",
                fontSize = Tokens.Instrument,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.onAccent,
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HeroMeta("NODE", "%02d".format(operating.nodeId))
            HeroMeta("PEERS", "${operating.peerCount} ACTIVE")
            HeroMeta("ENCLAVE", "AIRGAP")
        }
    }
}

@Composable
private fun HeroMeta(label: String, value: String) {
    Column {
        Text(
            label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.72f),
        )
        Text(
            value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun RelayCard(
    on: Boolean,
    ttl: Int,
    unitsHeard: Int,
    onRelayMode: (Boolean) -> Unit,
    onTtl: (Int) -> Unit,
) {
    val p = palette
    val shape = RoundedCornerShape(Tokens.RadiusCard)
    Column(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest, shape)
            .border(Tokens.Hairline, p.hairline, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Background mesh relay",
                    fontSize = Tokens.Body,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    "Rebroadcasts traffic for other units with screen off",
                    fontSize = Tokens.Label,
                    color = p.muted,
                )
            }
            Box(
                Modifier
                    .clickable { onRelayMode(!on) }
                    .background(if (on) p.periwinkle.core else p.sunken, RoundedCornerShape(Tokens.RadiusControl))
                    .border(Tokens.Hairline, if (on) p.periwinkle.core else p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    if (on) "ON" else "OFF",
                    fontSize = Tokens.Label,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (on) p.onAccent else p.muted,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "Time to live (hops)",
                    fontSize = Tokens.Body,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    "Maximum relay hops for outgoing packets",
                    fontSize = Tokens.Label,
                    color = p.muted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 3, 5, 7).forEach { hops ->
                    Box(
                        Modifier
                            .clickable { onTtl(hops) }
                            .background(if (ttl == hops) p.periwinkle.core else p.surfaceContainerLow, RoundedCornerShape(Tokens.RadiusControl))
                            .border(Tokens.Hairline, if (ttl == hops) p.periwinkle.core else p.hairline, RoundedCornerShape(Tokens.RadiusControl))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "$hops",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (ttl == hops) p.onAccent else p.ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnsecuredBar() {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).background(p.mint.core, CircleShape))
            Text(
                "AIR-GAP ISOLATION PROTOCOL",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.ink,
            )
        }
        Text(
            "VERIFIED SECURE",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = p.mint.core,
        )
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    val p = palette
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Tokens.StatusBand)
            .background(p.surfaceContainerLowest)
            .border(Tokens.Hairline, p.hairline)
            .padding(horizontal = Tokens.ScreenMargin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() },
        ) {
            Box(
                Modifier
                    .heightIn(min = Tokens.TouchTarget)
                    .width(Tokens.TouchTarget),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("‹", fontSize = 32.sp, color = p.ink)
            }
            Text(
                title,
                fontSize = Tokens.Subtitle,
                fontWeight = FontWeight.Bold,
                color = p.ink,
            )
        }
    }
}

private fun megabytes(bytes: Long): String =
    String.format(Locale.ROOT, "%.1f MB", bytes / 1_048_576.0)
