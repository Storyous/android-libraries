package com.storyous.commonutils.toaster

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import timber.log.Timber

object Toaster {

    @JvmStatic
    @JvmOverloads
    fun showLong(context: Context, resId: Int, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return showInternal(context, resId, Toast.LENGTH_LONG, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showShort(context: Context, resId: Int, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return showInternal(context, resId, Toast.LENGTH_SHORT, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showLong(context: Context, text: String, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return showInternal(context, text, Toast.LENGTH_LONG, applyBlock)
    }

    @JvmStatic
    @JvmOverloads
    fun showShort(context: Context, text: String, applyBlock: ((Toast) -> Unit) = {}): Toast {
        return showInternal(context, text, Toast.LENGTH_SHORT, applyBlock)
    }

    @JvmStatic
    fun show(context: Context, text: String, duration: Int): Toast {
        return showInternal(context, text, duration, {})
    }

    @SuppressLint("ToastDetector")
    fun makeText(context: Context, text: String, length: Int): Toast {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            ToastCompat.makeText(context, text, length)
        } else {
            Toast.makeText(context, text, length)
        }
    }

    private fun showInternal(
        context: Context,
        resId: Int,
        duration: Int,
        applyBlock: ((Toast) -> Unit) = {}
    ) = showInternal(context, context.resources.getText(resId).toString(), duration, applyBlock)


    private fun showInternal(
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


}
