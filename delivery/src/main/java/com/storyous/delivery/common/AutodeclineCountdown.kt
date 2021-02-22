package com.storyous.delivery.common

import android.os.CountDownTimer
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.DeliveryOrder
import kotlin.math.floor

class AutodeclineCountdown(
    private val textView: TextView,
    timeToDecline: Long
) : CountDownTimer(timeToDecline, SECOND_MILLIS) {

    companion object {
        const val TIME_UNIT = 60f
        const val SECOND_MILLIS = 1000L

        fun init(deliveryOrder: DeliveryOrder, visibilityGroup: Group, countdownText: TextView): AutodeclineCountdown? {
            val visible = deliveryOrder.state == DeliveryOrder.STATE_NEW &&
                deliveryOrder.timing?.autoDeclineAfter != null &&
                deliveryOrder.timing.autoDeclineAfter > TimestampUtil.getDate()
            visibilityGroup.isVisible = visible
            return if (visible) {
                AutodeclineCountdown(
                    countdownText,
                    TimestampUtil.getTimeAgo(deliveryOrder.timing!!.autoDeclineAfter!!.time)
                ).apply { start() }
            } else {
                null
            }
        }
    }

    override fun onTick(millisUntilFinished: Long) {
        textView.text = getRemainingTime(millisUntilFinished / SECOND_MILLIS)
    }

    override fun onFinish() {
        textView.text = "--:--"
    }

    private fun getRemainingTime(secondsUntilFinished: Long): String {
        val hours = floor(secondsUntilFinished / TIME_UNIT / TIME_UNIT).toInt()
        val minutes = (floor(secondsUntilFinished / TIME_UNIT) - hours * TIME_UNIT).toInt()
        val seconds = (secondsUntilFinished - hours * TIME_UNIT * TIME_UNIT - minutes * TIME_UNIT).toInt()
        return if (hours > 0) {
            textView.context.getString(
                R.string.autodecline_hours,
                hours.toString(),
                minutes.toString()
            )
        } else {
            textView.context.getString(
                R.string.autodecline_minutes,
                minutes.toString(),
                seconds.toString().padStart(2, '0')
            )
        }
    }
}
