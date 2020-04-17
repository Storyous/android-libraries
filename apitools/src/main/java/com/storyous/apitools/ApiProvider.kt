package com.storyous.apitools

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Responsible for create and provide apis
 */
object ApiProvider {

    private val apiInstances = mutableMapOf<KClass<out Api<*, *>>, Api<*, *>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Api<*, *>> getApi(clazz: KClass<T>): () -> T = {
        (apiInstances[clazz] ?: buildApi(clazz).also { apiInstances[clazz] = it }) as T
    }

    fun <T : Api<*, *>> buildApi(clazz: KClass<T>): T {
        return clazz.primaryConstructor!!.call()
            .also { it.rebuild() }
    }
}
