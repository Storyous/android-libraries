package com.storyous.commonutils

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> bind(
    view: View
) = T::class.java.getMethod("bind", View::class.java)
    .invoke(null, view) as T

inline fun <reified T : ViewBinding> viewBinding(
    owner: LifecycleOwner,
    noinline getViewBinding: () -> T
) = ViewBindingDelegate(owner, getViewBinding)

inline fun <T : ViewBinding> viewBinding(
    bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
    parent: ViewGroup,
    attachToParent: Boolean = false
) = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, attachToParent)

inline fun <reified T : ViewBinding> Fragment.viewBinding() = viewBinding(this) {
    bind<T>(requireView())
}

inline fun <reified T : ViewBinding> ComponentActivity.viewBinding() = viewBinding(this) {
    val view = checkNotNull(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) {
        "Call setContentView or Use Activity's secondary constructor passing layout res id."
    }

    bind<T>(view)
}

class ViewBindingDelegate<T : ViewBinding>(
    private val owner: LifecycleOwner,
    private val getViewBinding: () -> T
) : ReadOnlyProperty<Any, T> {

    private var _binding: T? = null
    private val lifecycleObserver by lazy { BindingLifecycleObserver() }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        owner.lifecycle.addObserver(lifecycleObserver)
        return _binding ?: getViewBinding().also { _binding = it }
    }

    private inner class BindingLifecycleObserver : DefaultLifecycleObserver {

        private val mainHandler = Handler(Looper.getMainLooper())

        @MainThread
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            // Fragment.viewLifecycleOwner call LifecycleObserver.onDestroy() before Fragment.onDestroyView().
            // That's why we need to postpone reset of the viewBinding
            mainHandler.post { _binding = null }
        }
    }
}
