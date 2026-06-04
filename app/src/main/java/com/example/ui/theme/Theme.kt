package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val BotColorScheme = darkColorScheme(
    primary = BotPrimary,
    secondary = BotSecondary,
    tertiary = BotTertiary,
    background = BotBackground,
    surface = BotSurface,
    surfaceVariant = BotSurfaceVariant,
    onPrimary = BotOnPrimary,
    onSecondary = BotBackground,
    onTertiary = BotBackground,
    onBackground = BotOnSurface,
    onSurface = BotOnSurface,
    onSurfaceVariant = BotOnSurfaceVariant,
    error = BotError,
    onError = BotOnPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Always use our custom theme, disable dynamic color for this trading bot
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = BotColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
