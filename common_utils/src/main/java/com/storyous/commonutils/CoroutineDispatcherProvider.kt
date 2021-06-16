package com.storyous.commonutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

@Suppress("VariableNaming", "PropertyName")
open class CoroutineDispatcherProvider {

    companion object {
        var delegate: (() -> CoroutineContext?) = { null }
    }

    open val Main: CoroutineContext by lazy { delegate() ?: Dispatchers.Main }

    open val IO: CoroutineContext by lazy { delegate() ?: Dispatchers.IO }

    open val Default: CoroutineContext by lazy { delegate() ?: Dispatchers.Default }
}

val CoroutineScope.provider: CoroutineDispatcherProvider
    get() = CoroutineDispatcherProvider()

class CoroutineProviderScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + provider.Main
}
