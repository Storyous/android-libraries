package com.storyous.commonutils.recyclerView

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UniversalSpanSizeLookup(recyclerView: RecyclerView) : GridLayoutManager.SpanSizeLookup() {

    private val spanSizeAdapter by lazy { recyclerView.adapter as? SpanSizeAdapter }

    init {
        isSpanIndexCacheEnabled = true
    }

    override fun getSpanSize(position: Int) = spanSizeAdapter?.getSpanSize(position) ?: 1
}
