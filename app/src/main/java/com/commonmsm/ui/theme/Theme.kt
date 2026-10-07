package com.commonmsm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BrutalColorScheme = darkColorScheme(
    primary = BrutalOrange,
    onPrimary = BrutalBlack,
    primaryContainer = BrutalBlack,
    onPrimaryContainer = BrutalOrange,
    secondary = BrutalNeonGreen,
    onSecondary = BrutalBlack,
    background = BrutalBlack,
    onBackground = BrutalWhite,
    surface = BrutalDarkSurface,
    onSurface = BrutalWhite,
    surfaceVariant = BrutalElevated,
    onSurfaceVariant = BrutalGray,
    outline = BrutalBorder
)

@Composable
fun CommonMsmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrutalColorScheme,
        typography = Typography,
        content = content
    )
}
