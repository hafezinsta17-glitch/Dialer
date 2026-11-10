package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.ui.components.IosAvatar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.liquidGlass
import com.example.ui.theme.IosYellow
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary

@Composable
fun ContactDetailScreen(
    contact: Contact,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCallClick: (number: String, name: String) -> Unit,
    onShareSingleVcf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("contact_detail_back_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = LiquidCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Contacts", color = LiquidCyan, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("contact_detail_edit_button")
                ) {
                    Text(text = "Edit", color = LiquidCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Profile Avatar & Name with Liquid Halo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidCyan.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                radius = size.width * 0.75f
                            )
                        )
                    }
                ) {
                    IosAvatar(
                        initials = contact.initials,
                        colorIndex = contact.avatarColorIndex,
                        size = 98.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = contact.displayName,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidTextPrimary
                )
                if (contact.company.isNotBlank() || contact.jobTitle.isNotBlank()) {
                    val sub = listOf(contact.jobTitle, contact.company).filter { it.isNotBlank() }.joinToString(" • ")
                    Text(
                        text = sub,
                        fontSize = 15.sp,
                        color = LiquidCyan.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 4 iOS Liquid Glass Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LiquidActionButton(
                    icon = Icons.Default.Chat,
                    label = "message",
                    tint = LiquidCyan,
                    onClick = {
                        if (contact.primaryPhone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${contact.primaryPhone}"))
                            context.startActivity(intent)
                        }
                    }
                )
                LiquidActionButton(
                    icon = Icons.Default.Call,
                    label = "call",
                    tint = LiquidEmerald,
                    onClick = {
                        if (contact.primaryPhone.isNotBlank()) {
                            onCallClick(contact.primaryPhone, contact.displayName)
                        }
                    }
                )
                LiquidActionButton(
                    icon = Icons.Default.Videocam,
                    label = "video",
                    tint = LiquidCyan,
                    onClick = {
                        if (contact.primaryPhone.isNotBlank()) {
                            onCallClick(contact.primaryPhone, contact.displayName)
                        }
                    }
                )
                LiquidActionButton(
                    icon = Icons.Default.Email,
                    label = "mail",
                    tint = LiquidCyan,
                    enabled = contact.primaryEmail.isNotBlank(),
                    onClick = {
                        if (contact.primaryEmail.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.primaryEmail}"))
                            context.startActivity(intent)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phone Numbers Liquid Glass Card
            if (contact.phoneNumbers.isNotEmpty()) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        contact.phoneNumbers.forEachIndexed { index, phone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCallClick(phone.value, contact.displayName) }
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = phone.label.lowercase(),
                                        fontSize = 12.sp,
                                        color = LiquidTextSecondary
                                    )
                                    Text(
                                        text = phone.value,
                                        fontSize = 17.sp,
                                        color = LiquidCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = LiquidEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (index < contact.phoneNumbers.size - 1) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Email Section
            if (contact.emails.isNotEmpty()) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        contact.emails.forEachIndexed { index, email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${email.value}"))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                Column {
                                    Text(
                                        text = email.label.lowercase(),
                                        fontSize = 12.sp,
                                        color = LiquidTextSecondary
                                    )
                                    Text(
                                        text = email.value,
                                        fontSize = 16.sp,
                                        color = LiquidCyan
                                    )
                                }
                            }
                            if (index < contact.emails.size - 1) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Notes Section
            if (contact.notes.isNotBlank()) {
                LiquidGlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Notes",
                            fontSize = 12.sp,
                            color = LiquidTextSecondary
                        )
                        Text(
                            text = contact.notes,
                            fontSize = 15.sp,
                            color = LiquidTextPrimary,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Action Options: Favorites & Share VCF
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleFavorite() }
                            .padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (contact.isFavorite) IosYellow else LiquidCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (contact.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            color = LiquidCyan,
                            fontSize = 16.sp
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 52.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShareSingleVcf() }
                            .padding(horizontal = 18.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.IosShare,
                            contentDescription = "Share",
                            tint = LiquidCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Share Contact (Export vCard)",
                            color = LiquidCyan,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Contact in Frosted Crimson Glass
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(20.dp),
                        borderWidth = 1.dp,
                        bgAlpha = 0.16f,
                        borderAlpha = 0.35f
                    )
                    .clickable(onClick = onDelete)
                    .padding(vertical = 16.dp)
                    .testTag("delete_contact_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete Contact",
                    color = LiquidRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun LiquidActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val displayTint = if (enabled) tint else LiquidTextSecondary.copy(alpha = 0.4f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .liquidGlass(
                    shape = CircleShape,
                    borderWidth = 1.dp,
                    bgAlpha = 0.16f,
                    borderAlpha = 0.4f
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = displayTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = displayTint,
            fontWeight = FontWeight.Medium
        )
    }
}
