package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Contact
import com.example.ui.components.IosBottomNav
import com.example.ui.components.PhoneTab
import com.example.ui.components.liquidGlass
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.ContactEditDialog
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.KeypadScreen
import com.example.ui.screens.PurgeConfirmDialog
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.IosGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PhoneViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: PhoneViewModel = viewModel()) {
    val context = LocalContext.current

    // Request READ & WRITE CONTACTS permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] == true
        if (readGranted) {
            viewModel.refresh(context)
            viewModel.showBanner("Device Contacts connected successfully!")
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
        )
    }

    // State collections
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val recents by viewModel.recents.collectAsStateWithLifecycle()
    val recentsFilter by viewModel.recentsFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val keypadInput by viewModel.keypadInput.collectAsStateWithLifecycle()
    val isDeviceConnected by viewModel.isDeviceConnected.collectAsStateWithLifecycle()

    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()
    val editingContact by viewModel.editingContact.collectAsStateWithLifecycle()
    val isAddContactOpen by viewModel.isAddContactOpen.collectAsStateWithLifecycle()

    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val callDuration by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val isCallMuted by viewModel.isCallMuted.collectAsStateWithLifecycle()
    val isCallSpeaker by viewModel.isCallSpeaker.collectAsStateWithLifecycle()

    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedContactIds by viewModel.selectedContactIds.collectAsStateWithLifecycle()
    val purgeDialogType by viewModel.purgeDialogType.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (selectedContact == null && activeCall == null) {
                    IosBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    PhoneTab.FAVORITES -> {
                        FavoritesScreen(
                            favorites = favorites,
                            onContactClick = { viewModel.selectContact(it) },
                            onCallClick = { number, name ->
                                viewModel.startCall(context, number, name)
                            },
                            onNavigateToContacts = {
                                viewModel.selectTab(PhoneTab.CONTACTS)
                            }
                        )
                    }
                    PhoneTab.RECENTS -> {
                        RecentsScreen(
                            recents = recents,
                            activeFilter = recentsFilter,
                            onFilterChange = { viewModel.setRecentsFilter(it) },
                            onClearAll = { viewModel.clearRecents() },
                            onCallClick = { number, name ->
                                viewModel.startCall(context, number, name)
                            },
                            onInfoClick = { number, name ->
                                val found = allContacts.firstOrNull { c ->
                                    c.phoneNumbers.any { p -> p.value == number }
                                } ?: Contact(
                                    id = java.util.UUID.randomUUID().toString(),
                                    displayName = name,
                                    phoneNumbers = listOf(com.example.model.ContactDetail("mobile", number))
                                )
                                viewModel.selectContact(found)
                            }
                        )
                    }
                    PhoneTab.CONTACTS -> {
                        ContactsScreen(
                            contacts = filteredContacts,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onContactClick = { viewModel.selectContact(it) },
                            onAddContactClick = { viewModel.openAddContact() },
                            isSelectionMode = isSelectionMode,
                            selectedContactIds = selectedContactIds,
                            onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                            onToggleContactSelection = { viewModel.toggleContactSelection(it) },
                            onSelectAll = { viewModel.selectAllContacts() },
                            onDeselectAll = { viewModel.deselectAllContacts() },
                            onPurgeSelected = {
                                viewModel.openPurgeDialog(com.example.viewmodel.PurgeDialogType.PURGE_SELECTED)
                            },
                            onExportSelected = {
                                val selectedList = allContacts.filter { it.id in selectedContactIds }
                                viewModel.shareContactsAsVcf(context, selectedList)
                            },
                            isDeviceConnected = isDeviceConnected
                        )
                    }
                    PhoneTab.KEYPAD -> {
                        KeypadScreen(
                            inputNumber = keypadInput,
                            onDigitClick = { viewModel.onKeypadDigit(it) },
                            onBackspaceClick = { viewModel.onKeypadBackspace() },
                            onClearAll = { viewModel.onKeypadClear() },
                            onCallClick = { useSystemDialer ->
                                viewModel.startCall(context, keypadInput, useSystemDialer = useSystemDialer)
                            },
                            onAddNumberClick = { number ->
                                viewModel.openAddContact(number)
                            }
                        )
                    }
                    PhoneTab.MANAGE -> {
                        ToolsScreen(
                            totalContactsCount = allContacts.size,
                            isDeviceConnected = isDeviceConnected,
                            isProcessing = isProcessing,
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            },
                            onRefresh = { viewModel.refresh(context) },
                            onOpenPurgeDialog = { type -> viewModel.openPurgeDialog(type) },
                            onToggleSelectMode = {
                                viewModel.selectTab(PhoneTab.CONTACTS)
                                if (!isSelectionMode) viewModel.toggleSelectionMode()
                            },
                            onImportUrisSelected = { uris, saveToDevice ->
                                viewModel.importMultipleVcf(context, uris, saveToDevice)
                            },
                            onExportToStream = { stream ->
                                viewModel.exportContactsToStream(stream)
                            },
                            onShareVcf = {
                                viewModel.shareContactsAsVcf(context)
                            }
                        )
                    }
                }
            }
        }

        // Contact Detail Overlay
        selectedContact?.let { contact ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                ContactDetailScreen(
                    contact = contact,
                    onBack = { viewModel.dismissContactDetail() },
                    onEdit = { viewModel.openEditContact(contact) },
                    onDelete = { viewModel.deleteContact(context, contact) },
                    onToggleFavorite = { viewModel.toggleFavorite(contact.id) },
                    onCallClick = { number, name ->
                        viewModel.startCall(context, number, name)
                    },
                    onShareSingleVcf = {
                        viewModel.shareContactsAsVcf(context, listOf(contact))
                    }
                )
            }
        }

        // Add / Edit Contact Modal Dialog
        if (isAddContactOpen || editingContact != null) {
            ContactEditDialog(
                initialContact = editingContact,
                isNewContact = isAddContactOpen,
                onDismiss = {
                    viewModel.dismissAddContact()
                    viewModel.dismissEditContact()
                },
                onSave = { contact, saveToDevice ->
                    viewModel.saveContact(context, contact, saveToDevice)
                }
            )
        }

        // Purge Confirmation Dialog
        purgeDialogType?.let { type ->
            PurgeConfirmDialog(
                type = type,
                selectedCount = selectedContactIds.size,
                totalCount = allContacts.size,
                onDismiss = { viewModel.dismissPurgeDialog() },
                onConfirm = { viewModel.executePurge(context) }
            )
        }

        // Active Call Overlay Screen
        activeCall?.let { call ->
            ActiveCallScreen(
                call = call,
                durationSeconds = callDuration,
                isMuted = isCallMuted,
                isSpeaker = isCallSpeaker,
                onToggleMute = { viewModel.toggleCallMute() },
                onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                onEndCall = { viewModel.endCall() }
            )
        }

        // Top Status Alert Banner (Liquid Glass Pill)
        AnimatedVisibility(
            visible = statusBanner != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp, start = 20.dp, end = 20.dp)
        ) {
            statusBanner?.let { msg ->
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = RoundedCornerShape(26.dp),
                            borderWidth = 1.dp,
                            bgAlpha = 0.35f,
                            borderAlpha = 0.6f
                        )
                        .background(Color(0xD90D1022))
                        .padding(horizontal = 22.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
