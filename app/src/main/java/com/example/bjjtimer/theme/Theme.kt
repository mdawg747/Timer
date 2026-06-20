package com.example.bjjtimer.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BJJDarkColorScheme = darkColorScheme(
    primary = BjjGold,
    onPrimary = DarkBackground,
    primaryContainer = BjjGoldDark,
    onPrimaryContainer = BjjGoldLight,
    secondary = RestBlue,
    onSecondary = Color.White,
    secondaryContainer = RestBlueDark,
    onSecondaryContainer = RestBlueGlow,
    tertiary = TimerGreen,
    onTertiary = Color.White,
    tertiaryContainer = TimerGreen,
    onTertiaryContainer = TimerGreenGlow,
    error = TimerRed,
    onError = Color.White,
    errorContainer = TimerRed,
    onErrorContainer = TimerRedGlow,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderMuted,
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = BjjGoldDark
)

@Composable
fun BJJTimerTheme(
    darkTheme: Boolean = true, // Always dark for gym visibility
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BJJDarkColorScheme,
        typography = Typography,
        content = content
    )
}
