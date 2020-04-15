package com.storyous.contacts

import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

class ContactsRepository(
    private val fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    @Suppress("Detekt.MaxLineLength")
    companion object {
        const val FLD_PHONE_NUMBER = "phoneNumber"
        const val FLD_DATE = "date"
    }

    @Throws(FirebaseException::class)
    suspend fun getContacts(
        merchantId: String,
        placeId: String,
        phoneNumber: String,
        limit: Long = 10
    ): List<Contact> {
        return fireStore.collection(contactsCollectionPath(merchantId, placeId))
            .orderBy(FLD_PHONE_NUMBER)
            .startsWith(FLD_PHONE_NUMBER, phoneNumber)
            .limit(limit)
            .get()
            .await()
            .toObjects()
    }

    @Throws(FirebaseException::class)
    suspend fun updateContact(
        merchantId: String,
        placeId: String,
        contact: Contact
    ) {
        fireStore.collection(contactsCollectionPath(merchantId, placeId))
            .document(contact.phoneNumber)
            .set(contact)
            .await()
    }

    @Throws(FirebaseException::class)
    suspend fun updateIncomingCalls(
        merchantId: String,
        placeId: String,
        incomingCall: IncomingCall
    ) {
        fireStore.collection(callsCollectionPath(merchantId, placeId))
            .document(incomingCall.phoneNumber)
            .set(incomingCall)
            .await()
    }

    @Throws(FirebaseException::class)
    suspend fun getIncomingCalls(
        merchantId: String,
        placeId: String,
        limit: Long = 10
    ): List<IncomingCall> {
        return fireStore.collection(callsCollectionPath(merchantId, placeId))
            .orderBy(FLD_DATE, Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects()
    }

    private fun merchantCollectionPath(merchantId: String) =
        "merchants/$merchantId"

    private fun placeCollectionPath(merchantId: String, placeId: String) =
        "${merchantCollectionPath(merchantId)}/places/$placeId"

    private fun contactsCollectionPath(merchantId: String, placeId: String) =
        "${placeCollectionPath(merchantId, placeId)}/contacts"

    private fun callsCollectionPath(merchantId: String, placeId: String) =
        "${placeCollectionPath(merchantId, placeId)}/calls"
}
