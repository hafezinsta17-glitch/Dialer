package com.example.ui.screens

import android.text.format.DateUtils
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.liquidGlass
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary
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

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 76.dp)
        ) {
            // Top Header Bar with Clear button and Segmented Control
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
                        color = if (recents.isNotEmpty()) LiquidCyan else LiquidCyan.copy(alpha = 0.3f),
                        fontSize = 16.sp
                    )
                }

                // Liquid Glass Segmented Control
                LiquidSegmentedControl(
                    filter = activeFilter,
                    onFilterSelected = onFilterChange
                )

                Spacer(modifier = Modifier.width(60.dp))
            }

            Text(
                text = "Recents",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidTextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
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
                                .size(76.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    borderWidth = 1.dp,
                                    bgAlpha = 0.14f,
                                    borderAlpha = 0.35f
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "No Calls",
                                tint = LiquidTextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (activeFilter == RecentsFilter.MISSED) "No Missed Calls" else "No Recent Calls",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidTextPrimary
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
                            color = Color.White.copy(alpha = 0.08f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 54.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidSegmentedControl(
    filter: RecentsFilter,
    onFilterSelected: (RecentsFilter) -> Unit
) {
    Box(
        modifier = Modifier
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                borderWidth = 1.dp,
                bgAlpha = 0.12f,
                borderAlpha = 0.35f
            )
            .padding(3.dp)
    ) {
        Row {
            LiquidSegmentItem(
                title = "All",
                isSelected = filter == RecentsFilter.ALL,
                onClick = { onFilterSelected(RecentsFilter.ALL) }
            )
            LiquidSegmentItem(
                title = "Missed",
                isSelected = filter == RecentsFilter.MISSED,
                onClick = { onFilterSelected(RecentsFilter.MISSED) }
            )
        }
    }
}

@Composable
fun LiquidSegmentItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.liquidGlass(
                        shape = RoundedCornerShape(16.dp),
                        borderWidth = 1.dp,
                        bgAlpha = 0.35f,
                        borderAlpha = 0.60f
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) LiquidTextPrimary else LiquidTextSecondary
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (call.type) {
                CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
            },
            contentDescription = call.type.name,
            tint = if (isMissed) LiquidRed else LiquidCyan,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                fontSize = 16.sp,
                fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Medium,
                color = if (isMissed) LiquidRed else LiquidTextPrimary
            )
            Text(
                text = call.number,
                fontSize = 13.sp,
                color = LiquidTextSecondary
            )
        }

        Text(
            text = timeFormatted,
            fontSize = 13.sp,
            color = LiquidTextSecondary,
            modifier = Modifier.padding(end = 8.dp)
        )

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Call Info",
                tint = LiquidCyan,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
