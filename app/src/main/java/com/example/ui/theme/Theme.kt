package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = VetDarkPrimary,
    secondary = VetDarkSecondary,
    tertiary = VetDarkSecondary,
    background = VetBackgroundDark,
    surface = VetSurfaceDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = VetTealPrimary,
    secondary = VetTealSecondary,
    tertiary = VetTealTertiary,
    background = VetBackgroundLight,
    surface = VetSurfaceLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce our premium teal visual branding
  dynamicColor: Boolean = false,
  layoutDirection: LayoutDirection = LayoutDirection.Rtl,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography) {
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
      content()
    }
  }
}
