package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// iOS 26 Liquid Glass System Colors
val LiquidCyan = Color(0xFF00F2FE)
val LiquidBlue = Color(0xFF4FACFE)
val LiquidPurple = Color(0xFF8A2BE2)
val LiquidViolet = Color(0xFF7F00FF)
val LiquidMagenta = Color(0xFFFF2A85)
val LiquidRose = Color(0xFFFF416C)
val LiquidEmerald = Color(0xFF00E676)
val LiquidGreen = Color(0xFF30D158)
val LiquidAmber = Color(0xFFFFB300)
val LiquidCoral = Color(0xFFFF5252)
val LiquidRed = LiquidCoral

// Liquid Dark Background Palette (Fluid obsidian & ethereal nebulae)
val LiquidBgDark = Color(0xFF070811)
val LiquidBgDeep = Color(0xFF0C0F22)
val LiquidBgAccent = Color(0xFF141A38)

// Specular Glass Translucencies
val GlassSurface = Color(0x1AFFFFFF)          // 10% white frosted body
val GlassSurfaceElevated = Color(0x28FFFFFF)  // 16% white frosted
val GlassSurfacePressed = Color(0x40FFFFFF)   // 25% white on tap
val GlassBorderHighlight = Color(0x66FFFFFF)  // 40% top-edge specular refraction
val GlassBorderLow = Color(0x14FFFFFF)        // 8% bottom-edge reflection
val GlassKeypad = Color(0x24FFFFFF)           // 14% frosted disc
val GlassKeypadPressed = Color(0x50FFFFFF)
val GlassDockBg = Color(0xCC0D1022)           // Translucent dock pill
val GlassDockBorder = Color(0x40FFFFFF)

// Standard iOS reference colors
val IosBlue = LiquidBlue
val IosBlueDark = LiquidCyan
val IosGreen = LiquidEmerald
val IosGreenDark = LiquidGreen
val IosRed = LiquidCoral
val IosRedDark = Color(0xFFFF453A)
val IosOrange = Color(0xFFFF9500)
val IosYellow = LiquidAmber
val IosPurple = LiquidPurple
val IosTeal = LiquidCyan
val IosIndigo = LiquidViolet

// Typography on Liquid Glass
val LiquidTextPrimary = Color(0xFFFFFFFF)
val LiquidTextSecondary = Color(0xFFB0B7C3)
val LiquidTextTertiary = Color(0xFF6B7280)

// Liquid Glass Avatar Gradient Palettes (Dual-tone liquid orbs)
val LiquidAvatarGradients = listOf(
    listOf(Color(0xFF00F2FE), Color(0xFF4FACFE)), // Cyan -> Electric Blue
    listOf(Color(0xFFFF0844), Color(0xFFFFB199)), // Coral -> Peach
    listOf(Color(0xFF7F00FF), Color(0xFFE100FF)), // Deep Purple -> Neon Violet
    listOf(Color(0xFF00F5A0), Color(0xFF00D9F5)), // Mint -> Turquoise
    listOf(Color(0xFFFA709A), Color(0xFFFEE140)), // Sunset Magenta -> Amber
    listOf(Color(0xFF667EEA), Color(0xFF764BA2)), // Cobalt -> Indigo
    listOf(Color(0xFFFF5858), Color(0xFFF09819)), // Neon Tangerine
    listOf(Color(0xFF0BA360), Color(0xFF3CBA92))  // Liquid Jade
)

val AvatarColors = LiquidAvatarGradients.map { it[0] }
