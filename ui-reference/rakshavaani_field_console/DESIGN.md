---
name: RakshaVaani Field Console
colors:
  surface: '#f8f9ff'
  surface-dim: '#cfdbec'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eef4ff'
  surface-container: '#e4efff'
  surface-container-high: '#dde9fb'
  surface-container-highest: '#d7e3f5'
  on-surface: '#111c29'
  on-surface-variant: '#3b4a47'
  inverse-surface: '#26313e'
  inverse-on-surface: '#e9f1ff'
  outline: '#6b7a77'
  outline-variant: '#bacac7'
  surface-tint: '#006a63'
  primary: '#006a63'
  on-primary: '#ffffff'
  primary-container: '#19d3c5'
  on-primary-container: '#005650'
  inverse-primary: '#30ddce'
  secondary: '#00629f'
  on-secondary: '#ffffff'
  secondary-container: '#3fa9ff'
  on-secondary-container: '#003c64'
  tertiary: '#835500'
  on-tertiary: '#ffffff'
  tertiary-container: '#f8af41'
  on-tertiary-container: '#6b4400'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#5afaeb'
  primary-fixed-dim: '#30ddce'
  on-primary-fixed: '#00201d'
  on-primary-fixed-variant: '#00504a'
  secondary-fixed: '#cfe4ff'
  secondary-fixed-dim: '#9acbff'
  on-secondary-fixed: '#001d34'
  on-secondary-fixed-variant: '#004a79'
  tertiary-fixed: '#ffddb4'
  tertiary-fixed-dim: '#ffb955'
  on-tertiary-fixed: '#291800'
  on-tertiary-fixed-variant: '#633f00'
  background: '#f8f9ff'
  on-background: '#111c29'
  surface-variant: '#d7e3f5'
typography:
  headline-xl:
    fontFamily: Barlow Condensed
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: 0.05em
  headline-xl-mobile:
    fontFamily: Barlow Condensed
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: 0.05em
  headline-lg:
    fontFamily: Barlow Condensed
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: 0.04em
  headline-md:
    fontFamily: Barlow Condensed
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: 0.03em
  headline-sm:
    fontFamily: Barlow Condensed
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0.02em
  body-lg:
    fontFamily: JetBrains Mono
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: -0.01em
  body-md:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  body-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.08em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.1em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 9px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: 0.12em
spacing:
  gutter: 0.5rem
  gutter-tablet: 0.75rem
  gutter-desktop: 1rem
  margin: 0.75rem
  margin-tablet: 1rem
  margin-desktop: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system establishes a mission-critical tactical interface built for extreme, low-connectivity, and hostile operating environments. Drawing from mil-spec avionics, heads-up terminal displays, and rugged edge field consoles, the visual identity prioritizes immediate legibility, high outdoor contrast, and absolute data density.

The style rejects decorative fluff in favor of strict, engineered pragmatism:
- **Tactical Real-Time Diagnostics:** Monospaced data readouts, edge telemetry indicators, and strict status labeling establish confidence during emergency command and field operations.
- **Instrument-Grade Angularity:** Strict zero-radius geometry, sharp hairline demarcations, and mechanical chamfer accents replace soft consumer curves.
- **High-Alert Hierarchy:** The canvas relies on a clean tactical light base punctuated by electric cyan for active signals, amber for degradation warnings, and an uncompromised high-intensity crimson reserved strictly for SOS events.

## Colors

The palette is engineered for high outdoor sunlight readability and crisp light-mode clarity. Color is treated as functional state rather than decoration.

- **Primary (`#19D3C5` Electric Cyan):** Active mesh pings, primary telemetry links, live transmitting states, and tactical interactive controls.
- **Secondary (`#3FA9FF` Technical Blue):** Peripheral system data, peer node identifiers, secondary operational layers, and static metrics.
- **Tertiary (`#FFB547` Amber Warning):** Degraded link warnings, battery depletion notices, packet retries, and environmental cautions.
- **Neutral Dark (`#07131F` Deep Midnight Navy):** High-contrast structural elements, with structured light panels utilized for elevated container surfaces.
- **Semantic SOS (`#FF4D5A` Beacon Red):** Strict semantic isolation. Reserved solely for life-safety triggers, active distress broadcasts, and unrecoverable link severance.
- **Success (`#32D583` Field Green):** Verified node handshakes, secured channel locks, and successful packet receipts.

