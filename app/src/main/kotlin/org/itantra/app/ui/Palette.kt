package org.itantra.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Stitch-aligned Tactical Design Palette for RakshaVaani.
 * Sourced directly from ui-reference design tokens:
 * - Primary Electric Cyan (#006A63, #19D3C5)
 * - Secondary Technical Blue (#00629F, #3FA9FF)
 * - Tertiary Tactical Amber (#835500, #FFB547)
 * - SOS Distress Red (#BA1A1A, #FF4D5A, #FFDAD6)
 * - High-Contrast Technical Ground (#F8F9FF, #111C29)
 */
@Immutable
data class ItantraPalette(
    val fieldMode: Boolean,
    val paper: Color,
    val ground: Color,
    val sunken: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val ink: Color,
    val muted: Color,
    val onAccent: Color,
    val periwinkle: Family,
    val aqua: Family,
    val sky: Family,
    val blush: Family,
    val orchid: Family,
    val mint: Family,
    val apricot: Family,
    val butter: Family,
    val fuchsia: Family,
) {
    @Immutable
    data class Family(
        val tint: Color,
        val mid: Color,
        val track: Color,
        val core: Color,
        val deep: Color,
    )

    companion object {
        val Spectrum =
            ItantraPalette(
                fieldMode = false,
                paper = Color(0xFFFFFFFF),
                ground = Color(0xFFF8F9FF),
                sunken = Color(0xFFEEF4FF),
                hairline = Color(0xFFBACAC7),
                hairlineStrong = Color(0xFF6B7A77),
                ink = Color(0xFF111C29),
                muted = Color(0xFF3B4A47),
                onAccent = Color(0xFFFFFFFF),
                // Identity / Transmit / Primary Tactical Cyan
                periwinkle =
                    Family(
                        tint = Color(0xFFD0FAF5),
                        mid = Color(0xFF19D3C5),
                        track = Color(0xFFBCEEE8),
                        core = Color(0xFF006A63),
                        deep = Color(0xFF003C64),
                    ),
                // Link / Live Carrier
                aqua =
                    Family(
                        tint = Color(0xFFCFE4FF),
                        mid = Color(0xFF3FA9FF),
                        track = Color(0xFFB8DAFF),
                        core = Color(0xFF00629F),
                        deep = Color(0xFF001D34),
                    ),
                // Measurement / Technical Telemetry
                sky =
                    Family(
                        tint = Color(0xFFDDE9FB),
                        mid = Color(0xFF66B3FF),
                        track = Color(0xFFC7DCF8),
                        core = Color(0xFF00629F),
                        deep = Color(0xFF002F54),
                    ),
                // Emergency SOS / Beacon Red
                blush =
                    Family(
                        tint = Color(0xFFFFDAD6),
                        mid = Color(0xFFFF4D5A),
                        track = Color(0xFFFFB4AB),
                        core = Color(0xFFBA1A1A),
                        deep = Color(0xFF93000A),
                    ),
                // Navigation / Preference / Control
                orchid =
                    Family(
                        tint = Color(0xFFE4EFFF),
                        mid = Color(0xFF7BAAF7),
                        track = Color(0xFFD2E3FC),
                        core = Color(0xFF1A73E8),
                        deep = Color(0xFF0D47A1),
                    ),
                // Success / Nominal / All-Clear
                mint =
                    Family(
                        tint = Color(0xFFD1FADF),
                        mid = Color(0xFF34D399),
                        track = Color(0xFFA7F3D0),
                        core = Color(0xFF059669),
                        deep = Color(0xFF065F46),
                    ),
                // Recoverable Warning / Amber
                apricot =
                    Family(
                        tint = Color(0xFFFFDDB4),
                        mid = Color(0xFFFFB547),
                        track = Color(0xFFFFE7CC),
                        core = Color(0xFF835500),
                        deep = Color(0xFF633F00),
                    ),
                // Held / Queued Sends
                butter =
                    Family(
                        tint = Color(0xFFFEF3C7),
                        mid = Color(0xFFFBBF24),
                        track = Color(0xFFFDE68A),
                        core = Color(0xFFD97706),
                        deep = Color(0xFF92400E),
                    ),
                // Template Translation Tag
                fuchsia =
                    Family(
                        tint = Color(0xFFEDE9FE),
                        mid = Color(0xFFA78BFA),
                        track = Color(0xFFDDD6FE),
                        core = Color(0xFF7C3AED),
                        deep = Color(0xFF5B21B6),
                    ),
            )

        val Field =
            ItantraPalette(
                fieldMode = true,
                paper = Color(0xFFFFFFFF),
                ground = Color(0xFFF8F9FF),
                sunken = Color(0xFFE5E7EB),
                hairline = Color(0xFFBACAC7),
                hairlineStrong = Color(0xFF111C29),
                ink = Color(0xFF111C29),
                muted = Color(0xFF3B4A47),
                onAccent = Color(0xFFFFFFFF),
                periwinkle =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
                aqua =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
                sky =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
                blush =
                    Family(
                        tint = Color(0xFFFFDAD6),
                        mid = Color(0xFFFF4D5A),
                        track = Color(0xFFFFB4AB),
                        core = Color(0xFFBA1A1A),
                        deep = Color(0xFF93000A),
                    ),
                orchid =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
                mint =
                    Family(
                        tint = Color(0xFFD1FADF),
                        mid = Color(0xFF34D399),
                        track = Color(0xFFA7F3D0),
                        core = Color(0xFF059669),
                        deep = Color(0xFF065F46),
                    ),
                apricot =
                    Family(
                        tint = Color(0xFFFFDDB4),
                        mid = Color(0xFFFFB547),
                        track = Color(0xFFFFE7CC),
                        core = Color(0xFF835500),
                        deep = Color(0xFF633F00),
                    ),
                butter =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
                fuchsia =
                    Family(
                        tint = Color(0xFFE5E7EB),
                        mid = Color(0xFF9CA3AF),
                        track = Color(0xFFD1D5DB),
                        core = Color(0xFF111C29),
                        deep = Color(0xFF111C29),
                    ),
            )
    }
}

val LocalPalette = staticCompositionLocalOf { ItantraPalette.Spectrum }

@Composable
fun ItantraTheme(
    fieldMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPalette provides if (fieldMode) ItantraPalette.Field else ItantraPalette.Spectrum,
    ) {
        content()
    }
}

val palette: ItantraPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalPalette.current

val ItantraPalette.surfaceContainerLowest: Color get() = paper
val ItantraPalette.surfaceContainerLow: Color get() = sunken
val ItantraPalette.surfaceContainer: Color get() = ground
val ItantraPalette.surfaceContainerHigh: Color get() = sunken
val ItantraPalette.surfaceContainerHighest: Color get() = hairline
