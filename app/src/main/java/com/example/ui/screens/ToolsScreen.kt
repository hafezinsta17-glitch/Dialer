package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed
import com.example.viewmodel.PurgeDialogType
import java.io.OutputStream

@Composable
fun ToolsScreen(
    totalContactsCount: Int,
    isDeviceConnected: Boolean,
    isProcessing: Boolean,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onOpenPurgeDialog: (PurgeDialogType) -> Unit,
    onToggleSelectMode: () -> Unit,
    onImportUrisSelected: (List<Uri>, saveToDevice: Boolean) -> Unit,
    onExportToStream: (OutputStream) -> Boolean,
    onShareVcf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var saveImportToDevice by remember { mutableStateOf(true) }

    // Multi-VCF file picker launcher
    val multipleVcfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportUrisSelected(uris, saveImportToDevice)
        }
    }

    // Export to single VCF document launcher
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-vcard")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    onExportToStream(stream)
                }
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Manage Contacts",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Device sync, contact purger & VCF import/export",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Processing progress banner
        if (isProcessing) {
            Card(
                colors = CardDefaults.cardColors(containerColor = IosBlue.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = IosBlue,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Processing contacts operation...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = IosBlue
                    )
                }
            }
        }

        // Section 1: Device Connection Status
        SectionHeaderTitle("DEVICE CONNECTION")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingRow(
                    icon = Icons.Default.PhoneAndroid,
                    iconTint = if (isDeviceConnected) IosGreen else IosOrange,
                    title = "Android Contacts Provider",
                    subtitle = if (isDeviceConnected) "Connected ($totalContactsCount contacts)" else "Permission Required",
                    action = {
                        if (!isDeviceConnected) {
                            Text(
                                text = "Grant",
                                color = IosBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable(onClick = onRequestPermission)
                                    .testTag("grant_permission_button")
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = IosBlue,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(onClick = onRefresh)
                                    .testTag("refresh_device_contacts_button")
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: Contact Purge Tools (USER REQUEST: "like purg phone contact")
        SectionHeaderTitle("PURGE & CLEAN CONTACTS")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Purge All Contacts
                SettingRow(
                    icon = Icons.Default.DeleteForever,
                    iconTint = IosRed,
                    title = "Purge All Phone Contacts",
                    subtitle = "Completely wipe contacts from phone",
                    onClick = { onOpenPurgeDialog(PurgeDialogType.PURGE_ALL) },
                    testTag = "purge_all_button"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 56.dp)
                )

                // Select Multiple Contacts to Purge
                SettingRow(
                    icon = Icons.Default.GroupRemove,
                    iconTint = IosOrange,
                    title = "Select Contacts to Purge",
                    subtitle = "Choose multiple contacts to batch delete",
                    onClick = onToggleSelectMode,
                    testTag = "select_to_purge_button"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 56.dp)
                )

                // Clean Duplicates
                SettingRow(
                    icon = Icons.Default.CleaningServices,
                    iconTint = IosBlue,
                    title = "Clean Duplicate Contacts",
                    subtitle = "Find and remove duplicate names/numbers",
                    onClick = { onOpenPurgeDialog(PurgeDialogType.CLEAN_DUPLICATES) },
                    testTag = "clean_duplicates_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 3: Select Multiple VCF Import (USER REQUEST: "Select multiple vcf file import")
        SectionHeaderTitle("IMPORT FROM VCF (VCARD)")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingRow(
                    icon = Icons.Default.FileUpload,
                    iconTint = IosBlue,
                    title = "Select Multiple VCF Files",
                    subtitle = "Import contacts from one or more .vcf files",
                    onClick = {
                        multipleVcfPickerLauncher.launch(
                            arrayOf("text/x-vcard", "text/vcard", "*/*")
                        )
                    },
                    testTag = "import_vcf_button"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 56.dp)
                )

                // Save to device contacts toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save to Android Device Contacts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Write imported vCards directly to device address book",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = saveImportToDevice,
                        onCheckedChange = { saveImportToDevice = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IosGreen
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 4: Export to VCF (USER REQUEST: "Export to vcf.")
        SectionHeaderTitle("EXPORT TO VCF (VCARD)")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingRow(
                    icon = Icons.Default.FileDownload,
                    iconTint = IosGreen,
                    title = "Export to .vcf File",
                    subtitle = "Save all $totalContactsCount contacts to a .vcf file",
                    onClick = {
                        val fileName = "contacts_backup_${System.currentTimeMillis()}.vcf"
                        exportDocumentLauncher.launch(fileName)
                    },
                    testTag = "export_vcf_button"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 56.dp)
                )

                SettingRow(
                    icon = Icons.Default.IosShare,
                    iconTint = IosBlue,
                    title = "Share Contacts VCF",
                    subtitle = "Send address book via Email, Drive or Chat",
                    onClick = onShareVcf,
                    testTag = "share_vcf_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionHeaderTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
