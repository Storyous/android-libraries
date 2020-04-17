package com.storyous.commonutils.recyclerView

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil

class DividerSpaceItemDecoration(
    private val innerOffsetSize: Int = 0,
    private val outerOffsetSize: Int = 0
) : RecyclerView.ItemDecoration() {

    constructor(offsetSize: Int = 0) : this(offsetSize, offsetSize)

    private val halfInnerOffsetSize = ceil(innerOffsetSize / 2.0).toInt()

    @Suppress("ComplexMethod")
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val layoutManager = parent.layoutManager

        if (layoutManager is GridLayoutManager) {
            val spanCount = layoutManager.spanCount
            val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)
            val spanIndex = layoutManager.spanSizeLookup.getSpanIndex(position, spanCount)

            val firstRow =
                isInFirstRow(position, spanCount, layoutManager.spanSizeLookup, state.itemCount)
            val lastRow =
                isInLastRow(position, spanCount, layoutManager.spanSizeLookup, state.itemCount)
            val firstCol = spanIndex == 0
            val lastCol = spanIndex + spanSize == spanCount

            outRect.top = if (firstRow) outerOffsetSize else halfInnerOffsetSize
            outRect.left = if (firstCol) outerOffsetSize else halfInnerOffsetSize
            outRect.bottom = if (lastRow) outerOffsetSize else halfInnerOffsetSize
            outRect.right = if (lastCol) outerOffsetSize else halfInnerOffsetSize
        } else {
            val firstRow = position == 0
            val lastRow = position == state.itemCount - 1

            outRect.top = if (firstRow) outerOffsetSize else innerOffsetSize
            outRect.left = outerOffsetSize
            outRect.right = outerOffsetSize
            outRect.bottom = if (lastRow) outerOffsetSize else 0
        }
    }

    private fun isInFirstRow(
        position: Int,
        spanCount: Int,
        spanSizeLookup: GridLayoutManager.SpanSizeLookup,
        itemCount: Int
    ): Boolean {
        return if (position > spanCount) {
            false
        } else {
            getItemsFirstRowPositions(spanCount, spanSizeLookup, itemCount).contains(position)
        }
    }

    private fun isInLastRow(
        position: Int,
        spanCount: Int,
        spanSizeLookup: GridLayoutManager.SpanSizeLookup,
        itemCount: Int
    ): Boolean {
        return if (position < itemCount - spanCount) {
            false
        } else {
            getItemsLastRowPositions(spanCount, spanSizeLookup, itemCount).contains(position)
        }
    }

    private fun getItemsLastRowPositions(
        spanCount: Int,
        spanSizeLookup: GridLayoutManager.SpanSizeLookup,
        itemCount: Int
    ): List<Int> {
        val lastRowPositions = mutableListOf<Int>()
        var lastIndex = itemCount
        for (position in itemCount - 1 downTo 0) {
            val index = spanSizeLookup.getSpanIndex(position, spanCount)
            if (index < lastIndex) {
                lastRowPositions.add(position)
                lastIndex = index
            } else {
                break
            }
        }
        return lastRowPositions
    }

    private fun getItemsFirstRowPositions(
        spanCount: Int,
        spanSizeLookup: GridLayoutManager.SpanSizeLookup,
        itemCount: Int
    ): List<Int> {
        val lastRowPositions = mutableListOf<Int>()
        var lastIndex = -1
        for (position in 0 until itemCount) {
            val index = spanSizeLookup.getSpanIndex(position, spanCount)
            if (index > lastIndex) {
                lastRowPositions.add(position)
                lastIndex = index
            } else {
                break
            }
        }
        return lastRowPositions
    }
}
