package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosRed
import com.example.viewmodel.RecentsFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    recents: List<CallRecord>,
    activeFilter: RecentsFilter,
    onFilterChange: (RecentsFilter) -> Unit,
    onClearAll: () -> Unit,
    onCallClick: (number: String, name: String) -> Unit,
    onInfoClick: (number: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = remember(recents, activeFilter) {
        if (activeFilter == RecentsFilter.ALL) recents else recents.filter { it.type == CallType.MISSED }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Header Bar with Clear button and All / Missed segmented pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClearAll,
                enabled = recents.isNotEmpty(),
                modifier = Modifier.testTag("clear_recents_button")
            ) {
                Text(
                    text = "Clear",
                    color = if (recents.isNotEmpty()) IosBlue else IosBlue.copy(alpha = 0.3f),
                    fontSize = 17.sp
                )
            }

            // iOS Segmented Control
            SegmentedControl(
                filter = activeFilter,
                onFilterSelected = onFilterChange
            )

            // Balance placeholder
            Spacer(modifier = Modifier.width(60.dp))
        }

        Text(
            text = "Recents",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "No Calls",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (activeFilter == RecentsFilter.MISSED) "No Missed Calls" else "No Recent Calls",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList, key = { it.id }) { call ->
                    RecentCallRow(
                        call = call,
                        onClick = { onCallClick(call.number, call.name) },
                        onInfoClick = { onInfoClick(call.number, call.name) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SegmentedControl(
    filter: RecentsFilter,
    onFilterSelected: (RecentsFilter) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
    ) {
        Row {
            SegmentItem(
                title = "All",
                isSelected = filter == RecentsFilter.ALL,
                onClick = { onFilterSelected(RecentsFilter.ALL) }
            )
            SegmentItem(
                title = "Missed",
                isSelected = filter == RecentsFilter.MISSED,
                onClick = { onFilterSelected(RecentsFilter.MISSED) }
            )
        }
    }
}

@Composable
fun SegmentItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecentCallRow(
    call: CallRecord,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val isMissed = call.type == CallType.MISSED
    val timeFormatted = remember(call.timestamp) {
        val now = System.currentTimeMillis()
        if (DateUtils.isToday(call.timestamp)) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(call.timestamp))
        } else {
            SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(call.timestamp))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Arrow status indicator
        Icon(
            imageVector = when (call.type) {
                CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
            },
            contentDescription = call.type.name,
            tint = if (isMissed) IosRed else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                fontSize = 17.sp,
                fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Normal,
                color = if (isMissed) IosRed else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = call.number,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = timeFormatted,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Call Info",
                tint = IosBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
