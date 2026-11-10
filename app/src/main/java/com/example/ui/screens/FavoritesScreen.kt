package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.ui.components.IosAvatar
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.liquidGlass
import com.example.ui.theme.IosYellow
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary

@Composable
fun FavoritesScreen(
    favorites: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onCallClick: (number: String, name: String) -> Unit,
    onNavigateToContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 76.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidTextPrimary
                )
            }

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    borderWidth = 1.dp,
                                    bgAlpha = 0.15f,
                                    borderAlpha = 0.45f
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "No Favorites",
                                tint = IosYellow,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "No Favorites Yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Star contacts to keep your closest connections a single tap away in Liquid Glass.",
                            fontSize = 14.sp,
                            color = LiquidTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(favorites, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactClick(contact) }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("favorite_row_${contact.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IosAvatar(
                                initials = contact.initials,
                                colorIndex = contact.avatarColorIndex,
                                size = 48.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.displayName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiquidTextPrimary
                                )
                                Text(
                                    text = contact.phoneNumbers.firstOrNull()?.label?.lowercase() ?: "mobile",
                                    fontSize = 13.sp,
                                    color = LiquidCyan.copy(alpha = 0.8f)
                                )
                            }

                            // Liquid Emerald Call Button
                            IconButton(
                                onClick = {
                                    if (contact.primaryPhone.isNotBlank()) {
                                        onCallClick(contact.primaryPhone, contact.displayName)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .liquidGlass(
                                        shape = CircleShape,
                                        borderWidth = 1.dp,
                                        bgAlpha = 0.20f,
                                        borderAlpha = 0.4f
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = LiquidEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 84.dp)
                        )
                    }
                }
            }
        }
    }
}
