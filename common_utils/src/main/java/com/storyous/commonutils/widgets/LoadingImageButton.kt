package com.storyous.storyouspay.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.storyous.commonutils.widgets.LoadingOverlayElement

class LoadingImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LoadingOverlayElement(context, attrs, defStyle) {

    private val mButton: ImageButton = ImageButton(getContext(), attrs, defStyle)
    private var mClickListener: OnClickListener? = null

    init {
        mButton.id = android.R.id.button1
        mButton.visibility = View.VISIBLE
        setViewToOverlay(mButton)
        setOverlayListener { this.onButtonClicked(it) }
    }

    private fun onButtonClicked(v: View) {
        if (!isLoading) {
            mClickListener?.onClick(v)
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        mClickListener = listener
    }

    fun setButtonBackgroundColor(@ColorRes resId: Int) {
        mButton.setBackgroundColor(ContextCompat.getColor(context, resId))
    }

    fun setButtonImage(@DrawableRes image: Int) {
        mButton.setImageResource(image)
    }
}
