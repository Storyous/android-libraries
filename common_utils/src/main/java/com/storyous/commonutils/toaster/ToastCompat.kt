package com.storyous.commonutils.toaster

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author Niharika.Arora
 */
@Suppress("TooManyFunctions")
// TODO Remove after min SDK > 25
internal class ToastCompat(context: Context, private val toast: Toast) : Toast(context) {

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
