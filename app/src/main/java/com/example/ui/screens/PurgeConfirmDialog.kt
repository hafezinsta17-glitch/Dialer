package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosRed
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
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(IosRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (type) {
                            PurgeDialogType.PURGE_ALL -> Icons.Default.DeleteForever
                            PurgeDialogType.PURGE_SELECTED -> Icons.Default.GroupRemove
                            PurgeDialogType.CLEAN_DUPLICATES -> Icons.Default.CleaningServices
                        },
                        contentDescription = null,
                        tint = if (type == PurgeDialogType.CLEAN_DUPLICATES) IosBlue else IosRed,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val title = when (type) {
                    PurgeDialogType.PURGE_ALL -> "Purge All Phone Contacts?"
                    PurgeDialogType.PURGE_SELECTED -> "Purge $selectedCount Contacts?"
                    PurgeDialogType.CLEAN_DUPLICATES -> "Clean Duplicate Contacts?"
                }

                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                val message = when (type) {
                    PurgeDialogType.PURGE_ALL ->
                        "This will permanently delete all $totalCount contacts from your device address book. This action cannot be undone."
                    PurgeDialogType.PURGE_SELECTED ->
                        "This will permanently remove the $selectedCount selected contacts from your phone. Are you sure you want to proceed?"
                    PurgeDialogType.CLEAN_DUPLICATES ->
                        "This will scan your contact list for duplicate names and phone numbers, merging entries and purging duplicates."
                }

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                // Extra security check for Purge All
                if (isPurgeAll) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Type \"PURGE\" to confirm:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IosRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmationWord,
                        onValueChange = { confirmationWord = it },
                        singleLine = true,
                        placeholder = { Text("PURGE") },
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
                            .height(46.dp)
                            .testTag("purge_cancel_button")
                    ) {
                        Text(text = "Cancel", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = canProceed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == PurgeDialogType.CLEAN_DUPLICATES) IosBlue else IosRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
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
