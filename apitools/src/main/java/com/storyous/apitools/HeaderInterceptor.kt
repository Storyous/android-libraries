package com.storyous.apitools

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Create custom headers in all requests.
 */
class HeaderInterceptor : Interceptor {

    internal val customHeaders = HashMap<String, String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        HashMap(customHeaders).forEach { builder.header(it.key, it.value) }

        return chain.proceed(builder.build())
    }
}
