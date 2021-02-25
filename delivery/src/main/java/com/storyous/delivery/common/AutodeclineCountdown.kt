package com.storyous.delivery.common

import android.os.CountDownTimer
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.DeliveryOrder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class AutodeclineCountdown(
    timeToDecline: Long,
    private val textView: TextView,
    @StringRes private val formatResId: Int?,
    private val onTimesUp: () -> Unit = {}
) : CountDownTimer(timeToDecline, TimeUnit.SECONDS.toMillis(1)) {

    companion object {
        private val hoursFormat = SimpleDateFormat("H'h':mm'm'", Locale.US)
        private val minutesFormat = SimpleDateFormat("m:ss", Locale.US)

        fun newInstance(
            deliveryOrder: DeliveryOrder,
            countdownText: TextView,
            @StringRes formatResId: Int? = null,
            onTimesUp: () -> Unit = {}
        ): AutodeclineCountdown? {

            val countdownEnabled = deliveryOrder.state == DeliveryOrder.STATE_NEW
            val millisToAutoDecline = deliveryOrder.timing?.autoDeclineAfter?.let {
                it.time - TimestampUtil.getDate().time
            } ?: 0L

            countdownText.isVisible = countdownEnabled

            return if (millisToAutoDecline <= 0L || !countdownEnabled) {
                countdownText.setText(R.string.autodecline_declined)
                null
            } else {
                AutodeclineCountdown(
                    millisToAutoDecline,
                    countdownText,
                    formatResId,
                    onTimesUp
                ).apply { start() }
            }
        }
    }

    override fun onTick(millisUntilFinished: Long) {
        val time = getRemainingTime(millisUntilFinished)
        with(textView) {
            text = formatResId?.let { context.getString(it, time) } ?: time
            isVisible = true
        }
    }

    override fun onFinish() {
        with(textView) {
            setText(R.string.autodecline_declined)
            isVisible = formatResId != null
        }
        onTimesUp()
    }

    private fun getRemainingTime(millisUntilFinished: Long): String {
        return if (TimeUnit.MILLISECONDS.toHours(millisUntilFinished) > 1) {
            hoursFormat
        } else {
            minutesFormat
        }.format(millisUntilFinished)
    }
}
