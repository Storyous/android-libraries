package com.storyous.commonutils.recyclerView

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

@Suppress("SpreadOperator")
fun RecyclerView.ViewHolder.getString(@StringRes resId: Int, vararg args: Any): String {
    return itemView.context.getString(resId, *args)
}
