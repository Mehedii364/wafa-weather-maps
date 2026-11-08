package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AuroraDarkColorScheme = darkColorScheme(
    primary = AuroraDarkPrimary,
    onPrimary = Color.White,
    primaryContainer = AuroraDarkElevated,
    onPrimaryContainer = AuroraDarkCyan,
    secondary = AuroraDarkViolet,
    onSecondary = Color.White,
    secondaryContainer = AuroraDarkElevated,
    onSecondaryContainer = AuroraDarkViolet,
    tertiary = AuroraDarkCyan,
    onTertiary = Color.Black,
    background = AuroraDarkBackground,
    onBackground = AuroraDarkTextPrimary,
    surface = AuroraDarkSurface,
    onSurface = AuroraDarkTextPrimary,
    surfaceVariant = AuroraDarkElevated,
    onSurfaceVariant = AuroraDarkTextSecondary,
    outline = AuroraDarkBorder,
    outlineVariant = AuroraDarkBorder.copy(alpha = 0.5f)
)

val AuroraLightColorScheme = lightColorScheme(
    primary = AuroraLightPrimary,
    onPrimary = Color.White,
    primaryContainer = AuroraLightElevated,
    onPrimaryContainer = AuroraLightPrimary,
    secondary = AuroraLightViolet,
    onSecondary = Color.White,
    secondaryContainer = AuroraLightElevated,
    onSecondaryContainer = AuroraLightViolet,
    tertiary = AuroraLightCyan,
    onTertiary = Color.White,
    background = AuroraLightBackground,
    onBackground = AuroraLightTextPrimary,
    surface = AuroraLightSurface,
    onSurface = AuroraLightTextPrimary,
    surfaceVariant = AuroraLightElevated,
    onSurfaceVariant = AuroraLightTextSecondary,
    outline = AuroraLightBorder,
    outlineVariant = AuroraLightBorder.copy(alpha = 0.5f)
)

@Composable
fun AuroraTheme(
    darkTheme: Boolean = true, // Default to Aurora Obsidian Dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AuroraDarkColorScheme else AuroraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
