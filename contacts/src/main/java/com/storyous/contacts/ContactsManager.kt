package com.storyous.contacts

import com.storyous.firebase.FirebaseAuth
import com.storyous.firebase.StoryousFirebaseAuth
import com.storyous.firebase.StoryousTokenData
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class ContactsManager(
    private val repository: ContactsRepository = ContactsRepository(),
    private val firebaseAuth: FirebaseAuth<StoryousTokenData> = StoryousFirebaseAuth()
) : FirebaseAuth<StoryousTokenData> by firebaseAuth {
    companion object {
        const val FIREBASE_TIMEOUT = 20000L
    }

    @Throws(IllegalArgumentException::class)
    suspend fun getContacts(phoneNumber: String) = withTimeout(FIREBASE_TIMEOUT) {
        repository.getContacts(
            requireNotNull(tokenData.merchantId),
            requireNotNull(tokenData.placeId),
            phoneNumber
        ).also { Timber.d("ContactsManager get $it") }
    }

    @Throws(IllegalArgumentException::class)
    suspend fun updateContact(contact: Contact) = withTimeout(FIREBASE_TIMEOUT) {
        repository.updateContact(
            requireNotNull(tokenData.merchantId),
            requireNotNull(tokenData.placeId),
            contact
        )
    }

    @Throws(IllegalArgumentException::class)
    suspend fun updateIncomingCalls(incomingCall: IncomingCall) = withTimeout(FIREBASE_TIMEOUT) {
        repository.updateIncomingCalls(
            requireNotNull(tokenData.merchantId),
            requireNotNull(tokenData.placeId),
            incomingCall
        )
    }

    @Throws(IllegalArgumentException::class)
    suspend fun getIncomingCalls() = withTimeout(FIREBASE_TIMEOUT) {
        repository.getIncomingCalls(
            requireNotNull(tokenData.merchantId),
            requireNotNull(tokenData.placeId)
        )
    }
}
