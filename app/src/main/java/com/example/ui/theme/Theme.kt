package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SunsetArcadeColorScheme =
  darkColorScheme(
    primary = CoralPrimary,
    onPrimary = BackgroundDark,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = GoldSecondary,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = GoldSecondary,
    tertiary = MintSuccess,
    onTertiary = BackgroundDark,
    error = CrimsonDanger,
    onError = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder
  )

@Composable
fun ComplianceSlicerTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = SunsetArcadeColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) = ComplianceSlicerTheme(content = content)


