package com.storyous.apitools

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Create Authorization header in all requests.
 */
class AuthInterceptor : Interceptor {

    companion object {
        const val HEADER_AUTH = "Authorization"
    }

    internal var authHeaderProvider: AuthHeaderProvider? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        if (chain.request().header(HEADER_AUTH) == null) {
            authHeaderProvider?.getAuth()?.let {
                builder.header(HEADER_AUTH, it)
            }
        }

        return chain.proceed(builder.build())
    }
}
