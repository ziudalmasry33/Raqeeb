package com.raqeeb.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = DarkCharcoal,
    primaryContainer = DeepEmerald,
    onPrimaryContainer = SoftWhite,
    secondary = EmeraldLight,
    onSecondary = SoftWhite,
    background = DarkCharcoal,
    onBackground = SoftWhite,
    surface = SurfaceDark,
    onSurface = SoftWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = MutedText,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = SoftWhite,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = SoftWhite,
    secondary = AccentGold,
    onSecondary = DarkCharcoal,
    background = LightBackground,
    onBackground = DarkCharcoal,
    surface = LightSurface,
    onSurface = DarkCharcoal,
    surfaceVariant = Color(0xFFE8EFEA),
    onSurfaceVariant = Color(0xFF4A5D57),
    error = AlertRed
)

@Composable
fun RaqeebTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}
