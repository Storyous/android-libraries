package com.storyous.commonutils.recyclerView

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.storyous.commonutils.R

class SpanRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.SpanRecyclerView, defStyleAttr, 0)

        val adapterClass = a.getString(R.styleable.SpanRecyclerView_adapter)

        if (adapterClass != null && !isInEditMode) {
            adapter = Class.forName(adapterClass).getConstructor().newInstance() as Adapter<*>?
        }

        if (a.hasValue(R.styleable.SpanRecyclerView_itemOffset)) {
            val itemOffset = a.getDimensionPixelOffset(R.styleable.SpanRecyclerView_itemOffset, 0)
            addItemDecoration(DividerSpaceItemDecoration(itemOffset))
        }

        if (a.hasValue(R.styleable.SpanRecyclerView_itemInnerOffset) ||
            a.hasValue(R.styleable.SpanRecyclerView_itemOuterOffset)) {
            val itemInnerOffset = a.getDimensionPixelOffset(R.styleable.SpanRecyclerView_itemInnerOffset, 0)
            val itemOuterOffset = a.getDimensionPixelOffset(R.styleable.SpanRecyclerView_itemOuterOffset, 0)
            addItemDecoration(DividerSpaceItemDecoration(itemInnerOffset, itemOuterOffset))
        }

        a.recycle()
        // works only if adapter is set view data binding
        updateSpanCount()
    }

    fun updateSpanCount() {
        val gridLayoutManager = layoutManager
        if (gridLayoutManager is GridLayoutManager) {
            if (adapter is SpanSizeAdapter) {
                (adapter as SpanSizeAdapter).spanCount = (gridLayoutManager).spanCount
            }

            gridLayoutManager.spanSizeLookup = UniversalSpanSizeLookup(this)
        }
    }
}
