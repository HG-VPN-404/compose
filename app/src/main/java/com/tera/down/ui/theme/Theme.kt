package com.tera.down.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Skema warna gelap (Dark Mode Only)
private val DarkColorScheme = darkColorScheme(
    primary = RedNetflix,
    secondary = RedNetflix,
    tertiary = RedNetflix,
    background = Black,
    surface = DarkGray,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = White,
    onSurface = White
)

@Composable
fun TeraDownTheme(
    content: @Composable () -> Unit
) {
    // Kita langsung tembak MaterialTheme dengan skema warna gelap
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}