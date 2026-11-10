package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderLow
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LiquidBgDark
import com.example.ui.theme.LiquidBgDeep
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidPurple
import com.example.ui.theme.LiquidViolet

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "liquid_orbs")
    val animOffset1 by transition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val animOffset2 by transition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidBgDark)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Deep space gradient base
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LiquidBgDark,
                            LiquidBgDeep,
                            LiquidBgDark
                        )
                    )
                )

                // Top-left luminous cyan liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.22f),
                            LiquidCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.15f + animOffset1, h * 0.18f + animOffset2),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.15f + animOffset1, h * 0.18f + animOffset2)
                )

                // Top-right ethereal violet liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidPurple.copy(alpha = 0.20f),
                            LiquidPurple.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.85f - animOffset2, h * 0.25f + animOffset1),
                        radius = w * 0.70f
                    ),
                    radius = w * 0.70f,
                    center = Offset(w * 0.85f - animOffset2, h * 0.25f + animOffset1)
                )

                // Bottom-center electric indigo liquid orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidViolet.copy(alpha = 0.18f),
                            LiquidViolet.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f + animOffset2, h * 0.78f - animOffset1),
                        radius = w * 0.75f
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.5f + animOffset2, h * 0.78f - animOffset1)
                )
            },
        content = content
    )
}

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    bgAlpha: Float = 0.14f,
    borderAlpha: Float = 0.45f
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = bgAlpha * 1.5f),
                Color.White.copy(alpha = bgAlpha * 0.8f),
                Color.White.copy(alpha = bgAlpha * 0.4f)
            )
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.25f),
                Color.White.copy(alpha = borderAlpha * 0.05f),
                Color.White.copy(alpha = borderAlpha * 0.40f)
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        ),
        shape = shape
    )

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .liquidGlass(shape = shape),
        content = content
    )
}
