package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.Contact
import com.example.model.ContactDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

object ContactsRepository {
    private const val TAG = "ContactsRepository"

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _recents = MutableStateFlow<List<CallRecord>>(emptyList())
    val recents: StateFlow<List<CallRecord>> = _recents.asStateFlow()

    private val _isDeviceConnected = MutableStateFlow(false)
    val isDeviceConnected: StateFlow<Boolean> = _isDeviceConnected.asStateFlow()

    // In-memory local contacts (e.g. imported or user-added when write permission is not granted)
    private val localContacts = mutableListOf<Contact>()

    init {
        // Initialize with default sample recents
        _recents.value = generateDefaultRecents()
    }

    suspend fun refreshContacts(context: Context) = withContext(Dispatchers.IO) {
        val hasPerm = DeviceContactsHelper.hasReadPermission(context)
        _isDeviceConnected.value = hasPerm

        if (hasPerm) {
            val deviceList = DeviceContactsHelper.fetchDeviceContacts(context)
            // Combine with local contacts not in device
            val combined = (deviceList + localContacts).distinctBy {
                "${it.displayName.lowercase().trim()}_${it.primaryPhone.filter { ch -> ch.isDigit() }}"
            }.sortedBy { it.displayName.lowercase() }

            _contacts.value = combined
        } else {
            // If permission is not yet granted, show local/sample contacts so UI is immediately useful
            if (localContacts.isEmpty()) {
                localContacts.addAll(generateSampleContacts())
            }
            _contacts.value = localContacts.sortedBy { it.displayName.lowercase() }
        }
    }

    suspend fun addContact(context: Context, contact: Contact, saveToDevice: Boolean = true) = withContext(Dispatchers.IO) {
        var addedToDevice = false
        if (saveToDevice && DeviceContactsHelper.hasWritePermission(context)) {
            addedToDevice = DeviceContactsHelper.insertContactToDevice(context, contact)
        }

        if (addedToDevice) {
            refreshContacts(context)
        } else {
            val newContact = contact.copy(isDeviceContact = false)
            localContacts.add(newContact)
            val current = _contacts.value.toMutableList()
            current.add(newContact)
            _contacts.value = current.sortedBy { it.displayName.lowercase() }
        }
    }

