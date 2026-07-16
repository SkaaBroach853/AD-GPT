package com.adgpt.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = SoftBlue,
    secondary = ElectricBlue,
    background = CarbonBlack,
    surface = PanelBlack,
    onPrimary = CarbonBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ADGPTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = AppTypography,
        content = content
    )
}
