package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvatarColors

@Composable
fun IosAvatar(
    initials: String,
    colorIndex: Int,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val baseColor = AvatarColors.getOrElse(colorIndex % AvatarColors.size) { AvatarColors[0] }
    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.95f),
            baseColor.copy(alpha = 0.75f)
        )
    )

    val fontSize = (size.value * 0.42f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
