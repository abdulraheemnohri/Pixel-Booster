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
import com.example.core.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = PositiveMint,
    onSecondary = OnPositiveMint,
    secondaryContainer = MintContainerDark,
    onSecondaryContainer = PositiveMint,
    tertiary = WarningAmber,
    onTertiary = OnWarningAmber,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = WarningAmber,
    error = CriticalRed,
    onError = OnCriticalRed,
    errorContainer = RedContainerDark,
    onErrorContainer = CriticalRed,
    background = CanvasRootDark,
    onBackground = TextPrimaryDark,
    surface = MainSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = ElevatedSurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    surfaceContainer = ElevatedSurfaceDark,
    surfaceContainerHigh = HighElevationDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = OnPrimaryBlueLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = PositiveMint,
    onSecondary = OnPositiveMint,
    tertiary = WarningAmber,
    onTertiary = OnWarningAmber,
    error = CriticalRed,
    onError = OnCriticalRed,
    background = CanvasRootLight,
    onBackground = TextPrimaryLight,
    surface = MainSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = ElevatedSurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    surfaceContainer = ElevatedSurfaceLight,
    surfaceContainerHigh = HighElevationLight
)

@Composable
fun PixelBoosterTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
