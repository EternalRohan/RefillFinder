package com.refillfinder.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 26.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 16.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 13.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 10.sp, letterSpacing = 0.4.sp)
)
