package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GroupRemove
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.liquidGlass
import com.example.ui.theme.IosOrange
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidEmerald
import com.example.ui.theme.LiquidPurple
import com.example.ui.theme.LiquidRed
import com.example.ui.theme.LiquidTextPrimary
import com.example.ui.theme.LiquidTextSecondary
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

    val multipleVcfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportUrisSelected(uris, saveImportToDevice)
        }
    }

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

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 76.dp)
        ) {
            Text(
                text = "Manage",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidTextPrimary
            )
            Text(
                text = "iOS 26 Liquid Contact Engine & Purge Suite",
                fontSize = 14.sp,
                color = LiquidCyan.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Processing progress banner
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .liquidGlass(
                            shape = RoundedCornerShape(18.dp),
                            borderWidth = 1.dp,
                            bgAlpha = 0.22f,
                            borderAlpha = 0.6f
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = LiquidCyan,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Liquid engine processing contacts...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LiquidCyan
                        )
                    }
                }
            }

            // Section 1: Device Connection Status
            SectionHeaderTitle("DEVICE CONNECTION")
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingRow(
                    icon = Icons.Default.PhoneAndroid,
                    iconTint = if (isDeviceConnected) LiquidEmerald else IosOrange,
                    title = "Android Contacts Provider",
                    subtitle = if (isDeviceConnected) "Synchronized ($totalContactsCount contacts)" else "Permission Required",
                    action = {
                        if (!isDeviceConnected) {
                            Text(
                                text = "Grant",
                                color = LiquidCyan,
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
                                tint = LiquidCyan,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable(onClick = onRefresh)
                                    .testTag("refresh_device_contacts_button")
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Contact Purge Tools
            SectionHeaderTitle("PURGE & CLEAN CONTACTS")
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingRow(
                        icon = Icons.Default.DeleteForever,
                        iconTint = LiquidRed,
                        title = "Purge All Phone Contacts",
                        subtitle = "Wipe contacts permanently with dual confirmation",
                        onClick = { onOpenPurgeDialog(PurgeDialogType.PURGE_ALL) },
                        testTag = "purge_all_button"
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 58.dp)
                    )

                    SettingRow(
                        icon = Icons.Default.GroupRemove,
                        iconTint = IosOrange,
                        title = "Select Contacts to Purge",
                        subtitle = "Multi-select contacts to batch purge",
                        onClick = onToggleSelectMode,
                        testTag = "select_to_purge_button"
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 58.dp)
                    )

                    SettingRow(
                        icon = Icons.Default.CleaningServices,
                        iconTint = LiquidCyan,
                        title = "Clean Duplicate Contacts",
                        subtitle = "Identify and purge duplicate numbers/names",
                        onClick = { onOpenPurgeDialog(PurgeDialogType.CLEAN_DUPLICATES) },
                        testTag = "clean_duplicates_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Select Multiple VCF Import
            SectionHeaderTitle("IMPORT FROM VCF (VCARD)")
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingRow(
                        icon = Icons.Default.FileUpload,
                        iconTint = LiquidPurple,
                        title = "Select Multiple VCF Files",
                        subtitle = "Import and merge from multiple .vcf files",
                        onClick = {
                            multipleVcfPickerLauncher.launch(
                                arrayOf("text/x-vcard", "text/vcard", "*/*")
                            )
                        },
                        testTag = "import_vcf_button"
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 58.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Save to Android Device Contacts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = LiquidTextPrimary
                            )
                            Text(
                                text = "Directly writes into system Contacts Provider",
                                fontSize = 12.sp,
                                color = LiquidTextSecondary
                            )
                        }
                        Switch(
                            checked = saveImportToDevice,
                            onCheckedChange = { saveImportToDevice = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = LiquidEmerald
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Export to VCF
            SectionHeaderTitle("EXPORT TO VCF (VCARD)")
            LiquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingRow(
                        icon = Icons.Default.FileDownload,
                        iconTint = LiquidEmerald,
                        title = "Export to .vcf File",
                        subtitle = "Export all $totalContactsCount contacts to a single file",
                        onClick = {
                            val fileName = "contacts_backup_${System.currentTimeMillis()}.vcf"
                            exportDocumentLauncher.launch(fileName)
                        },
                        testTag = "export_vcf_button"
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 58.dp)
                    )

                    SettingRow(
                        icon = Icons.Default.IosShare,
                        iconTint = LiquidCyan,
                        title = "Share Contacts VCF",
                        subtitle = "Send address book via Email, Drive or Chat",
                        onClick = onShareVcf,
                        testTag = "share_vcf_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeaderTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = LiquidCyan.copy(alpha = 0.85f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 14.dp, bottom = 8.dp)
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.18f))
                .border(
                    width = 1.dp,
                    color = iconTint.copy(alpha = 0.45f),
                    shape = CircleShape
                ),
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
                color = LiquidTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = LiquidTextSecondary
            )
        }

        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
