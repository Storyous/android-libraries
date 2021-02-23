package com.storyous.delivery.common

import android.os.CountDownTimer
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.DeliveryOrder
import java.text.SimpleDateFormat
import java.util.Locale

class AutodeclineCountdown(
    private val visibilityGroup: Group,
    private val textView: TextView,
    private val formatResId: Int,
    private val viewModel: DeliveryViewModel?,
    timeToDecline: Long
) : CountDownTimer(timeToDecline, SECOND_MILLIS) {

    companion object {
        const val TIME_UNIT = 60f
        const val SECOND_MILLIS = 1000L

        fun newInstance(
            deliveryOrder: DeliveryOrder,
            visibilityGroup: Group,
            countdownText: TextView,
            formatResId: Int = 0,
            viewModel: DeliveryViewModel? = null
        ): AutodeclineCountdown? {
            val visible = deliveryOrder.state == DeliveryOrder.STATE_NEW &&
                deliveryOrder.timing?.autoDeclineAfter != null &&
                deliveryOrder.timing.autoDeclineAfter > TimestampUtil.getDate()
            val declined = deliveryOrder.state == DeliveryOrder.STATE_NEW && !visible && formatResId > 0
            visibilityGroup.isVisible = visible || declined
            if (declined) {
                countdownText.text = countdownText.context.getString(R.string.autodecline_declined)
            }
            return if (visible) {
                AutodeclineCountdown(
                    visibilityGroup,
                    countdownText,
                    formatResId,
                    viewModel,
                    -TimestampUtil.getTimeAgo(deliveryOrder.timing!!.autoDeclineAfter!!.time)
                ).apply { start() }
            } else {
                null
            }
        }
    }

    override fun onTick(millisUntilFinished: Long) {
        val time = getRemainingTime(millisUntilFinished)
        textView.text = if (formatResId > 0) {
            textView.context.getString(formatResId, time)
        } else {
            time
        }
    }

    override fun onFinish() {
        if (formatResId > 0) {
            textView.text = textView.context.getString(R.string.autodecline_declined)
        } else {
            visibilityGroup.isVisible = false
        }
        viewModel?.selectedOrderId = viewModel?.selectedOrderId
    }

    private fun getRemainingTime(millisUntilFinished: Long): String {
        return SimpleDateFormat(
            if (millisUntilFinished > TIME_UNIT * TIME_UNIT * SECOND_MILLIS) {
                "H'h':mm'm'"
            } else {
                "m:ss"
            },
            Locale.US).format(millisUntilFinished)
    }
}
