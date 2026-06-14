package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// In Persian and Arabic, letters connect cursively.
// Standard English letter-spacing divides cursive letters and causes disjointed gaps.
// We force a clean 0.sp letter-spacing and generous line-heights (1.35x - 1.5x) for maximum scientific clarity.
val VazirFontFamily = FontFamily.SansSerif
val InterFontFamily = FontFamily.Default
val PersianFont = VazirFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 68.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 54.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp, // Taller line-height allows dense multi-loop Persian curves to show without overlapping
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium, // Medium weight for better Persian legibility
        fontSize = 14.sp,
        lineHeight = 24.sp, // Ample line height for dense cursive connecting structures
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp, // Increased line height to 20.sp for beautiful legibility
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PersianFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)
