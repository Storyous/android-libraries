package com.storyous.commonutils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import timber.log.Timber

object Toaster {

    @JvmStatic
    @JvmOverloads
    fun showLong(context: Context, resId: Int, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return show(context, resId, Toast.LENGTH_LONG, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showShort(context: Context, resId: Int, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return show(context, resId, Toast.LENGTH_SHORT, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showLong(context: Context, text: String, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return show(context, text, Toast.LENGTH_LONG, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showShort(context: Context, text: String, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return show(context, text, Toast.LENGTH_SHORT, applyBlock)
    }

    @JvmStatic
    fun show(context: Context, text: String, duration: Int): Toast {
        return show(context, text, duration, {})
    }

    @SuppressLint("ToastDetector")
    fun makeText(context: Context, text: String, length: Int): Toast {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            ToastCompat.makeText(context, text, length)
        } else {
            Toast.makeText(context, text, length)
        }
    }

    private fun show(
        context: Context,
        resId: Int,
        duration: Int,
        applyBlock: ((Toast) -> Unit) = {}
    ) = show(context, context.resources.getText(resId).toString(), duration, applyBlock)


    private fun show(
        context: Context,
        text: String,
        length: Int,
        applyBlock: ((Toast) -> Unit) = {}
    ): Toast {
        Timber.i("Showing toast in $context: $text")
        return makeText(context, text, length).apply {
            applyBlock.invoke(this)
            show()
        }
    }


    /**
     * @author Niharika.Arora
     */
    @Suppress("TooManyFunctions")
    // TODO Remove after min SDK > 25
    private class ToastCompat(context: Context, private val toast: Toast) : Toast(context) {

        override fun show() {
            toast.show()
        }

        override fun setDuration(duration: Int) {
            toast.duration = duration
        }

        override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
            toast.setGravity(gravity, xOffset, yOffset)
        }

        override fun setMargin(horizontalMargin: Float, verticalMargin: Float) {
            toast.setMargin(horizontalMargin, verticalMargin)
        }

        override fun setText(resId: Int) {
            toast.setText(resId)
        }

        override fun setText(s: CharSequence) {
            toast.setText(s)
        }

        @Deprecated("But needed because of Android 7 which needs ToastCompat")
        override fun setView(view: View) {
            toast.view = view
            setContextCompat(view, ToastContextWrapper(view.context))
        }

        override fun getHorizontalMargin(): Float {
            return toast.horizontalMargin
        }

        override fun getVerticalMargin(): Float {
            return toast.verticalMargin
        }

        override fun getDuration(): Int {
            return toast.duration
        }

        override fun getGravity(): Int {
            return toast.gravity
        }

        override fun getXOffset(): Int {
            return toast.xOffset
        }

        override fun getYOffset(): Int {
            return toast.yOffset
        }

        @Deprecated("But needed because of Android 7 which needs ToastCompat")
        override fun getView(): View? {
            return toast.view
        }

        private fun setContextCompat(view: View?, context: Context) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 && view != null) {
                try {
                    val field = View::class.java.getDeclaredField("mContext")
                    field.isAccessible = true
                    field[view] = context
                } catch (_: Throwable) {
                    //  do nothing
                }
            }
        }

        companion object {
            /**
             * Make a standard toast that just contains a text view.
             *
             * @param context  The context to use.  Usually your [Application]
             * or [Activity] object.
             * @param text     The text to show.  Can be formatted text.
             * @param duration How long to display the message.  Either [.LENGTH_SHORT] or
             * [.LENGTH_LONG]
             */
            fun makeText(context: Context, text: CharSequence?, duration: Int): ToastCompat {
                // We cannot pass the ToastContextWrapper to Toast.makeText() because
                // the View will unwrap the base context and we are in vain.
                @SuppressLint("ShowToast, ToastDetector") val toast =
                    Toast.makeText(context, text, duration)
                return ToastCompat(context, toast)
            }

            /**
             * Make a standard toast that just contains a text view with the text from a resource.
             *
             * @param context  The context to use.  Usually your [Application]
             * or [Activity] object.
             * @param resId    The resource id of the string resource to use.  Can be formatted text.
             * @param duration How long to display the message.  Either [.LENGTH_SHORT] or
             * [.LENGTH_LONG]
             * @throws Resources.NotFoundException if the resource can't be found.
             */
            @Throws(Resources.NotFoundException::class)
            fun makeText(context: Context, @StringRes resId: Int, duration: Int): Toast {
                return makeText(context, context.resources.getText(resId), duration)
            }
        }

        /**
         * Construct an empty Toast object.  You must call [.setView] before you
         * can call [.show].
         *
         * @param context The context to use.  Usually your [Application]
         * or [Activity] object.
         * @param base    The base toast
         */
        init {
            setContextCompat(toast.view, ToastContextWrapper(context))
        }
    }

    /**
     * @author Niharika.Arora
     */
    private class ToastContextWrapper(base: Context) : ContextWrapper(base) {

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

}
