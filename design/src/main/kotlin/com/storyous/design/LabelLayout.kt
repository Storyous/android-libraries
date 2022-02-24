package com.storyous.design

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.use

open class LabelLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    private val label: AppCompatTextView = LayoutInflater.from(context)
        .inflate(R.layout.label, this, false) as AppCompatTextView

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LabelLayout).use {
            setText(it.getString(R.styleable.LabelLayout_android_text))
            orientation = it.getInt(R.styleable.LabelLayout_android_orientation, VERTICAL)
        }
    }

    fun setText(text: String?) {
        label.text = text
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (getChildAt(0) != label) {
            removeView(label)
            super.addView(label, 0, params)
            baselineAlignedChildIndex = 0
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> getChild(
        index: Int = 1
    ): T? = runCatching { getChildAt(index) as? T }.getOrNull()
}

class TextInputLabelLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LabelLayout(context, attrs) {

    val editText: EditText? get() = getChild(1)
}
