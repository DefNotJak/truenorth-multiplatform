package com.truenorth.citizenshiptest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.truenorth.citizenshiptest.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = CanadaRedLight,
    onPrimary = Charcoal,
    primaryContainer = CanadaRedContainerDark,
    onPrimaryContainer = CanadaRedContainerLight,
    secondary = LightOnDark,
    onSecondary = Charcoal,
    background = DarkBackground,
    onBackground = LightOnDark,
    surface = DarkSurface,
    onSurface = LightOnDark
)

private val LightColorScheme = lightColorScheme(
    primary = CanadaRed,
    onPrimary = Color.White,
    primaryContainer = CanadaRedContainerLight,
    onPrimaryContainer = CanadaRedContainerDark,
    secondary = Charcoal,
    onSecondary = Color.White,
    background = OffWhite,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal
)

@Composable
fun TrueNorthTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
