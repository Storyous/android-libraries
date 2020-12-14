package com.storyous.firebase

import timber.log.Timber

interface FirebaseAuth<T> {
    val tokenData: T

    fun isAuthenticated(): Boolean

    @Throws(IllegalStateException::class)
    suspend fun authenticate(token: String)

    fun signOut()

    suspend fun <R> callOnAuthorized(tokenProvider: suspend () -> String, block: suspend () -> R): R? {
        return runCatching {
            if (!isAuthenticated()) {
                authenticate(tokenProvider())
            }
            block()
        }.onFailure {
            Timber.e(it, "FirebaseAuth call failed.")
        }.getOrNull()
    }
}
