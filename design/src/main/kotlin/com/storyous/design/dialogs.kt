package com.storyous.design

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MEASURE_DEBOUNCE = 500L
private const val REPAINT_DELAY = 10L

private var View.measureJob: Job?
    get() = runCatching { getTag(R.id.measure_job) as? Job }.getOrNull()
    set(value) = setTag(R.id.measure_job, value)
private var View.lastMeasuredHeight: Int?
    get() = runCatching { getTag(R.id.last_measured_height) as? Int }.getOrNull()
    set(value) = setTag(R.id.last_measured_height, value)

/**
 * fix wrong measured height of NestedScrollView in dialog
 */
fun DialogFragment.updateHeightOnChange(
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    dialog?.window?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
        dialog?.window?.decorView?.run {
            measureJob = measureJob?.takeIf { it.isActive }
                ?: coroutineScope.launch { measureHeight(this@run) }
        }
    }
}

private suspend fun measureHeight(view: View) {
    delay(MEASURE_DEBOUNCE)

    val tag = view.context.getString(R.string.measureViewTag)
    val measureView = view.findViewWithTag<ViewGroup>(tag) ?: return
    val parent = measureView.parent as View

    if (parent.lastMeasuredHeight != parent.measuredHeight) {
        parent.lastMeasuredHeight = parent.measuredHeight
        parent.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        parent.invalidate()
        parent.requestLayout()
        delay(REPAINT_DELAY)
    }

    val scrollViewHeight = measureView.measuredHeight
    val scrollViewContentHeight = measureView.getChildAt(0)?.measuredHeight ?: 0
    if (scrollViewHeight > scrollViewContentHeight) {
        parent.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        parent.invalidate()
        parent.requestLayout()
        delay(REPAINT_DELAY)
    }
}
