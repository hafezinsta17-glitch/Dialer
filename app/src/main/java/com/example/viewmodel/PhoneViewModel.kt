package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ContactsRepository
import com.example.data.DeviceContactsHelper
import com.example.data.VcfManager
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.Contact
import com.example.model.ContactDetail
import com.example.ui.components.PhoneTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.UUID

enum class RecentsFilter {
    ALL,
    MISSED
}

enum class PurgeDialogType {
    PURGE_ALL,
    PURGE_SELECTED,
    CLEAN_DUPLICATES
}

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactsRepository

    val allContacts = repository.contacts
    val recents = repository.recents
    val isDeviceConnected = repository.isDeviceConnected

    private val _selectedTab = MutableStateFlow(PhoneTab.CONTACTS)
    val selectedTab: StateFlow<PhoneTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentsFilter = MutableStateFlow(RecentsFilter.ALL)
    val recentsFilter: StateFlow<RecentsFilter> = _recentsFilter.asStateFlow()

    private val _keypadInput = MutableStateFlow("")
    val keypadInput: StateFlow<String> = _keypadInput.asStateFlow()

    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    private val _editingContact = MutableStateFlow<Contact?>(null)
    val editingContact: StateFlow<Contact?> = _editingContact.asStateFlow()

    private val _isAddContactOpen = MutableStateFlow(false)
    val isAddContactOpen: StateFlow<Boolean> = _isAddContactOpen.asStateFlow()

    // Active Call Simulation
    private val _activeCall = MutableStateFlow<CallRecord?>(null)
    val activeCall: StateFlow<CallRecord?> = _activeCall.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isCallSpeaker = MutableStateFlow(false)
    val isCallSpeaker: StateFlow<Boolean> = _isCallSpeaker.asStateFlow()

    private var callTimerJob: Job? = null

    // Purge & Multi-selection
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedContactIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedContactIds: StateFlow<Set<String>> = _selectedContactIds.asStateFlow()

    private val _purgeDialogType = MutableStateFlow<PurgeDialogType?>(null)
    val purgeDialogType: StateFlow<PurgeDialogType?> = _purgeDialogType.asStateFlow()

    // Import/Export status
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Filtered Contacts Flow
    val filteredContacts: StateFlow<List<Contact>> = combine(allContacts, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.lowercase().trim()
            list.filter { contact ->
                contact.displayName.lowercase().contains(q) ||
                contact.phoneNumbers.any { it.value.contains(q) } ||
                contact.company.lowercase().contains(q) ||
                contact.emails.any { it.value.lowercase().contains(q) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites Flow
    val favoriteContacts: StateFlow<List<Contact>> = allContacts.combine(searchQuery) { list, _ ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh(getApplication())
    }

    fun refresh(context: Context) {
        viewModelScope.launch {
            repository.refreshContacts(context)
        }
    }

    fun selectTab(tab: PhoneTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setRecentsFilter(filter: RecentsFilter) {
        _recentsFilter.value = filter
    }

    fun clearRecents() {
        repository.clearRecents()
    }

    // Keypad Logic
    fun onKeypadDigit(digit: String) {
        _keypadInput.value += digit
    }

    fun onKeypadBackspace() {
        val current = _keypadInput.value
        if (current.isNotEmpty()) {
            _keypadInput.value = current.dropLast(1)
        }
    }

    fun onKeypadClear() {
        _keypadInput.value = ""
    }

    // Call Actions
    fun startCall(context: Context, number: String, name: String = "", useSystemDialer: Boolean = false) {
        if (number.isBlank()) return

        val displayName = if (name.isNotBlank()) name else {
            allContacts.value.firstOrNull { it.phoneNumbers.any { p -> p.value == number } }?.displayName ?: number
        }

        if (useSystemDialer) {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                repository.recordCall(displayName, number, CallType.OUTGOING, 0)
            } catch (e: Exception) {
                // fallback to simulated call
                launchSimulatedCall(displayName, number)
            }
        } else {
            launchSimulatedCall(displayName, number)
        }
    }

    private fun launchSimulatedCall(name: String, number: String) {
        _callDurationSeconds.value = 0
        _isCallMuted.value = false
        _isCallSpeaker.value = false

        val call = CallRecord(
            id = UUID.randomUUID().toString(),
            name = name,
            number = number,
            type = CallType.OUTGOING,
            timestamp = System.currentTimeMillis()
        )
        _activeCall.value = call

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }
    }

    fun endCall() {
        val call = _activeCall.value
        val duration = _callDurationSeconds.value
        callTimerJob?.cancel()
        callTimerJob = null
        _activeCall.value = null

        if (call != null) {
            repository.recordCall(call.name, call.number, CallType.OUTGOING, duration)
        }
    }

    fun toggleCallMute() {
        _isCallMuted.value = !_isCallMuted.value
    }

    fun toggleCallSpeaker() {
        _isCallSpeaker.value = !_isCallSpeaker.value
    }

    // Contact Details & Editing
    fun selectContact(contact: Contact) {
        _selectedContact.value = contact
    }

    fun dismissContactDetail() {
        _selectedContact.value = null
    }

    fun openEditContact(contact: Contact) {
        _editingContact.value = contact
    }

    fun dismissEditContact() {
        _editingContact.value = null
    }

    fun openAddContact(prefillNumber: String = "") {
        _editingContact.value = Contact(
            id = UUID.randomUUID().toString(),
            displayName = "",
            phoneNumbers = if (prefillNumber.isNotBlank()) listOf(ContactDetail("mobile", prefillNumber)) else emptyList()
        )
        _isAddContactOpen.value = true
    }

    fun dismissAddContact() {
        _isAddContactOpen.value = false
        _editingContact.value = null
    }

    fun saveContact(context: Context, contact: Contact, saveToDevice: Boolean) {
        viewModelScope.launch {
            if (_isAddContactOpen.value) {
                repository.addContact(context, contact, saveToDevice)
                showBanner("Contact added: ${contact.displayName}")
                dismissAddContact()
            } else {
                repository.updateContact(context, contact)
                showBanner("Contact updated: ${contact.displayName}")
                _selectedContact.value = contact
                dismissEditContact()
            }
        }
    }

    fun deleteContact(context: Context, contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(context, contact)
            showBanner("Deleted ${contact.displayName}")
            dismissContactDetail()
        }
    }

    fun toggleFavorite(contactId: String) {
        repository.toggleFavorite(contactId)
        val current = _selectedContact.value
        if (current?.id == contactId) {
            _selectedContact.value = current.copy(isFavorite = !current.isFavorite)
        }
    }

    // Purge Features
    fun openPurgeDialog(type: PurgeDialogType) {
        _purgeDialogType.value = type
    }

    fun dismissPurgeDialog() {
        _purgeDialogType.value = null
    }

    fun executePurge(context: Context) {
        val type = _purgeDialogType.value ?: return
        dismissPurgeDialog()

        viewModelScope.launch {
            _isProcessing.value = true
            when (type) {
                PurgeDialogType.PURGE_ALL -> {
                    val count = repository.purgeAllContacts(context)
                    showBanner("Purged all $count contacts successfully!")
                    exitSelectionMode()
                }
                PurgeDialogType.PURGE_SELECTED -> {
                    val count = repository.purgeSelectedContacts(context, _selectedContactIds.value)
                    showBanner("Purged $count selected contacts!")
                    exitSelectionMode()
                }
                PurgeDialogType.CLEAN_DUPLICATES -> {
                    val count = repository.cleanDuplicates(context)
                    showBanner(if (count > 0) "Removed $count duplicate contacts!" else "No duplicate contacts found.")
                }
            }
            _isProcessing.value = false
        }
    }

    // Selection Mode
    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedContactIds.value = emptySet()
        }
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedContactIds.value = emptySet()
    }

    fun toggleContactSelection(id: String) {
        val current = _selectedContactIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedContactIds.value = current
    }

    fun selectAllContacts() {
        _selectedContactIds.value = allContacts.value.map { it.id }.toSet()
    }

    fun deselectAllContacts() {
        _selectedContactIds.value = emptySet()
    }

    // VCF Import & Export
    fun importMultipleVcf(context: Context, uris: List<Uri>, saveToDevice: Boolean) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _isProcessing.value = true
            val result = VcfManager.importMultipleVcfFiles(context, uris)
            val count = repository.importVcfContacts(context, result.importedContacts, saveToDevice)
            _isProcessing.value = false

            showBanner("Imported $count contacts from ${result.totalFiles} VCF file(s)!")
        }
    }

    fun exportContactsToStream(outputStream: OutputStream, contactsToExport: List<Contact>? = null): Boolean {
        val list = contactsToExport ?: allContacts.value
        val success = VcfManager.exportToOutputStream(outputStream, list)
        if (success) {
            showBanner("Exported ${list.size} contacts to VCF file!")
        }
        return success
    }

    fun shareContactsAsVcf(context: Context, contactsToShare: List<Contact>? = null) {
        val list = contactsToShare ?: allContacts.value
        if (list.isEmpty()) {
            showBanner("No contacts to share")
            return
        }

        viewModelScope.launch {
            val fileName = if (list.size == 1) {
                "${list[0].displayName.replace(" ", "_")}.vcf"
            } else {
                "contacts_${System.currentTimeMillis()}.vcf"
            }

            val file = VcfManager.createShareableVcfFile(context, list, fileName)
            if (file != null) {
                try {
                    val authority = "${context.packageName}.fileprovider"
                    val contentUri = FileProvider.getUriForFile(context, authority, file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/x-vcard"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(shareIntent, "Share Contacts via VCF").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } catch (e: Exception) {
                    showBanner("Failed to share VCF: ${e.message}")
                }
            } else {
                showBanner("Could not generate shareable VCF file")
            }
        }
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
        viewModelScope.launch {
            delay(3500)
            if (_statusBanner.value == message) {
                _statusBanner.value = null
            }
        }
    }
}
