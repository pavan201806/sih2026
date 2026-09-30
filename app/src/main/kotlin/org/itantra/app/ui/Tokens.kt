package org.itantra.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stitch Design Tokens for RakshaVaani Mil-Spec Interface.
 * Implements high-contrast tactical styling, zero-radius geometry, and crisp typography.
 */
object Tokens {
    // ── High Contrast Ground Plane ───────────────────────────────────────────

    val Ink = Color(0xFF111C29)          // on-surface (Midnight Navy)
    val Paper = Color(0xFFFFFFFF)        // surface-container-lowest
    val Muted = Color(0xFF3B4A47)        // on-surface-variant
    val Rule = Color(0xFFBACAC7)         // outline-variant
    val InkPaper = Color(0xFFFFFFFF)

    // ── State Signals ────────────────────────────────────────────────────────

    val Ok = Color(0xFF059669)           // Success / Field Green
    val Warn = Color(0xFFFFB547)         // Warning / Amber
    val Alert = Color(0xFFBA1A1A)        // Semantic SOS Beacon Red
    val AlertField = Color(0xFF93000A)   // High-intensity SOS container
    val Cyan = Color(0xFF19D3C5)         // Primary Tactical Active Cyan
    val Blue = Color(0xFF3FA9FF)         // Secondary Technical Blue

    // ── Layout Metrics ───────────────────────────────────────────────────────

    val Grid: Dp = 8.dp
    val ScreenMargin: Dp = 14.dp
    val TouchTarget: Dp = 64.dp
    val SecondaryAction: Dp = 72.dp
    val StatusBand: Dp = 56.dp
    val ModeBand: Dp = 48.dp
    val InstrumentBand: Dp = 40.dp

    const val TRANSMIT_FRACTION = 0.33f

    // ── Typography Scale ─────────────────────────────────────────────────────

    val Title: TextUnit = 22.sp          // headline-md
    val Body: TextUnit = 16.sp           // body-lg
    val Status: TextUnit = 14.sp         // label-lg
    val Instrument: TextUnit = 12.sp     // label-md (Monospace)
    val Icon: TextUnit = 32.sp

    const val INDIC_LINE_HEIGHT = 1.4f

    val Display: TextUnit = 44.sp        // headline-xl
    val Headline: TextUnit = 28.sp       // headline-lg
    val Figure: TextUnit = 22.sp
    val Subtitle: TextUnit = 19.sp       // headline-sm
    val Callout: TextUnit = 17.sp
    val BodySmall: TextUnit = 14.sp      // body-md
    val Label: TextUnit = 12.sp          // body-sm
    val Caption: TextUnit = 10.sp        // label-sm

    // ── Shape Metrics (Strict Zero/Tactical Radius) ──────────────────────────

    val RadiusPill: Dp = 2.dp
    val RadiusDock: Dp = 0.dp
    val RadiusCard: Dp = 0.dp
    val RadiusTile: Dp = 0.dp
    val RadiusControl: Dp = 2.dp
    val RadiusInset: Dp = 2.dp

    val ZeroShape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)

    val Hairline: Dp = 1.dp
    val SignalBorder: Dp = 1.5.dp
    val AlertBorder: Dp = 2.dp

    // ── Dock / PTT Array ─────────────────────────────────────────────────────

    val TransmitCircle: Dp = 144.dp
    val DockFlank: Dp = 64.dp

    // ── Motion Constants ─────────────────────────────────────────────────────

    const val HALO_MILLIS = 1_600
    const val EQ_MILLIS = 720
    const val EQ_STAGGER_MILLIS = 90
    const val EQ_BARS = 7
    const val PULSE_MILLIS = 2_000
    const val ARC_MILLIS = 1_400
    const val SHIMMER_MILLIS = 1_600
    const val BREATHE_MILLIS = 2_600
    const val TRANSITION_MILLIS = 250
}
