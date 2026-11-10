package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary

data class KeypadButtonData(
    val digit: String,
    val letters: String
)

val KeypadRows = listOf(
    listOf(KeypadButtonData("1", ""), KeypadButtonData("2", "ABC"), KeypadButtonData("3", "DEF")),
    listOf(KeypadButtonData("4", "GHI"), KeypadButtonData("5", "JKL"), KeypadButtonData("6", "MNO")),
    listOf(KeypadButtonData("7", "PQRS"), KeypadButtonData("8", "TUV"), KeypadButtonData("9", "WXYZ")),
    listOf(KeypadButtonData("*", ""), KeypadButtonData("0", "+"), KeypadButtonData("#", ""))
)

@Composable
fun KeypadScreen(
    inputNumber: String,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearAll: () -> Unit,
    onCallClick: (useSystemDialer: Boolean) -> Unit,
    onAddNumberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 76.dp), // space for floating dock
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display area for dialed number
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (inputNumber.isEmpty()) " " else inputNumber,
                    fontSize = if (inputNumber.length > 12) 28.sp else 36.sp,
                    fontWeight = FontWeight.Medium,
                    color = LiquidTextPrimary,
                    maxLines = 1,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("dialed_number_display")
                )

                AnimatedVisibility(
                    visible = inputNumber.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onAddNumberClick(inputNumber)
                            }
                            .padding(top = 8.dp)
                            .testTag("add_number_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Number",
                            tint = LiquidCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Number",
                            color = LiquidCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Circular Liquid Glass Keypad Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3.5f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in KeypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (item in row) {
                            LiquidGlassKeypadButton(
                                data = item,
                                onClick = {
                                    triggerHaptic()
                                    onDigitClick(item.digit)
                                },
                                onLongClick = {
                                    triggerHaptic()
                                    if (item.digit == "0") {
                                        onDigitClick("+")
                                    }
                                }
                            )
                        }
                    }
                }

                // Bottom row: Empty space, Liquid Emerald Call Button, Backspace Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(76.dp))

                    // Liquid Emerald Glowing Call Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00F5A0),
                                        LiquidEmerald,
                                        Color(0xFF00965E)
                                    ),
                                    center = Offset(38f, 30f),
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
                                // Top crescent reflection
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
                            .clickable {
                                triggerHaptic()
                                onCallClick(false)
                            }
                            .testTag("call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Backspace button
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (inputNumber.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                triggerHaptic()
                                                onBackspaceClick()
                                            },
                                            onLongPress = {
                                                triggerHaptic()
                                                onClearAll()
                                            }
                                        )
                                    }
                                    .testTag("keypad_backspace_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    tint = LiquidTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LiquidGlassKeypadButton(
    data: KeypadButtonData,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.35f)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = CircleShape
            )
            .drawBehind {
                // Top crescent specular reflection
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.35f, size.height * 0.22f),
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.35f, size.height * 0.22f)
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .testTag("keypad_btn_${data.digit}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = data.digit,
                fontSize = if (data.digit == "*" || data.digit == "#") 32.sp else 35.sp,
                fontWeight = FontWeight.Normal,
                color = LiquidTextPrimary,
                lineHeight = 36.sp
            )
            if (data.letters.isNotEmpty()) {
                Text(
                    text = data.letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidCyan.copy(alpha = 0.85f),
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
