package com.storyous.apitools

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import kotlin.reflect.KClass

class ApiBuilder(definition: Api<*, *>) {

    private val retrofit: Retrofit

    init {
        val httpClientBuilder = definition.okHttpClientBuilder

        if (definition.cache != null) {
            httpClientBuilder.cache(definition.cache)
        }

        definition.interceptors.iterator().forEach {
            httpClientBuilder.addInterceptor(it)
        }

        retrofit = Retrofit.Builder()
            .baseUrl(definition.baseUrl)
            .client(httpClientBuilder.build())
            .apply {
                definition.convertFactories.iterator().forEach { addConverterFactory(it) }
            }
            .build()
    }

    fun <T : Any> build(cls: KClass<T>): T {
        return retrofit.create(cls.java)
    }

    fun <T : Any> buildError(cls: KClass<T>): Converter<ResponseBody, T> {
        return retrofit.responseBodyConverter(cls.java, arrayOfNulls<Annotation>(0))
    }
}
