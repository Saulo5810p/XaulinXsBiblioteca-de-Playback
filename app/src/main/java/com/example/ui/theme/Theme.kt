package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RetroReceiverColorScheme = darkColorScheme(
    primary = VintageAmber,
    onPrimary = MetalDarkBackground,
    primaryContainer = VintageAmberGlow,
    onPrimaryContainer = VintageAmber,
    secondary = FluorescentGreen,
    onSecondary = MetalDarkBackground,
    secondaryContainer = FluorescentGreenGlow,
    onSecondaryContainer = FluorescentGreen,
    tertiary = BrassGold,
    background = MetalDarkBackground,
    onBackground = TextMetallicLight,
    surface = MetalPanelSurface,
    onSurface = TextMetallicLight,
    surfaceVariant = MetalCardSurface,
    onSurfaceVariant = TextMetallicMuted,
    outline = MetallicBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroReceiverColorScheme,
        typography = Typography,
        content = content
    )
}
