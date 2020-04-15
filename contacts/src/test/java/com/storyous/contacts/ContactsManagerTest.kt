package com.storyous.contacts

import com.storyous.storyouspay.contacts.MockFirestore
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ContactsManagerTest {

    companion object {
        const val AUTH_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmF" +
                    "tZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJwbGFjZUlkIjoicGxhY2VJZCIsIm1lc" +
                    "mNoYW50SWQiOiJtZXJjaGFudElkIn0.qA980a8Z1TgnFtaFy8kg_VmQdZh_JubrC-xe0nPNPp8"
    }

    private lateinit var contactsManager: ContactsManager
    private val firestoreMock = MockFirestore(Contact::class.java)

    @Before
    fun setup() {
        JWTWrapper.decoder = JWTDecoder()

        firestoreMock.clear()

        contactsManager = ContactsManager(
            ContactsRepository(firestoreMock.firestore),
            firestoreMock.auth
        )

        runBlocking {
            contactsManager.authenticate(AUTH_TOKEN)
        }
    }

    @Test
    fun getContacts() = runBlocking {
        val telNum = "+420"
        val contacts = contactsManager.getContacts(telNum)

        Assert.assertTrue(contacts.isEmpty())
    }

    @Test
    fun addContact() = runBlocking {
        val contact = Contact("+420123456789", "John", "62-24 78th Street", "New York")
        contactsManager.updateContact(contact)

        val telNum = "+420"
        val contacts = contactsManager.getContacts(telNum)

        Assert.assertEquals(1, contacts.size)
    }

    @Test
    fun failOnUpdateContact() = runBlocking {
        val contact = Contact("+420123456789", "John", "62-24 78th Street", "New York")

        firestoreMock.nextException = IllegalStateException("Aborted")
        runCatching {
            contactsManager.updateContact(contact)
        }

        firestoreMock.nextException = null
        val telNum = "+420"
        var contacts = contactsManager.getContacts(telNum)

        Assert.assertEquals(0, contacts.size)

        contactsManager.updateContact(contact)

        contacts = contactsManager.getContacts(telNum)

        Assert.assertEquals(1, contacts.size)
    }

    @Test
    fun failOnNonAuthenticated() = runBlocking {
        val contact = Contact("+420123456789", "John", "62-24 78th Street", "New York")

        Assert.assertTrue(
            runCatching { contactsManager.authenticate("corrupted_token") }.isFailure
        )

        firestoreMock.nextException = IllegalStateException("Unaurthorized")
        Assert.assertTrue(
            runCatching { contactsManager.updateContact(contact) }.isFailure
        )
        firestoreMock.nextException = null

        val telNum = "+420"
        var contacts = contactsManager.getContacts(telNum)

        Assert.assertEquals(0, contacts.size)

        contactsManager.authenticate(AUTH_TOKEN)
        contactsManager.updateContact(contact)

        contacts = contactsManager.getContacts(telNum)

        Assert.assertEquals(1, contacts.size)
    }
}