    suspend fun updateContact(context: Context, contact: Contact) = withContext(Dispatchers.IO) {
        val current = _contacts.value.toMutableList()
        val index = current.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            current[index] = contact
            _contacts.value = current.sortedBy { it.displayName.lowercase() }
        }
        val localIndex = localContacts.indexOfFirst { it.id == contact.id }
        if (localIndex != -1) {
            localContacts[localIndex] = contact
        }
    }

    suspend fun deleteContact(context: Context, contact: Contact) = withContext(Dispatchers.IO) {
        if (contact.isDeviceContact && DeviceContactsHelper.hasWritePermission(context)) {
            DeviceContactsHelper.deleteSingleContact(context, contact)
        }
        localContacts.removeAll { it.id == contact.id }
        _contacts.value = _contacts.value.filterNot { it.id == contact.id }
    }

    suspend fun purgeAllContacts(context: Context): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        if (DeviceContactsHelper.hasWritePermission(context)) {
            deletedCount = DeviceContactsHelper.purgeAllDeviceContacts(context)
        }
        deletedCount += localContacts.size
        localContacts.clear()
        _contacts.value = emptyList()
        deletedCount
    }

    suspend fun purgeSelectedContacts(context: Context, contactIds: Set<String>): Int = withContext(Dispatchers.IO) {
        val toDelete = _contacts.value.filter { it.id in contactIds }
        val deviceIds = toDelete.filter { it.isDeviceContact }.map { it.id }

        var deleted = 0
        if (deviceIds.isNotEmpty() && DeviceContactsHelper.hasWritePermission(context)) {
            deleted += DeviceContactsHelper.purgeSelectedContacts(context, deviceIds)
        }

        localContacts.removeAll { it.id in contactIds }
        deleted += (toDelete.size - deviceIds.size)

        _contacts.value = _contacts.value.filterNot { it.id in contactIds }
        deleted
    }

    suspend fun cleanDuplicates(context: Context): Int = withContext(Dispatchers.IO) {
        val current = _contacts.value
        val seen = mutableSetOf<String>()
        val duplicates = mutableSetOf<String>()

        for (c in current) {
            val key = "${c.displayName.lowercase().trim()}_${c.primaryPhone.filter { it.isDigit() }}"
            if (!seen.add(key)) {
                duplicates.add(c.id)
            }
        }

        if (duplicates.isNotEmpty()) {
            purgeSelectedContacts(context, duplicates)
        } else {
            0
        }
    }

    fun toggleFavorite(contactId: String) {
        _contacts.value = _contacts.value.map {
            if (it.id == contactId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    suspend fun importVcfContacts(
        context: Context,
        newContacts: List<Contact>,
        saveToDevice: Boolean
    ): Int = withContext(Dispatchers.IO) {
        var savedToDeviceCount = 0
        val canWrite = DeviceContactsHelper.hasWritePermission(context)

        for (c in newContacts) {
            if (saveToDevice && canWrite) {
                if (DeviceContactsHelper.insertContactToDevice(context, c)) {
                    savedToDeviceCount++
                }
            } else {
                localContacts.add(c)
            }
        }

        if (savedToDeviceCount > 0) {
            refreshContacts(context)
        } else {
            val combined = (_contacts.value + newContacts).distinctBy {
                "${it.displayName.lowercase().trim()}_${it.primaryPhone.filter { ch -> ch.isDigit() }}"
            }.sortedBy { it.displayName.lowercase() }
            _contacts.value = combined
        }

        newContacts.size
    }

    fun recordCall(name: String, number: String, type: CallType, durationSeconds: Int = 0) {
        val newCall = CallRecord(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { number },
            number = number,
            type = type,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        val updated = listOf(newCall) + _recents.value
        _recents.value = updated.take(100)
    }

    fun clearRecents() {
        _recents.value = emptyList()
    }

    private fun generateDefaultRecents(): List<CallRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            CallRecord(
                id = "c1",
                name = "Emma Watson",
                number = "+1 (555) 234-5678",
                type = CallType.OUTGOING,
                timestamp = now - 1000 * 60 * 12, // 12 mins ago
                durationSeconds = 142
            ),
            CallRecord(
                id = "c2",
                name = "Dr. Michael Chen",
                number = "+1 (555) 890-1234",
                type = CallType.MISSED,
                timestamp = now - 1000 * 60 * 60 * 2, // 2 hours ago
                durationSeconds = 0
            ),
            CallRecord(
                id = "c3",
                name = "Sophia Miller",
                number = "+1 (555) 345-6789",
                type = CallType.INCOMING,
                timestamp = now - 1000 * 60 * 60 * 5, // 5 hours ago
                durationSeconds = 310
            ),
            CallRecord(
                id = "c4",
                name = "Alexander Wright",
                number = "+1 (555) 123-4567",
                type = CallType.OUTGOING,
                timestamp = now - 1000 * 60 * 60 * 24, // Yesterday
                durationSeconds = 58
            ),
            CallRecord(
                id = "c5",
                name = "Unknown Caller",
                number = "+1 (800) 555-0199",
                type = CallType.MISSED,
                timestamp = now - 1000 * 60 * 60 * 36,
                durationSeconds = 0
            )
        )
    }

    private fun generateSampleContacts(): List<Contact> {
        return listOf(
            Contact(
                id = "s1",
                displayName = "Alexander Wright",
                firstName = "Alexander",
                lastName = "Wright",
                phoneNumbers = listOf(
                    ContactDetail("mobile", "+1 (555) 123-4567"),
                    ContactDetail("work", "+1 (555) 987-6543")
                ),
                emails = listOf(ContactDetail("work", "alex.wright@techinnovate.com")),
                company = "Tech Innovations",
                jobTitle = "Lead Architect",
                isFavorite = true,
                avatarColorIndex = 0
            ),
            Contact(
                id = "s2",
                displayName = "Benjamin Carter",
                firstName = "Benjamin",
                lastName = "Carter",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 234-9876")),
                emails = listOf(ContactDetail("home", "bcarter@gmail.com")),
                company = "Skyline Media",
                jobTitle = "Creative Director",
                isFavorite = false,
                avatarColorIndex = 1
            ),
            Contact(
                id = "s3",
                displayName = "Charlotte Davis",
                firstName = "Charlotte",
                lastName = "Davis",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 876-5432")),
                emails = listOf(ContactDetail("work", "cdavis@designhub.io")),
                company = "Design Hub",
                jobTitle = "Product Designer",
                isFavorite = true,
                avatarColorIndex = 2
            ),
            Contact(
                id = "s4",
                displayName = "Dr. Michael Chen",
                firstName = "Michael",
                lastName = "Chen",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 890-1234")),
                emails = listOf(ContactDetail("work", "dr.chen@cityclinic.org")),
                company = "Metro City Clinic",
                jobTitle = "Cardiologist",
                isFavorite = true,
                avatarColorIndex = 3
            ),
            Contact(
                id = "s5",
                displayName = "Emma Watson",
                firstName = "Emma",
                lastName = "Watson",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 234-5678")),
                emails = listOf(ContactDetail("home", "emma.w@icloud.com")),
                company = "Arts & Culture Foundation",
                jobTitle = "Ambassador",
                isFavorite = true,
                avatarColorIndex = 4
            ),
            Contact(
                id = "s6",
                displayName = "Liam Johnson",
                firstName = "Liam",
                lastName = "Johnson",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 456-7890")),
                emails = listOf(ContactDetail("work", "liam.j@globalconsult.com")),
                company = "Global Consulting Group",
                jobTitle = "Senior Partner",
                isFavorite = false,
                avatarColorIndex = 5
            ),
            Contact(
                id = "s7",
                displayName = "Lucas Scott",
                firstName = "Lucas",
                lastName = "Scott",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 678-9012")),
                emails = listOf(ContactDetail("home", "lscott@outlook.com")),
                company = "Tree Hill Athletics",
                jobTitle = "Coach",
                isFavorite = false,
                avatarColorIndex = 6
            ),
            Contact(
                id = "s8",
                displayName = "Noah Parker",
                firstName = "Noah",
                lastName = "Parker",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 789-0123")),
                emails = listOf(ContactDetail("work", "noah@parkercapital.com")),
                company = "Parker Capital",
                jobTitle = "Managing Director",
                isFavorite = false,
                avatarColorIndex = 7
            ),
            Contact(
                id = "s9",
                displayName = "Olivia Martinez",
                firstName = "Olivia",
                lastName = "Martinez",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 567-8901")),
                emails = listOf(ContactDetail("home", "omartinez@studio.com")),
                company = "Vivid Studios",
                jobTitle = "Film Producer",
                isFavorite = false,
                avatarColorIndex = 0
            ),
            Contact(
                id = "s10",
                displayName = "Sophia Miller",
                firstName = "Sophia",
                lastName = "Miller",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 345-6789")),
                emails = listOf(ContactDetail("home", "sophia.m@me.com")),
                company = "Horizon Media",
                jobTitle = "Marketing VP",
                isFavorite = true,
                avatarColorIndex = 1
            ),
            Contact(
                id = "s11",
                displayName = "William Taylor",
                firstName = "William",
                lastName = "Taylor",
                phoneNumbers = listOf(ContactDetail("mobile", "+1 (555) 901-2345")),
                emails = listOf(ContactDetail("work", "william.taylor@venture.org")),
                company = "Taylor Ventures",
                jobTitle = "Founding Member",
                isFavorite = false,
                avatarColorIndex = 2
            )
        )
    }
}
