package com.example.data

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.Contact
import com.example.model.ContactDetail
import kotlin.math.abs

object DeviceContactsHelper {
    private const val TAG = "DeviceContactsHelper"

    fun hasReadPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWritePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllPermissions(context: Context): Boolean {
        return hasReadPermission(context) && hasWritePermission(context)
    }

    /**
     * Reads all device contacts using ContentResolver.
     */
    fun fetchDeviceContacts(context: Context): List<Contact> {
        if (!hasReadPermission(context)) {
            Log.w(TAG, "Read contacts permission not granted")
            return emptyList()
        }

        val contactsMap = mutableMapOf<String, MutableContactData>()

        // 1. Fetch Phone numbers & base contact info
        val phoneProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )

        try {
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            phoneCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val lookupIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val labelIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
                val starIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val lookup = cursor.getString(lookupIdx) ?: ""
                    val name = cursor.getString(nameIdx) ?: "Unknown"
                    val number = cursor.getString(numIdx) ?: ""
                    val type = cursor.getInt(typeIdx)
                    val customLabel = cursor.getString(labelIdx)
                    val isStarred = (cursor.getInt(starIdx) == 1)

                    val label = when (type) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "mobile"
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "home"
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "work"
                        ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "main"
                        ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> customLabel ?: "custom"
                        else -> "other"
                    }

                    val entry = contactsMap.getOrPut(id) {
                        MutableContactData(
                            id = id,
                            lookupKey = lookup,
                            displayName = name,
                            isStarred = isStarred
                        )
                    }
                    if (number.isNotBlank()) {
                        entry.phoneNumbers.add(ContactDetail(label = label, value = number.trim()))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying phone contacts: ${e.message}", e)
        }

        // 2. Fetch Emails
        val emailProjection = arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTACT_ID,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Email.TYPE,
            ContactsContract.CommonDataKinds.Email.LABEL
        )

        try {
            val emailCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null
            )

            emailCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                val dataIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                val typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
                val labelIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val email = cursor.getString(dataIdx) ?: continue
                    val type = cursor.getInt(typeIdx)
                    val customLabel = cursor.getString(labelIdx)

                    val label = when (type) {
                        ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "home"
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "work"
                        ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM -> customLabel ?: "custom"
                        else -> "email"
                    }

                    contactsMap[id]?.emails?.add(ContactDetail(label = label, value = email.trim()))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying emails: ${e.message}", e)
        }

        // 3. Convert to Contact objects
        return contactsMap.values.map { data ->
            val colorIndex = abs(data.displayName.hashCode()) % 8
            Contact(
                id = data.id,
                lookupKey = data.lookupKey,
                displayName = data.displayName,
                phoneNumbers = data.phoneNumbers.distinctBy { it.value },
                emails = data.emails.distinctBy { it.value },
                isFavorite = data.isStarred,
                avatarColorIndex = colorIndex,
                isDeviceContact = true
            )
        }.sortedBy { it.displayName.lowercase() }
    }

    /**
     * Purges ALL contacts from the device Contacts Provider.
     * Returns the number of raw contacts deleted.
     */
    fun purgeAllDeviceContacts(context: Context): Int {
        if (!hasWritePermission(context)) {
            Log.w(TAG, "Write contacts permission missing for purge")
            return 0
        }

        return try {
            val deleted = context.contentResolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                null,
                null
            )
            Log.i(TAG, "Purged $deleted raw contacts from device")
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Failed to purge device contacts: ${e.message}", e)
            0
        }
    }

    /**
     * Purges a list of contacts by IDs from the device.
     */
    fun purgeSelectedContacts(context: Context, contactIds: List<String>): Int {
        if (!hasWritePermission(context) || contactIds.isEmpty()) return 0

        var deletedCount = 0
        for (id in contactIds) {
            try {
                val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                val count = context.contentResolver.delete(uri, null, null)
                if (count > 0) deletedCount += count
            } catch (e: Exception) {
                // Try deleting raw contacts directly
                try {
                    val rawCount = context.contentResolver.delete(
                        ContactsContract.RawContacts.CONTENT_URI,
                        "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                        arrayOf(id)
                    )
                    deletedCount += rawCount
                } catch (e2: Exception) {
                    Log.e(TAG, "Error deleting contact $id: ${e2.message}")
                }
            }
        }
        return deletedCount
    }

    /**
     * Deletes a single contact from the device.
     */
    fun deleteSingleContact(context: Context, contact: Contact): Boolean {
        if (!hasWritePermission(context)) return false
        return try {
            val id = contact.id.toLongOrNull()
            if (id != null) {
                val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
                val count = context.contentResolver.delete(uri, null, null)
                count > 0
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete contact: ${e.message}")
            false
        }
    }

    /**
     * Inserts a contact into the Android Contacts Provider.
     */
    fun insertContactToDevice(context: Context, contact: Contact): Boolean {
        if (!hasWritePermission(context)) return false

        val ops = ArrayList<ContentProviderOperation>()
        val rawContactInsertIndex = ops.size

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Display Name / Structured Name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)
                .build()
        )

        // Phone numbers
        for (phone in contact.phoneNumbers) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.value)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )
        }

        // Emails
        for (email in contact.emails) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email.value)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .build()
            )
        }

        // Company
        if (contact.company.isNotBlank() || contact.jobTitle.isNotBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, contact.jobTitle)
                    .build()
            )
        }

        // Notes
        if (contact.notes.isNotBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.notes)
                    .build()
            )
        }

        return try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            results.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert contact: ${e.message}", e)
            false
        }
    }

    private data class MutableContactData(
        val id: String,
        val lookupKey: String,
        val displayName: String,
        val isStarred: Boolean,
        val phoneNumbers: MutableList<ContactDetail> = mutableListOf(),
        val emails: MutableList<ContactDetail> = mutableListOf()
    )
}
