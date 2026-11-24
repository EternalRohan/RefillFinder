package com.refillfinder.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Primary Blue Palette ──────────────────────────────────────────
val PrimaryBlue       = Color(0xFF1565C0)   // Deep blue (primary)
val PrimaryBlueMid    = Color(0xFF1E88E5)   // Action blue
val PrimaryBlueDark   = Color(0xFF0D47A1)   // Gradient end

// ── Accent ───────────────────────────────────────────────────────
val AccentTeal        = Color(0xFF00897B)   // Success / teal
val AccentTealLight   = Color(0xFF0F766E)   // Teal bg tint

// ── Adaptive Container Colors (Light vs Dark) ─────────────────────
val PrimaryBlueLight: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF1E3A8A) else Color(0xFFE3F2FD)
val GreenLight: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF064E3B) else Color(0xFFDCFCE7)
val YellowLight: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF78350F) else Color(0xFFFEF3C7)
val RedLight: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)

// ── Status Text Colors ───────────────────────────────────────────
val GreenActive: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFF4ADE80) else Color(0xFF16A34A)
val YellowReported: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFFFBBF24) else Color(0xFFD97706)
val RedOffline: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color(0xFF0F172A)) Color(0xFFF87171) else Color(0xFFDC2626)

// ── Gradient stops ────────────────────────────────────────────────
val GradientStart     = Color(0xFF1565C0)
val GradientEnd       = Color(0xFF00897B)

// ── Dynamic Theme Colors (Material 3 Adaptive) ───────────────────
val BackgroundWhite: Color @Composable get() = MaterialTheme.colorScheme.background
val SurfaceWhite: Color @Composable get() = MaterialTheme.colorScheme.surface
val OnSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val OnSurfaceMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val OnSurfaceSubtle: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderLight: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderLighter: Color @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

val BackgroundDark: Color @Composable get() = MaterialTheme.colorScheme.background
val SurfaceCard: Color @Composable get() = MaterialTheme.colorScheme.surface
val SurfaceDark: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val SurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val BorderSubtle: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderDefault: Color @Composable get() = MaterialTheme.colorScheme.outline
