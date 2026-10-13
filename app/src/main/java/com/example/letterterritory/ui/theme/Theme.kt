package com.example.letterterritory.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.letterterritory.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PlayerPrimaryDark,
    onPrimary = PlayerOnPrimaryDark,
    primaryContainer = PlayerPrimaryContainerDark,
    onPrimaryContainer = PlayerOnPrimaryContainerDark,
    secondary = CpuSecondaryDark,
    onSecondary = CpuOnSecondaryDark,
    secondaryContainer = CpuSecondaryContainerDark,
    onSecondaryContainer = CpuOnSecondaryContainerDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PlayerPrimaryLight,
    onPrimary = PlayerOnPrimaryLight,
    primaryContainer = PlayerPrimaryContainerLight,
    onPrimaryContainer = PlayerOnPrimaryContainerLight,
    secondary = CpuSecondaryLight,
    onSecondary = CpuOnSecondaryLight,
    secondaryContainer = CpuSecondaryContainerLight,
    onSecondaryContainer = CpuOnSecondaryContainerLight,
    background = LightBackground,
    surface = LightSurface,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    outline = LightOutline
)

@Composable
fun LetterTerritoryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}