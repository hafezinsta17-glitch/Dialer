package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallRecord
import com.example.ui.components.IosAvatar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.liquidGlass
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary
import kotlin.math.abs

@Composable
fun ActiveCallScreen(
    call: CallRecord,
    durationSeconds: Int,
    isMuted: Boolean,
    isSpeaker: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationFormatted = String.format(
        "%02d:%02d",
        durationSeconds / 60,
        durationSeconds % 60
    )

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = call.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (durationSeconds == 0) "calling with liquid glass..." else durationFormatted,
                    fontSize = 17.sp,
                    color = LiquidCyan,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(if (durationSeconds == 0) pulseScale else 1f)
                        .drawBehind {
                            // Holographic concentric liquid ripple rings
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        LiquidCyan.copy(alpha = 0.30f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.9f
                                )
                            )
                        }
                ) {
                    IosAvatar(
                        initials = if (call.name.isNotBlank()) {
                            val parts = call.name.split(" ")
                            if (parts.size == 1) parts[0].take(1).uppercase()
                            else "${parts[0].take(1)}${parts.last().take(1)}".uppercase()
                        } else "#",
                        colorIndex = abs(call.name.hashCode()) % 8,
                        size = 110.dp
                    )
                }
            }

            // In-Call Action Control Grid in Liquid Glass
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LiquidCallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "mute",
                        isActive = isMuted,
                        onClick = onToggleMute,
                        testTag = "call_mute_button"
                    )
                    LiquidCallControlButton(
                        icon = Icons.Default.Dialpad,
                        label = "keypad",
                        isActive = false,
                        onClick = { /* in-call keypad */ },
                        testTag = "call_keypad_button"
                    )
                    LiquidCallControlButton(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "speaker",
                        isActive = isSpeaker,
                        onClick = onToggleSpeaker,
                        testTag = "call_speaker_button"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LiquidCallControlButton(
                        icon = Icons.Default.Add,
                        label = "add call",
                        isActive = false,
                        onClick = { /* add call */ }
                    )
                    LiquidCallControlButton(
                        icon = Icons.Default.Videocam,
                        label = "FaceTime",
                        isActive = false,
                        onClick = { /* facetime */ }
                    )
                    LiquidCallControlButton(
                        icon = Icons.Default.People,
                        label = "contacts",
                        isActive = false,
                        onClick = { /* contacts */ }
                    )
                }
            }

            // Glowing Liquid Crimson End Call Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF5252),
                                LiquidRed,
                                Color(0xFFC62828)
                            ),
                            center = Offset(38f, 28f),
                            radius = 55f
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .drawBehind {
                        // Top crescent specular sheen
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.4f, size.height * 0.25f),
                                radius = size.width * 0.4f
                            ),
                            radius = size.width * 0.4f,
                            center = Offset(size.width * 0.4f, size.height * 0.25f)
                        )
                    }
                    .clickable(onClick = onEndCall)
                    .testTag("end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
fun LiquidCallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val activeTint = if (isActive) Color.Black else LiquidTextPrimary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .then(
                    if (isActive) {
                        Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                    } else {
                        Modifier.liquidGlass(
                            shape = CircleShape,
                            borderWidth = 1.dp,
                            bgAlpha = 0.16f,
                            borderAlpha = 0.45f
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = activeTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) LiquidCyan else LiquidTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
