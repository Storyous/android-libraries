package com.storyous.firebase

import com.auth0.android.jwt.DecodeException
import com.google.firebase.FirebaseApp
import com.google.firebase.nongmsauth.FirebaseRestAuth
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class StoryousFirebaseAuth(
    private val firebaseAuth: FirebaseRestAuth = FirebaseRestAuth.getInstance(FirebaseApp.getInstance())
) : FirebaseAuth<StoryousTokenData> {

    override val tokenData = StoryousTokenData()

    init {
        runCatching {
            firebaseAuth.currentUser?.idToken?.let {
                val (merchantId, placeId) = decodeToken(it)

                tokenData.merchantId = merchantId
                tokenData.placeId = placeId
                Timber.d("App is authorized for merchantId=$merchantId placeId=$placeId.")
            }
        }.onFailure {
            Timber.d(it, "App is not authorized.")
        }
    }

    override fun isAuthenticated() = firebaseAuth.currentUser != null

    @Throws(IllegalStateException::class)
    override suspend fun authenticate(token: String) {
        val (merchantId, placeId) = decodeToken(token)

        runCatching {
            firebaseAuth.signInWithCustomToken(token).await()
        }.onFailure {
            tokenData.merchantId = null
            tokenData.placeId = null
            Timber.e(it, "Authorization fail. merchantId=$merchantId placeId=$placeId")
        }.onSuccess {
            tokenData.merchantId = merchantId
            tokenData.placeId = placeId
            Timber.d("Authorization success. merchantId=$merchantId placeId=$placeId")
        }.getOrThrow()

        Unit
    }

    override fun signOut() = firebaseAuth.signOut()

    @Throws(IllegalStateException::class, DecodeException::class)
    private fun decodeToken(token: String): Pair<String, String> {
        return JWTWrapper.decode(token)
            .takeIf { it.merchantId != null && it.placeId != null }
            ?.let { it.merchantId!! to it.placeId!! }
            ?: error("JWT token not contains merchantId or placeId.")
    }
}
