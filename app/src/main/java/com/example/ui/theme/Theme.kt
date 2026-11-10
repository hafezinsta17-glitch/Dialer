package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IosBlueDark,
    onPrimary = IosTextPrimaryDark,
    secondary = IosGreenDark,
    onSecondary = IosTextPrimaryDark,
    tertiary = IosIndigo,
    background = IosBgDark,
    onBackground = IosTextPrimaryDark,
    surface = IosSurfaceDark,
    onSurface = IosTextPrimaryDark,
    surfaceVariant = IosSecondarySurfaceDark,
    onSurfaceVariant = IosTextSecondaryDark,
    error = IosRedDark,
    onError = IosTextPrimaryDark,
    outline = IosSeparatorDark
)

private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = IosSurfaceLight,
    secondary = IosGreen,
    onSecondary = IosSurfaceLight,
    tertiary = IosIndigo,
    background = IosBgLight,
    onBackground = IosTextPrimaryLight,
    surface = IosSurfaceLight,
    onSurface = IosTextPrimaryLight,
    surfaceVariant = IosSecondarySurfaceLight,
    onSurfaceVariant = IosTextSecondaryLight,
    error = IosRed,
    onError = IosSurfaceLight,
    outline = IosSeparatorLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
