@file:Suppress("TooManyFunctions")

package com.storyous.commonutils.extensions

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.storyous.commonutils.CoroutineProviderScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

fun <T> LiveData<T>.getDistinct(): LiveData<T> {

    return MediatorLiveData<T>().also {
        it.addSource(this, object : Observer<T> {

            private var initialized = false
            private var lastObj: T? = null

            override fun onChanged(obj: T?) {
                if (!initialized) {
                    initialized = true
                    lastObj = obj
                    it.postValue(lastObj)
                } else if ((obj == null && lastObj != null) || obj != lastObj) {
                    lastObj = obj
                    it.postValue(lastObj)
                }
            }
        })
    }
}

fun <T> MutableLiveData<T>.notifyObservers() {
    this.postValue(this.value)
}

fun <T> MutableLiveData<T>.update(block: (T?) -> T?) {
    value = block(value)
}

suspend fun <T> LiveData<T>.await(trigger: Int = 0): T {
    var triggerCount = trigger
    return await {
        try {
            triggerCount == 0
        } finally {
            triggerCount = trigger.dec()
        }
    }
}

fun <T> LiveData<T>.awaitForJava(acceptCondition: (T) -> Boolean, callback: (T) -> Unit) {
    val scope = CoroutineProviderScope()
    scope.launch {
        val data = await { acceptCondition(it) }
        callback(data)
    }
}

suspend fun <T> LiveData<T>.await(acceptCondition: (T) -> Boolean): T {
    return suspendCancellableCoroutine { cont ->
        val observer = object : Observer<T> {

            override fun onChanged(t: T) {
                if (t == null) {
                    return
                }
                if (acceptCondition(t)) {
                    removeObserver(this)
                    cont.resume(t)
                }
            }
        }
        observeForever(observer)
    }
}

suspend fun <T> LiveData<T>.await(timeout: Long, acceptCondition: (T) -> Boolean): T {
    return withTimeout(timeout) { await(acceptCondition) }
}

fun <T> LiveData<T>.awaitNonNull(block: (T) -> Unit) {
    runBlocking {
        block(await { it != null })
    }
}

fun <T> LiveData<T>.reattachObserver(owner: LifecycleOwner, observer: Observer<T>) {
    removeObservers(owner)
    observe(owner, observer)
}

fun <T> MediatorLiveData<T>.onChange(sources: Array<LiveData<*>>, onChange: () -> T): MediatorLiveData<T> {
    sources.forEach {
        addSource(it) { value = onChange() }
    }
    return this
}

fun <T> LiveData<T>.repeat(interval: Long): LiveData<T> = object : MediatorLiveData<T>() {
    private val handler = Handler(Looper.getMainLooper())
    private val updateValue: () -> Unit = {
        if (this@repeat.value != null) {
            value = this@repeat.value
        }
        if (hasActiveObservers()) {
            handler.postDelayed(runnable, interval)
        }
    }
    private val runnable = Runnable { updateValue() }

    override fun onActive() {
        super.onActive()
        handler.postDelayed(runnable, interval)
    }

    override fun onInactive() {
        super.onInactive()
        handler.removeCallbacks(runnable)
    }
}.apply {
    addSource(this@repeat) {
        value = it
    }
}

fun <T> LiveData<T>.debounce(time: Long): LiveData<T> = MediatorLiveData<T>().apply {
    val handler = Handler(Looper.getMainLooper())

    val runnable = Runnable {
        value = this@debounce.value
    }

    addSource(this@debounce) {
        handler.removeCallbacks(runnable)
        if (value == null) {
            handler.post(runnable)
        } else {
            handler.postDelayed(runnable, time)
        }
    }
}
