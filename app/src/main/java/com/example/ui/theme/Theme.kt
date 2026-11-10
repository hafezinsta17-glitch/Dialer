package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LiquidGlassColorScheme = darkColorScheme(
    primary = LiquidCyan,
    onPrimary = LiquidBgDark,
    secondary = LiquidEmerald,
    onSecondary = LiquidBgDark,
    tertiary = LiquidPurple,
    background = LiquidBgDark,
    onBackground = LiquidTextPrimary,
    surface = GlassSurface,
    onSurface = LiquidTextPrimary,
    surfaceVariant = GlassSurfaceElevated,
    onSurfaceVariant = LiquidTextSecondary,
    outline = GlassBorderHighlight,
    error = LiquidCoral,
    onError = LiquidTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to stunning Liquid Glass dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = LiquidGlassColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = LiquidBgDark.toArgb()
            window.navigationBarColor = LiquidBgDark.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
