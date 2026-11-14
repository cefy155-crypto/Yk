package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = OnElectricLime,
    primaryContainer = CharcoalElevated,
    onPrimaryContainer = ElectricLime,
    secondary = CyberCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = CyberCyanContainer,
    onSecondaryContainer = CyberCyan,
    tertiary = AmberKinetic,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberKinetic,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalBorder,
    outlineVariant = CharcoalBorder,
    error = CoralAlert,
    onError = ObsidianBg,
    errorContainer = CoralContainer,
    onErrorContainer = CoralAlert
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = LightSurface,
    tertiary = AmberKinetic,
    onTertiary = LightTextPrimary,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = ColorTokens.LightOutline,
    error = CoralAlert,
    onError = LightSurface
)

private object ColorTokens {
    val LightOutline = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
}

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SportCoachProTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
