package com.storyous.commonutils.toaster

import android.content.Context
import android.content.ContextWrapper
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

/**
 * @author Niharika.Arora
 */
internal class ToastContextWrapper(base: Context) : ContextWrapper(base) {

    override fun getApplicationContext(): Context {
        return ApplicationContextWrapper(baseContext.applicationContext)
    }

    private class ApplicationContextWrapper(base: Context) : ContextWrapper(base) {
        override fun getSystemService(name: String): Any? {
            return if (name == WINDOW_SERVICE) {
                WindowManagerWrapper(baseContext.getSystemService(name) as WindowManager)
            } else {
                super.getSystemService(name)
            }
        }
    }

    private class WindowManagerWrapper(private val base: WindowManager) : WindowManager {

        @Deprecated("But needed because of Android 7 which needs ToastCompat")
        override fun getDefaultDisplay(): Display {
            return base.defaultDisplay
        }

        override fun removeViewImmediate(view: View) {
            base.removeViewImmediate(view)
        }

        override fun addView(view: View, params: ViewGroup.LayoutParams) {
            try {
                base.addView(view, params)
            } catch (_: WindowManager.BadTokenException) {
                // ignore
            }
        }

        override fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
            base.updateViewLayout(view, params)
        }

        override fun removeView(view: View) {
            base.removeView(view)
        }
    }
}
