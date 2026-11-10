package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidAvatarGradients

@Composable
fun IosAvatar(
    initials: String,
    colorIndex: Int,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val gradientColors = LiquidAvatarGradients.getOrElse(colorIndex % LiquidAvatarGradients.size) {
        LiquidAvatarGradients[0]
    }

    val liquidBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val fontSize = (size.value * 0.40f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(liquidBrush)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            )
            .drawBehind {
                // Top crescent specular reflection (liquid glass sheen)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(this.size.width * 0.35f, this.size.height * 0.25f),
                        radius = this.size.width * 0.45f
                    ),
                    radius = this.size.width * 0.45f,
                    center = Offset(this.size.width * 0.35f, this.size.height * 0.25f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
