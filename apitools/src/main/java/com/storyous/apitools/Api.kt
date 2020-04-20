package com.storyous.apitools

import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import java.security.KeyStore
import kotlin.reflect.KClass

abstract class Api<S : Any, E : Any> {

    companion object {
        const val LOCALHOST = "http://localhost/"
    }

    abstract val serviceClazz: KClass<S>
    abstract val errorClazz: KClass<E>
    abstract var baseUrl: String
    lateinit var service: S
    lateinit var errorConverter: Converter<ResponseBody, E>
    open val okHttpClientBuilder = OkHttpClient.Builder()
    open val cache: Cache? = null
    open val convertFactories: List<Converter.Factory> = listOf()
    open val keyStorePassword: Pair<KeyStore, String>? = null
    open val interceptors: List<Interceptor> = listOf()

    fun rebuild() {
        val apiBuilder = ApiBuilder(this)
        service = apiBuilder.build(serviceClazz)
        errorConverter = apiBuilder.buildError(errorClazz)
    }
}
