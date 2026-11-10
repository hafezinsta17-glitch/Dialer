package com.example.model

data class ContactDetail(
    val label: String,
    val value: String
)

data class Contact(
    val id: String,
    val rawContactId: Long? = null,
    val lookupKey: String? = null,
    val displayName: String,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumbers: List<ContactDetail> = emptyList(),
    val emails: List<ContactDetail> = emptyList(),
    val company: String = "",
    val jobTitle: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val avatarColorIndex: Int = 0,
    val isDeviceContact: Boolean = false
) {
    val primaryPhone: String
        get() = phoneNumbers.firstOrNull()?.value ?: ""

    val primaryEmail: String
        get() = emails.firstOrNull()?.value ?: ""

    val initials: String
        get() {
            val parts = displayName.trim().split("\\s+".toRegex())
            return when {
                parts.isEmpty() || parts[0].isBlank() -> "#"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[parts.size - 1].take(1)}".uppercase()
            }
        }

    val sortSection: String
        get() {
            val trimmed = displayName.trim()
            if (trimmed.isEmpty()) return "#"
            val firstChar = trimmed[0].uppercaseChar()
            return if (firstChar in 'A'..'Z') firstChar.toString() else "#"
        }
}

enum class CallType {
    OUTGOING,
    INCOMING,
    MISSED
}

data class CallRecord(
    val id: String,
    val name: String,
    val number: String,
    val type: CallType,
    val timestamp: Long,
    val durationSeconds: Int = 0
)
