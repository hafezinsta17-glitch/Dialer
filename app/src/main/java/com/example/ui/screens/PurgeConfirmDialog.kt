package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.liquidGlass
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary
import com.example.viewmodel.PurgeDialogType

@Composable
fun PurgeConfirmDialog(
    type: PurgeDialogType,
    selectedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmationWord by remember { mutableStateOf("") }
    val isPurgeAll = type == PurgeDialogType.PURGE_ALL
    val canProceed = if (isPurgeAll) confirmationWord.trim().uppercase() == "PURGE" else true

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    borderWidth = 1.2.dp,
                    bgAlpha = 0.28f,
                    borderAlpha = 0.6f
                )
                .background(Color(0xE60F1328))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing Warning Orb
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (type == PurgeDialogType.CLEAN_DUPLICATES) LiquidCyan.copy(alpha = 0.18f)
                            else LiquidRed.copy(alpha = 0.18f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (type == PurgeDialogType.CLEAN_DUPLICATES) LiquidCyan.copy(alpha = 0.5f)
                            else LiquidRed.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            PurgeDialogType.PURGE_ALL -> Icons.Default.DeleteForever
                            PurgeDialogType.PURGE_SELECTED -> Icons.Default.GroupRemove
                            PurgeDialogType.CLEAN_DUPLICATES -> Icons.Default.CleaningServices
                        },
                        contentDescription = null,
                        tint = if (type == PurgeDialogType.CLEAN_DUPLICATES) LiquidCyan else LiquidRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val title = when (type) {
                    PurgeDialogType.PURGE_ALL -> "Purge All Phone Contacts?"
                    PurgeDialogType.PURGE_SELECTED -> "Purge $selectedCount Contacts?"
                    PurgeDialogType.CLEAN_DUPLICATES -> "Clean Duplicate Contacts?"
                }

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                val message = when (type) {
                    PurgeDialogType.PURGE_ALL ->
                        "This will permanently erase all $totalCount contacts from your device Contacts Provider. This operation cannot be undone."
                    PurgeDialogType.PURGE_SELECTED ->
                        "This will permanently remove the $selectedCount selected contacts from your device address book."
                    PurgeDialogType.CLEAN_DUPLICATES ->
                        "This will merge duplicate names and numbers, purging redundant entries."
                }

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = LiquidTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                // Extra safety check for Purge All
                if (isPurgeAll) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Type \"PURGE\" to confirm:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmationWord,
                        onValueChange = { confirmationWord = it },
                        singleLine = true,
                        placeholder = { Text("PURGE", color = LiquidTextSecondary.copy(alpha = 0.5f)) },
                        textStyle = TextStyle(color = LiquidTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = LiquidRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purge_confirm_text_input")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("purge_cancel_button")
                    ) {
                        Text(text = "Cancel", fontSize = 16.sp, color = LiquidTextSecondary)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = canProceed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == PurgeDialogType.CLEAN_DUPLICATES) LiquidCyan else LiquidRed
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("purge_confirm_button")
                    ) {
                        Text(
                            text = if (type == PurgeDialogType.CLEAN_DUPLICATES) "Clean" else "Purge",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
