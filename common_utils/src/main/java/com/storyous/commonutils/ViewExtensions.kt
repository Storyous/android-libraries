package com.storyous.commonutils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View

fun View.show(show: Boolean) {
    this.visibility = if (show) View.VISIBLE else View.GONE
}

fun SpannableStringBuilder.append(text: String?, style: Int): SpannableStringBuilder {
    if (text == null) {
        return this
    }
    append(text).setSpan(StyleSpan(style), length - text.length, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}
