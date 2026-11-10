package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.ui.components.IosAlphabetScrubber
import com.example.ui.components.IosAvatar
import com.example.ui.components.IosSearchBar
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContactClick: (Contact) -> Unit,
    onAddContactClick: () -> Unit,
    isSelectionMode: Boolean,
    selectedContactIds: Set<String>,
    onToggleSelectionMode: () -> Unit,
    onToggleContactSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onPurgeSelected: () -> Unit,
    onExportSelected: () -> Unit,
    isDeviceConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Group contacts by section
    val grouped = remember(contacts) {
        contacts.groupBy { it.sortSection }
            .toSortedMap(compareBy { if (it == "#") "Z1" else it })
    }

    val availableSections = remember(grouped) { grouped.keys }

    // Map section header to index in LazyColumn
    val sectionIndices = remember(grouped) {
        val map = mutableMapOf<String, Int>()
        var currentIndex = 1 // 0 is My Card header
        for ((section, list) in grouped) {
            map[section] = currentIndex
            currentIndex += 1 + list.size
        }
        map
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // iOS Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleSelectionMode,
                    modifier = Modifier.testTag("toggle_selection_mode_button")
                ) {
                    Text(
                        text = if (isSelectionMode) "Done" else "Select",
                        color = IosBlue,
                        fontSize = 17.sp,
                        fontWeight = if (isSelectionMode) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Text(
                    text = "Contacts",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onAddContactClick,
                    modifier = Modifier.testTag("add_contact_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact",
                        tint = IosBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // iOS Search Bar
            IosSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

            // Contact List with Alphabet Scrubber
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 20.dp) // space for scrubber
                ) {
                    // My Card Header
                    if (searchQuery.isEmpty()) {
                        item {
                            MyCardRow()
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }

                    // Grouped contacts by letter
                    for ((section, sectionContacts) in grouped) {
                        item(key = "section_$section") {
                            SectionHeader(letter = section)
                        }

                        items(
                            items = sectionContacts,
                            key = { it.id }
                        ) { contact ->
                            ContactRow(
                                contact = contact,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedContactIds.contains(contact.id),
                                onClick = {
                                    if (isSelectionMode) {
                                        onToggleContactSelection(contact.id)
                                    } else {
                                        onContactClick(contact)
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = if (isSelectionMode) 104.dp else 72.dp)
                            )
                        }
                    }

                    // Bottom Total Contacts Counter
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${contacts.size} Contacts",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // A-Z Scrubber on the right
                if (searchQuery.isEmpty() && contacts.isNotEmpty()) {
                    IosAlphabetScrubber(
                        availableSections = availableSections,
                        onLetterSelected = { letter ->
                            sectionIndices[letter]?.let { idx ->
                                scope.launch {
                                    listState.scrollToItem(idx)
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        // Floating Batch Action Bar when Selection Mode is active
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BatchActionBar(
                selectedCount = selectedContactIds.size,
                totalCount = contacts.size,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onPurgeSelected = onPurgeSelected,
                onExportSelected = onExportSelected
            )
        }
    }
}

@Composable
fun MyCardRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IosAvatar(
            initials = "ME",
            colorIndex = 1,
            size = 54.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "My Card",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Set up your profile",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(letter: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = letter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ContactRow(
    contact: Contact,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .testTag("contact_row_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (isSelected) "Selected" else "Not Selected",
                tint = if (isSelected) IosBlue else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = 6.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        IosAvatar(
            initials = contact.initials,
            colorIndex = contact.avatarColorIndex,
            size = 42.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.displayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (contact.isFavorite) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        tint = IosYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (contact.company.isNotBlank() || contact.primaryPhone.isNotBlank()) {
                val subtext = if (contact.company.isNotBlank()) contact.company else contact.primaryPhone
                Text(
                    text = subtext,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        if (contact.isDeviceContact) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Device Contact",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun BatchActionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onPurgeSelected: () -> Unit,
    onExportSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedCount selected",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    if (selectedCount < totalCount) {
                        TextButton(onClick = onSelectAll) {
                            Text("Select All", color = IosBlue, fontSize = 14.sp)
                        }
                    } else {
                        TextButton(onClick = onDeselectAll) {
                            Text("Deselect", color = IosBlue, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ElevatedButton(
                    onClick = onExportSelected,
                    enabled = selectedCount > 0,
                    modifier = Modifier.weight(1f).testTag("batch_export_button"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = "Export VCF",
                        tint = IosBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export VCF", color = IosBlue, fontSize = 14.sp)
                }

                ElevatedButton(
                    onClick = onPurgeSelected,
                    enabled = selectedCount > 0,
                    modifier = Modifier.weight(1f).testTag("batch_purge_button"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = IosRed.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Purge",
                        tint = IosRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Purge ($selectedCount)", color = IosRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
