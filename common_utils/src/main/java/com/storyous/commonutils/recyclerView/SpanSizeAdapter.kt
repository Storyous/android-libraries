package com.storyous.commonutils.recyclerView

interface SpanSizeAdapter {

    var spanCount: Int

    fun getSpanSize(position: Int): Int
}
