package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallRecord
import com.example.ui.components.IosAvatar
import com.example.ui.theme.IosRed
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

    val transition = rememberInfiniteTransition()
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = call.name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (durationSeconds == 0) "calling..." else durationFormatted,
                    fontSize = 17.sp,
                    color = Color(0xFF8E8E93)
                )

                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    modifier = Modifier.scale(if (durationSeconds == 0) pulseScale else 1f)
                ) {
                    IosAvatar(
                        initials = if (call.name.isNotBlank()) {
                            val parts = call.name.split(" ")
                            if (parts.size == 1) parts[0].take(1).uppercase()
                            else "${parts[0].take(1)}${parts.last().take(1)}".uppercase()
                        } else "#",
                        colorIndex = abs(call.name.hashCode()) % 8,
                        size = 100.dp
                    )
                }
            }

            // In-Call Action Control Grid (2 rows of 3 buttons)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "mute",
                        isActive = isMuted,
                        onClick = onToggleMute,
                        testTag = "call_mute_button"
                    )
                    CallControlButton(
                        icon = Icons.Default.Dialpad,
                        label = "keypad",
                        isActive = false,
                        onClick = { /* keypad in call */ },
                        testTag = "call_keypad_button"
                    )
                    CallControlButton(
                        icon = Icons.Default.VolumeUp,
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
                    CallControlButton(
                        icon = Icons.Default.Add,
                        label = "add call",
                        isActive = false,
                        onClick = { /* add call */ }
                    )
                    CallControlButton(
                        icon = Icons.Default.Videocam,
                        label = "FaceTime",
                        isActive = false,
                        onClick = { /* facetime */ }
                    )
                    CallControlButton(
                        icon = Icons.Default.People,
                        label = "contacts",
                        isActive = false,
                        onClick = { /* contacts in call */ }
                    )
                }
            }

            // Red End Call Button
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .background(IosRed)
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
fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val bg = if (isActive) Color.White else Color(0xFF2C2C2E)
    val tint = if (isActive) Color.Black else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}
