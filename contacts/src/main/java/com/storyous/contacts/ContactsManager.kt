package com.storyous.contacts

import com.auth0.android.jwt.DecodeException
import com.google.firebase.FirebaseApp
import com.google.firebase.nongmsauth.FirebaseRestAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class ContactsManager(
    private val repository: ContactsRepository = ContactsRepository(),
    private val firebaseAuth: FirebaseRestAuth = FirebaseRestAuth.getInstance(FirebaseApp.getInstance())
) {
    companion object {
        const val FIREBASE_TIMEOUT = 20000L
    }

    private var merchantId: String? = null
    private var placeId: String? = null

    init {
        runCatching {
            firebaseAuth.currentUser?.idToken?.let {
                val (merchantId, placeId) = decodeToken(it)

                this.merchantId = merchantId
                this.placeId = placeId
                Timber.d("App is authorized for merchantId=$merchantId placeId=$placeId.")
            }
        }.onFailure {
            Timber.d(it, "App is not authorized.")
        }
    }

    @Throws(IllegalArgumentException::class)
    suspend fun getContacts(phoneNumber: String) = withTimeout(FIREBASE_TIMEOUT) {
        repository.getContacts(
            requireNotNull(merchantId),
            requireNotNull(placeId),
            phoneNumber
        ).also { Timber.d("ContactsManager get $it") }
    }

    @Throws(IllegalArgumentException::class)
    suspend fun updateContact(contact: Contact) = withTimeout(FIREBASE_TIMEOUT) {
        repository.updateContact(
            requireNotNull(merchantId),
            requireNotNull(placeId),
            contact
        )
    }

    @Throws(IllegalArgumentException::class)
    suspend fun updateIncomingCalls(incomingCall: IncomingCall) = withTimeout(FIREBASE_TIMEOUT) {
        repository.updateIncomingCalls(
            requireNotNull(merchantId),
            requireNotNull(placeId),
            incomingCall
        )
    }

    @Throws(IllegalArgumentException::class)
    suspend fun getIncomingCalls() = withTimeout(FIREBASE_TIMEOUT) {
        repository.getIncomingCalls(
            requireNotNull(merchantId),
            requireNotNull(placeId)
        )
    }

    fun isAuthenticated() = firebaseAuth.currentUser != null

    @Throws(IllegalStateException::class)
    suspend fun authenticate(token: String) {
        val (merchantId, placeId) = decodeToken(token)

        runCatching {
            firebaseAuth.signInWithCustomToken(token).await()
        }.onFailure {
            this@ContactsManager.merchantId = null
            this@ContactsManager.placeId = null
            Timber.e(it, "Authorization fail. merchantId=$merchantId placeId=$placeId")
        }.onSuccess {
            this@ContactsManager.merchantId = merchantId
            this@ContactsManager.placeId = placeId
            Timber.d("Authorization success. merchantId=$merchantId placeId=$placeId")
        }.getOrThrow()

        Unit
    }

    @Throws(IllegalStateException::class, DecodeException::class)
    private fun decodeToken(token: String): Pair<String, String> {
        return JWTWrapper.decode(token)
            .takeIf { it.merchantId != null && it.placeId != null }
            ?.let { it.merchantId!! to it.placeId!! }
            ?: throw IllegalStateException("JWT token not contains merchantId or placeId.")
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
