package com.refillfinder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary           = PrimaryBlue,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFE3F2FD),
    onPrimaryContainer= PrimaryBlueDark,
    secondary         = AccentTeal,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFFE0F2F1),
    background        = Color(0xFFF4F7FB),
    onBackground      = Color(0xFF0F172A),
    surface           = Color(0xFFFFFFFF),
    onSurface         = Color(0xFF0F172A),
    surfaceVariant    = Color(0xFFF1F5F9),
    onSurfaceVariant  = Color(0xFF64748B),
    outline           = Color(0xFFE2E8F0),
    error             = Color(0xFFDC2626),
    onError           = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary           = PrimaryBlueMid,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFF1E293B),
    onPrimaryContainer= Color.White,
    secondary         = AccentTeal,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFF0F766E),
    background        = Color(0xFF0F172A),  // Dark Slate 900
    onBackground      = Color(0xFFF8FAFC),  // Light Slate 50
    surface           = Color(0xFF1E293B),  // Dark Slate 800
    onSurface         = Color(0xFFF8FAFC),  // Light Slate 50
    surfaceVariant    = Color(0xFF334155),  // Dark Slate 700
    onSurfaceVariant  = Color(0xFFCBD5E1),  // Light Slate 300
    outline           = Color(0xFF475569),  // Dark Slate 600
    error             = Color(0xFFF87171),
    onError           = Color.White
)

@Composable
fun ReFillFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
