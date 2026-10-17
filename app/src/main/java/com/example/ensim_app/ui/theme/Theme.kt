package com.example.ensim_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BrandColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = White,
    background = Navy900,
    onBackground = OffWhite,
    surface = Navy800,
    onSurface = OffWhite,
    surfaceVariant = Navy800,
    onSurfaceVariant = SlateBlue,
)

@Composable
fun ENSIM_appTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrandColorScheme,
        typography = Typography,
        content = content
    )
}