## Typography

Typography functions as an analytical readout. All tactical body copy, metadata, timestamps, and parameters utilize `JetBrains Mono` to guarantee tabular number alignment, character differentiation (such as `0` vs `O` and `1` vs `l`), and predictable line wrapping.

`Barlow Condensed` serves as the high-impact operational display face. Set in uppercase or heavy small-caps with tracked letter spacing, it maximizes horizontal space efficiency on dense Android handheld screens, allowing long emergency sector identifiers and tactical coordinates to render without truncated strings. All labels (`label-lg`, `label-md`, `label-sm`) default to uppercase presentation.

## Layout & Spacing

The layout is built upon an ultra-dense, 4px-aligned modular technical grid designed for rapid tactical scanning. Rather than generous breathing room, space is tightly bounded to allow critical multi-channel readouts, RF link states, and emergency telemetry to coexist above the fold.

- **Handheld Mobile (Portrait, <600px):** Single-column stacked telemetry layout. 12px outer canvas margins maximize horizontal screen real estate for multi-column hex and status logs.
- **Tablet & Field Slates (600px - 1024px):** 2-column asymmetric split. Left column locks to real-time RF network maps and SOS queue; right column dedicates to active peer messaging and AI edge diagnostic feeds.
- **Docked Command Station (>1024px):** 3-column fixed HUD layout with continuous top and bottom telemetry bands.

## Elevation & Depth

This design system avoids soft organic drop shadows, which fail under direct sunlight and degrade performance on low-power hardware. Depth is produced via rigid tonal stepping, hairline borders, and neon-inspired signal backlights:

- **Base Field:** Light foundational ground plane representing inactive display area.
- **Level 1 Container:** Standard panel surface with a solid 1px structural border.
- **Level 2 Interactive/Focus:** Active card, modal console, or selected radio item, bounded by a high-contrast active border.
- **HUD Projection / Active Signal:** Highlighted items leverage a tight, non-diffuse outline glow.
- **SOS Critical State:** High-alert surfaces flash an un-blurred 2px inner keyline of `#FF4D5A`.

## Shapes

The interface implements a strict `roundedness: 0` policy. All containers, buttons, indicators, inputs, and flyouts feature precise, 90-degree right angles or 45-degree angled chamfers (cut corners).

Chamfer details (cut corners of 4px to 6px) are created via CSS `clip-path` on primary interactive triggers and top-level HUD frames. This mechanical geometry reinforces the mil-spec tactical nature of the device and completely eliminates consumer rounded-card visual metaphors.

## Components

### Buttons & Tactical Triggers
- **Primary Action (Active Transmit):** Background `#19D3C5`, text `#07131F`, bold `Barlow Condensed` uppercase. 0px border radius with optional 6px diagonal cut-corner on the top-right. Active state inverts text to white with high-contrast border.
- **Secondary Action (Telemetry Query):** Background transparent, 1px border `#3FA9FF`, text `#3FA9FF`.
- **Distress SOS Trigger:** High-priority, oversized button. Background `#FF4D5A`, text `#FFFFFF`. Features a permanent high-visibility striped perimeter border pattern and physical hold-to-confirm telemetry countdown.

### Status Chips & Badges
- Strict monospaced text indicators framed in 1px solid borders.
- Preceded by a 6px square glyph indicator: glowing solid `#19D3C5` for P2P mesh online, pulsing `#FFB547` for packet hopping, and blinking `#FF4D5A` for unlinked distress state.

### Telemetry Strips & Edge Panels
- Screen headers and footers act as continuous instrumentation strips showing battery voltage, mesh hop count, GPS coordinates, and frequency band.
- Panels feature hairline dividers and corner registration tick marks (`+` and corner brackets) to frame raw incoming data streams.

### Input Fields & Terminal Console
- Text entry elements use an inset surface with a 1px structural border. On focus, border snaps to `#19D3C5` with a monospaced block cursor. Labels sit flush above the field in uppercase `label-sm` technical typography.

### Checkboxes & Segmented Switches
- Binary switches are presented as rectangular toggle cells with crisp divider lines. Checked states are represented by filled geometric squares rather than rounded checkmarks.