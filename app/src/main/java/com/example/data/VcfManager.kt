package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.Contact
import com.example.model.ContactDetail
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID
import kotlin.math.abs

object VcfManager {
    private const val TAG = "VcfManager"

    data class ImportResult(
        val totalFiles: Int,
        val importedContacts: List<Contact>,
        val failedFiles: Int = 0
    )

    /**
     * Imports multiple VCF files from content URIs and returns parsed contacts.
     */
    fun importMultipleVcfFiles(context: Context, uris: List<Uri>): ImportResult {
        val allContacts = mutableListOf<Contact>()
        var failedCount = 0

        for (uri in uris) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val contacts = parseVcf(stream)
                    allContacts.addAll(contacts)
                } ?: run {
                    failedCount++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing VCF from URI: $uri", e)
                failedCount++
            }
        }

        // Deduplicate imported contacts by (Name + Primary Phone)
        val deduplicated = allContacts.distinctBy {
            "${it.displayName.lowercase().trim()}_${it.primaryPhone.filter { ch -> ch.isDigit() }}"
        }

        return ImportResult(
            totalFiles = uris.size,
            importedContacts = deduplicated,
            failedFiles = failedCount
        )
    }

    /**
     * Parses an input stream of vCard text (supporting multi-contact vCards).
     */
    fun parseVcf(inputStream: InputStream): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var inVCard = false
        var currentFormattedName = ""
        var currentFirstName = ""
        var currentLastName = ""
        var currentCompany = ""
        var currentTitle = ""
        var currentNotes = ""
        val currentPhones = mutableListOf<ContactDetail>()
        val currentEmails = mutableListOf<ContactDetail>()

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()

            // Handle folded lines in vCard (lines beginning with space or tab)
            var fullLine = trimmed
            while (true) {
                reader.mark(1024)
                val nextLine = reader.readLine()
                if (nextLine != null && (nextLine.startsWith(" ") || nextLine.startsWith("\t"))) {
                    fullLine += nextLine.trim()
                } else {
                    reader.reset()
                    break
                }
            }

            if (fullLine.equals("BEGIN:VCARD", ignoreCase = true)) {
                inVCard = true
                currentFormattedName = ""
                currentFirstName = ""
                currentLastName = ""
                currentCompany = ""
                currentTitle = ""
                currentNotes = ""
                currentPhones.clear()
                currentEmails.clear()
            } else if (fullLine.equals("END:VCARD", ignoreCase = true)) {
                if (inVCard) {
                    val displayName = when {
                        currentFormattedName.isNotBlank() -> currentFormattedName
                        currentFirstName.isNotBlank() || currentLastName.isNotBlank() ->
                            "$currentFirstName $currentLastName".trim()
                        currentPhones.isNotEmpty() -> currentPhones.first().value
                        else -> "Unnamed Contact"
                    }

                    val colorIdx = abs(displayName.hashCode()) % 8
                    val contact = Contact(
                        id = UUID.randomUUID().toString(),
                        displayName = displayName,
                        firstName = currentFirstName,
                        lastName = currentLastName,
                        phoneNumbers = currentPhones.toList(),
                        emails = currentEmails.toList(),
                        company = currentCompany,
                        jobTitle = currentTitle,
                        notes = currentNotes,
                        avatarColorIndex = colorIdx,
                        isDeviceContact = false
                    )
                    contacts.add(contact)
                }
                inVCard = false
            } else if (inVCard) {
                val colonIdx = fullLine.indexOf(':')
                if (colonIdx > 0) {
                    val tagPart = fullLine.substring(0, colonIdx).uppercase()
                    val valuePart = fullLine.substring(colonIdx + 1).trim()

                    when {
                        tagPart == "FN" -> {
                            currentFormattedName = decodeVCardValue(valuePart)
                        }
                        tagPart == "N" || tagPart.startsWith("N;") -> {
                            val decoded = decodeVCardValue(valuePart)
                            val parts = decoded.split(";")
                            if (parts.isNotEmpty()) currentLastName = parts[0].trim()
                            if (parts.size > 1) currentFirstName = parts[1].trim()
                        }
                        tagPart.startsWith("TEL") -> {
                            val label = extractPhoneLabel(tagPart)
                            val cleanNumber = decodeVCardValue(valuePart)
                            if (cleanNumber.isNotBlank()) {
                                currentPhones.add(ContactDetail(label = label, value = cleanNumber))
                            }
                        }
                        tagPart.startsWith("EMAIL") -> {
                            val label = extractEmailLabel(tagPart)
                            val cleanEmail = decodeVCardValue(valuePart)
                            if (cleanEmail.isNotBlank()) {
                                currentEmails.add(ContactDetail(label = label, value = cleanEmail))
                            }
                        }
                        tagPart.startsWith("ORG") -> {
                            val decoded = decodeVCardValue(valuePart)
                            currentCompany = decoded.split(";").firstOrNull()?.trim() ?: decoded
                        }
                        tagPart.startsWith("TITLE") -> {
                            currentTitle = decodeVCardValue(valuePart)
                        }
                        tagPart.startsWith("NOTE") -> {
                            currentNotes = decodeVCardValue(valuePart).replace("\\n", "\n")
                        }
                    }
                }
            }

            line = reader.readLine()
        }

        return contacts
    }

    private fun extractPhoneLabel(tag: String): String {
        val upper = tag.uppercase()
        return when {
            upper.contains("CELL") || upper.contains("MOBILE") -> "mobile"
            upper.contains("HOME") -> "home"
            upper.contains("WORK") -> "work"
            upper.contains("MAIN") -> "main"
            upper.contains("FAX") -> "fax"
            else -> "phone"
        }
    }

    private fun extractEmailLabel(tag: String): String {
        val upper = tag.uppercase()
        return when {
            upper.contains("WORK") -> "work"
            upper.contains("HOME") -> "home"
            else -> "email"
        }
    }

    private fun decodeVCardValue(value: String): String {
        // Basic unescaping for vCard 3.0 escaped commas and colons
        return value.replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
    }

    /**
     * Converts a list of contacts to a valid vCard 3.0 string.
     */
    fun buildVcfString(contacts: List<Contact>): String {
        val sb = StringBuilder()
        for (c in contacts) {
            sb.appendLine("BEGIN:VCARD")
            sb.appendLine("VERSION:3.0")
            sb.appendLine("FN:${c.displayName}")

            val lastName = c.lastName.ifBlank {
                val parts = c.displayName.trim().split(" ")
                if (parts.size > 1) parts.last() else ""
            }
            val firstName = c.firstName.ifBlank {
                val parts = c.displayName.trim().split(" ")
                if (parts.size > 1) parts.first() else c.displayName
            }
            sb.appendLine("N:$lastName;$firstName;;;")

            for (phone in c.phoneNumbers) {
                val type = when (phone.label.lowercase()) {
                    "mobile", "cell" -> "CELL"
                    "home" -> "HOME"
                    "work" -> "WORK"
                    else -> "VOICE"
                }
                sb.appendLine("TEL;TYPE=$type:${phone.value}")
            }

            for (email in c.emails) {
                val type = when (email.label.lowercase()) {
                    "work" -> "WORK"
                    else -> "HOME"
                }
                sb.appendLine("EMAIL;TYPE=$type:${email.value}")
            }

            if (c.company.isNotBlank()) {
                sb.appendLine("ORG:${c.company}")
            }
            if (c.jobTitle.isNotBlank()) {
                sb.appendLine("TITLE:${c.jobTitle}")
            }
            if (c.notes.isNotBlank()) {
                sb.appendLine("NOTE:${c.notes.replace("\n", "\\n")}")
            }

            sb.appendLine("END:VCARD")
        }
        return sb.toString()
    }

    /**
     * Writes VCF content to an OutputStream (e.g. from CreateDocument Uri).
     */
    fun exportToOutputStream(outputStream: OutputStream, contacts: List<Contact>): Boolean {
        return try {
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(buildVcfString(contacts))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write VCF output stream: ${e.message}", e)
            false
        }
    }

    /**
     * Generates a temporary .vcf file for sharing via Intent.ACTION_SEND.
     */
    fun createShareableVcfFile(context: Context, contacts: List<Contact>, fileName: String = "contacts.vcf"): File? {
        return try {
            val cacheDir = File(context.cacheDir, "vcf_exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)
            file.writeText(buildVcfString(contacts), Charsets.UTF_8)
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing temporary shareable VCF: ${e.message}", e)
            null
        }
    }
}
