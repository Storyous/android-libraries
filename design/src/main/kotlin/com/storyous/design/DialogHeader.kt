package com.storyous.design

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible

class DialogHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val title by lazy { findViewById<AppCompatTextView>(R.id.title) }
    private val close by lazy { findViewById<AppCompatImageButton>(R.id.closeDialogButton) }

    init {
        inflate(context, R.layout.dialog_header_layout, this)

        orientation = HORIZONTAL
        setVerticalGravity(Gravity.CENTER_VERTICAL)

        context.obtainStyledAttributes(attrs, R.styleable.DialogHeader).use {
            setBackgroundResource(
                it.getResourceId(
                    R.styleable.DialogHeader_android_background,
                    R.color.dialog_header_background
                )
            )
            minimumHeight = it.getDimensionPixelSize(
                R.styleable.DialogHeader_android_minHeight,
                resources.getDimensionPixelSize(R.dimen.dialog_header_min_height)
            )
            elevation = it.getDimension(
                R.styleable.DialogHeader_android_elevation,
                resources.getDimension(R.dimen.elevation_toolbar)
            )
            setTitle(it.getText(R.styleable.DialogHeader_android_text))
        }
    }

    fun setTitle(text: CharSequence?) {
        title.text = text ?: if (isInEditMode) "Title" else ""
    }

    fun setCloseClickListener(listener: ((View) -> Unit)?) {
        close.apply {
            isVisible = listener != null
            setOnClickListener(listener)
        }
    }
}
