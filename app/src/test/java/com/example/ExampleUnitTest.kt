package com.example

import com.example.data.VcfManager
import com.example.model.Contact
import com.example.model.ContactDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {

    @Test
    fun testContactInitialsAndSorting() {
        val contact1 = Contact(
            id = "1",
            displayName = "Steve Jobs"
        )
        assertEquals("SJ", contact1.initials)
        assertEquals("S", contact1.sortSection)

        val singleName = Contact(
            id = "2",
            displayName = "Cher"
        )
        assertEquals("C", singleName.initials)
        assertEquals("C", singleName.sortSection)

        val numberName = Contact(
            id = "3",
            displayName = "911 Emergency"
        )
        assertEquals("#", numberName.sortSection)
    }

    @Test
    fun testVcfExportAndParseRoundTrip() {
        val originalContacts = listOf(
            Contact(
                id = "c1",
                displayName = "Alice Wonderland",
                firstName = "Alice",
                lastName = "Wonderland",
                phoneNumbers = listOf(
                    ContactDetail("mobile", "+1 (555) 012-3456"),
                    ContactDetail("home", "+1 (555) 987-6543")
                ),
                emails = listOf(ContactDetail("work", "alice@wonderland.org")),
                company = "Curious Inc",
                jobTitle = "Explorer"
            ),
            Contact(
                id = "c2",
                displayName = "Bob Builder",
                firstName = "Bob",
                lastName = "Builder",
                phoneNumbers = listOf(ContactDetail("work", "+1 (555) 777-8888")),
                emails = emptyList(),
                company = "Construction Co",
                jobTitle = "Chief Builder"
            )
        )

        // 1. Export to VCF string
        val vcfString = VcfManager.buildVcfString(originalContacts)
        assertTrue(vcfString.contains("BEGIN:VCARD"))
        assertTrue(vcfString.contains("FN:Alice Wonderland"))
        assertTrue(vcfString.contains("TEL;TYPE=CELL:+1 (555) 012-3456"))
        assertTrue(vcfString.contains("FN:Bob Builder"))
        assertTrue(vcfString.contains("END:VCARD"))

        // 2. Parse back from InputStream (simulating multi-contact VCF file)
        val stream = ByteArrayInputStream(vcfString.toByteArray(Charsets.UTF_8))
        val parsed = VcfManager.parseVcf(stream)

        assertEquals(2, parsed.size)
        assertEquals("Alice Wonderland", parsed[0].displayName)
        assertEquals(2, parsed[0].phoneNumbers.size)
        assertEquals("+1 (555) 012-3456", parsed[0].primaryPhone)
        assertEquals("alice@wonderland.org", parsed[0].primaryEmail)
        assertEquals("Curious Inc", parsed[0].company)

        assertEquals("Bob Builder", parsed[1].displayName)
        assertEquals("+1 (555) 777-8888", parsed[1].primaryPhone)
        assertEquals("Construction Co", parsed[1].company)
    }

    @Test
    fun testVcfParserHandlesEscapedCharacters() {
        val vcfInput = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John\, Jr. Doe
            N:Doe;John\, Jr.;;;
            TEL;TYPE=CELL:555-0100
            NOTE:Line 1\nLine 2\, continued
            END:VCARD
        """.trimIndent()

        val parsed = VcfManager.parseVcf(ByteArrayInputStream(vcfInput.toByteArray()))
        assertEquals(1, parsed.size)
        assertEquals("John, Jr. Doe", parsed[0].displayName)
        assertEquals("Line 1\nLine 2, continued", parsed[0].notes)
    }
}
