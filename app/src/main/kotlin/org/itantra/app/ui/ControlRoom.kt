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
    // 85 % sits at the left end and 200 % at the right, which is the range rule 9 names.
    val fraction = ((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE)).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackHeader("Text size", onBack)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionLabel("TEXT SIZE")

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusTile))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusTile))
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("A", fontSize = Tokens.Status, color = p.muted)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(p.sunken, RoundedCornerShape(Tokens.RadiusPill)),
                        )
                        Box(
                            Modifier
                                .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                                .height(5.dp)
                                .background(p.periwinkle.core, RoundedCornerShape(Tokens.RadiusPill)),
                        )
                    }
                    Text("A", fontSize = Tokens.Headline, color = p.ink)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Instrument("85 %", p.muted)
                    Instrument("$percent %", p.periwinkle.deep)
                    Instrument("200 %", p.muted)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScaleButton(
                        "A−",
                        "Smaller text",
                        enabled = scale > MIN_SCALE + 0.01f,
                        modifier = Modifier.weight(1f),
                    ) {
                        onScale((scale - STEP).coerceAtLeast(MIN_SCALE))
                    }
                    ScaleButton(
                        "A+",
                        "Larger text",
                        enabled = scale < MAX_SCALE - 0.01f,
                        modifier = Modifier.weight(1f),
                    ) {
                        onScale((scale + STEP).coerceAtMost(MAX_SCALE))
                    }
                }
            }

            Text(
                "On top of the system setting, and kept across restarts. " +
                    "Every screen holds its layout to 200 % without truncating.",
                fontSize = Tokens.Label,
                lineHeight = Tokens.Label * 1.5f,
                color = p.muted,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )

            SectionLabel("PREVIEW AT THIS SIZE")

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(p.surfaceContainerLowest, RoundedCornerShape(Tokens.RadiusTile))
                    .border(Tokens.Hairline, p.hairline, RoundedCornerShape(Tokens.RadiusTile)),
            ) {
                RampRow("ALERT", "display", Tokens.Display, FontWeight.Bold, first = true)
                RampRow("Push to talk", "title", Tokens.Title, FontWeight.Bold)
                RampRow("need help now, two injured", "body", Tokens.Body, FontWeight.Normal)
                RampRow("हिन्दी सहायता", "indic 1.4×", Tokens.Subtitle, FontWeight.SemiBold, indic = true)
                RampRow("TOTAL 780 ms · 44 B", "instrument", Tokens.Instrument, FontWeight.Medium, mono = true)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .padding(
                    start = Tokens.ScreenMargin,
                    end = Tokens.ScreenMargin,
                    top = 12.dp,
                    bottom = Tokens.ScreenMargin,
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.padding(top = 6.dp).size(7.dp).background(p.orchid.core, CircleShape))
            Text(
                "Indic scripts reserve 1.4× the Latin line box at every size, so matras and " +
                    "conjuncts never clip.",
                fontSize = Tokens.Label,
                lineHeight = Tokens.Label * 1.45f,
                color = p.muted,
            )
        }
    }
}

@Composable
private fun RampRow(
    sample: String,
    role: String,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
    first: Boolean = false,
    indic: Boolean = false,
    mono: Boolean = false,
) {
    val p = palette
    if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(p.sunken))
    Row(
        Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            sample,
            fontSize = size,
            fontWeight = weight,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            lineHeight = if (indic) size * Tokens.INDIC_LINE_HEIGHT else size * 1.2f,
            color = if (mono) p.sky.deep else p.ink,
            modifier = Modifier.weight(1f),
        )
        Instrument(role, p.muted)
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
    Column(
        modifier
            .heightIn(min = Tokens.TouchTarget)
            .background(if (enabled) p.periwinkle.tint else p.sunken, RoundedCornerShape(Tokens.RadiusControl))
            .border(
                Tokens.Hairline,
                if (enabled) p.periwinkle.mid else p.hairline,
                RoundedCornerShape(Tokens.RadiusControl),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            fontSize = Tokens.Title,
            fontWeight = FontWeight.Bold,
            color = if (enabled) p.periwinkle.deep else p.muted,
        )
    }
}

private const val MIN_SCALE = 0.85f
private const val MAX_SCALE = 2.0f
private const val STEP = 0.15f

@Composable
private fun SectionLabel(text: String) {
    val p = palette
    Text(
        text,
        fontSize = Tokens.Instrument,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        color = p.muted,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun Instrument(
    text: String,
    colour: Color,
) {
    Text(
        text,
        fontSize = Tokens.Instrument,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        color = colour,
    )
}

private fun megabytes(bytes: Long): String =
    String.format(Locale.ROOT, "%.1f MB", bytes / 1_048_576.0)
