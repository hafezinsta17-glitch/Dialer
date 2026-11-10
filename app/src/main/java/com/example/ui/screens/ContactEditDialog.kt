package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Contact
import com.example.model.ContactDetail
import com.example.ui.components.IosAvatar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary
import kotlin.math.abs

@Composable
fun ContactEditDialog(
    initialContact: Contact?,
    isNewContact: Boolean,
    onDismiss: () -> Unit,
    onSave: (contact: Contact, saveToDevice: Boolean) -> Unit
) {
    var firstName by remember {
        mutableStateOf(
            initialContact?.firstName ?: initialContact?.displayName?.split(" ")?.firstOrNull() ?: ""
        )
    }
    var lastName by remember {
        mutableStateOf(
            initialContact?.lastName ?: initialContact?.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: ""
        )
    }
    var phoneNumbers by remember {
        mutableStateOf(
            initialContact?.phoneNumbers?.map { it.value }?.ifEmpty { listOf("") } ?: listOf("")
        )
    }
    var email by remember {
        mutableStateOf(initialContact?.primaryEmail ?: "")
    }
    var company by remember {
        mutableStateOf(initialContact?.company ?: "")
    }
    var jobTitle by remember {
        mutableStateOf(initialContact?.jobTitle ?: "")
    }
    var notes by remember {
        mutableStateOf(initialContact?.notes ?: "")
    }
    var saveToDevice by remember {
        mutableStateOf(true)
    }

    val displayName = listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LiquidGlassBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("edit_contact_cancel_button")
                    ) {
                        Text(text = "Cancel", color = LiquidCyan, fontSize = 16.sp)
                    }

                    Text(
                        text = if (isNewContact) "New Contact" else "Edit Contact",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidTextPrimary
                    )

                    TextButton(
                        onClick = {
                            val finalDisplay = if (displayName.isNotBlank()) displayName else "Unnamed Contact"
                            val cleanPhones = phoneNumbers
                                .filter { it.isNotBlank() }
                                .map { ContactDetail("mobile", it.trim()) }
                            val cleanEmails = if (email.isNotBlank()) listOf(ContactDetail("home", email.trim())) else emptyList()

                            val updated = (initialContact ?: Contact(
                                id = java.util.UUID.randomUUID().toString(),
                                displayName = finalDisplay
                            )).copy(
                                displayName = finalDisplay,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                phoneNumbers = cleanPhones,
                                emails = cleanEmails,
                                company = company.trim(),
                                jobTitle = jobTitle.trim(),
                                notes = notes.trim(),
                                avatarColorIndex = abs(finalDisplay.hashCode()) % 8
                            )
                            onSave(updated, saveToDevice)
                        },
                        enabled = firstName.isNotBlank() || lastName.isNotBlank() || phoneNumbers.any { it.isNotBlank() },
                        modifier = Modifier.testTag("edit_contact_done_button")
                    ) {
                        Text(
                            text = "Done",
                            color = if (firstName.isNotBlank() || lastName.isNotBlank() || phoneNumbers.any { it.isNotBlank() }) LiquidCyan else LiquidCyan.copy(alpha = 0.3f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Avatar Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (displayName.isNotBlank()) {
                        val parts = displayName.split(" ")
                        if (parts.size == 1) parts[0].take(1).uppercase()
                        else "${parts[0].take(1)}${parts.last().take(1)}".uppercase()
                    } else "+"

                    IosAvatar(
                        initials = initials,
                        colorIndex = abs(displayName.hashCode()) % 8,
                        size = 84.dp
                    )
                }

                // Names Liquid Glass Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column {
                        LiquidInputField(
                            label = "First name",
                            value = firstName,
                            onValueChange = { firstName = it },
                            testTag = "input_first_name"
                        )
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        LiquidInputField(
                            label = "Last name",
                            value = lastName,
                            onValueChange = { lastName = it },
                            testTag = "input_last_name"
                        )
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        LiquidInputField(
                            label = "Company",
                            value = company,
                            onValueChange = { company = it },
                            testTag = "input_company"
                        )
                    }
                }

                // Phone Numbers Liquid Glass Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column {
                        phoneNumbers.forEachIndexed { index, phone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (phoneNumbers.size > 1) {
                                    IconButton(
                                        onClick = {
                                            phoneNumbers = phoneNumbers.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RemoveCircle,
                                            contentDescription = "Remove phone",
                                            tint = LiquidRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField(
                                        label = if (index == 0) "Mobile phone" else "Phone ${index + 1}",
                                        value = phone,
                                        onValueChange = { newVal ->
                                            phoneNumbers = phoneNumbers.mapIndexed { i, v -> if (i == index) newVal else v }
                                        },
                                        testTag = "input_phone_$index"
                                    )
                                }
                            }
                            if (index < phoneNumbers.size - 1) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }

                        // Add phone button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { phoneNumbers = phoneNumbers + "" },
                                modifier = Modifier.testTag("add_phone_field_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Add phone",
                                    tint = LiquidEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "add phone", color = LiquidCyan, fontSize = 15.sp)
                            }
                        }
                    }
                }

                // Email & Job Title Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column {
                        LiquidInputField(
                            label = "Email",
                            value = email,
                            onValueChange = { email = it },
                            testTag = "input_email"
                        )
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        LiquidInputField(
                            label = "Job title",
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            testTag = "input_job_title"
                        )
                    }
                }

                // Notes Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    LiquidInputField(
                        label = "Notes",
                        value = notes,
                        onValueChange = { notes = it },
                        singleLine = false,
                        testTag = "input_notes"
                    )
                }

                // Save to Device toggle in Liquid Glass Card
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Save to Android Contacts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LiquidTextPrimary
                            )
                            Text(
                                text = "Synchronizes with device address book",
                                fontSize = 12.sp,
                                color = LiquidTextSecondary
                            )
                        }
                        Switch(
                            checked = saveToDevice,
                            onCheckedChange = { saveToDevice = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LiquidEmerald
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun LiquidInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = label, color = LiquidTextSecondary.copy(alpha = 0.6f))
        },
        textStyle = TextStyle(
            color = LiquidTextPrimary,
            fontSize = 16.sp
        ),
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = LiquidCyan
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    )
}
