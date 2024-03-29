package com.storyous.delivery.common

import android.content.Context
import android.view.ViewStub
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.google.gson.GsonBuilder
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Date

object DeliveryConfiguration : DeliveryOrderFunctions by DefaultOrderFunctions {
    var deliveryModel: DeliveryModel = DeliveryModel()
    var deliveryRepository: DeliveryRepository? = null
    var placeInfo: PlaceInfo? = null
    var formatter: Formatter = DefaultFormatter()
    var onActivityToolbarCreate: (Toolbar, FragmentManager) -> Unit = { _, _ -> }
    var onCreateActionButton: (ViewStub, FragmentManager) -> Unit = { _, _ -> }
    var onUnauthorizedError: () -> Unit = {}
    var globalDispatchDisabled = false to ""
    var showSettingsBar = false
    var gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
    var printBillBy: suspend (orderId: String) -> Unit = {}

    @Throws(ConfigurationInvalidException::class)
    fun checkValid() {
        if (deliveryRepository == null) {
            throw ConfigurationInvalidException("Delivery repository must be set before running the activity")
        }
        if (placeInfo == null) {
            throw ConfigurationInvalidException("Delivery place info must be set before running the activity")
        }
    }
}

class ConfigurationInvalidException(message: String) : RuntimeException(message)

data class PlaceInfo(val placeId: String, val merchantId: String, val autoConfirmEnabled: Boolean)

interface Formatter {
    fun formatCount(count: Double): String
    fun formatPrice(price: BigDecimal): String
    fun formatPriceWithoutCurrency(price: BigDecimal): String
    fun defaultCurrency(context: Context): String
}

class DefaultFormatter : Formatter {
    companion object {
        private const val SPACE = '\u0020'
        private const val NON_BREAKING_SPACE = '\u00a0'
    }

    private val numberFormatter = DecimalFormat.getInstance() as DecimalFormat
    private val currencyFormatter = (DecimalFormat.getCurrencyInstance() as DecimalFormat).apply {
        minimumFractionDigits = 2
    }

    override fun formatCount(count: Double): String {
        return numberFormatter.format(BigDecimal.valueOf(count))
    }

    override fun formatPrice(price: BigDecimal): String {
        return format(price)
    }

    override fun formatPriceWithoutCurrency(price: BigDecimal): String {
        return format(price)
            .replace(Regex("[^0-9]*$"), "")
            .replace(currencyFormatter.positivePrefix, "")
            .trim { it <= ' ' }
    }

    private fun format(value: BigDecimal): String {
        return currencyFormatter.format(value.setScale(2, RoundingMode.HALF_UP))
            .replace(NON_BREAKING_SPACE, SPACE)
    }

    override fun defaultCurrency(context: Context): String {
        return context.getString(R.string.default_currency)
    }
}

interface DeliveryOrderFunctions {
    var acceptVisible: suspend (DeliveryOrder) -> Boolean
    var acceptEnabled: suspend (DeliveryOrder) -> Boolean
    var dispatchVisible: suspend (DeliveryOrder) -> Boolean
    var dispatchEnabled: suspend (DeliveryOrder) -> Boolean
    var printBillVisible: suspend (DeliveryOrder) -> Boolean
    var printBillEnabled: suspend (DeliveryOrder) -> Boolean
}

object DefaultOrderFunctions : DeliveryOrderFunctions {
    override var acceptVisible: suspend (DeliveryOrder) -> Boolean = {
        it.state == DeliveryOrder.STATE_NEW
    }

    override var acceptEnabled: suspend (DeliveryOrder) -> Boolean = {
        it.state == DeliveryOrder.STATE_NEW &&
            it.timing?.autoDeclineAfter ?: Date(Long.MAX_VALUE) > TimestampUtil.getDate()
    }

    override var dispatchVisible: suspend (DeliveryOrder) -> Boolean = {
        it.state == DeliveryOrder.STATE_CONFIRMED
    }

    override var dispatchEnabled: suspend (DeliveryOrder) -> Boolean = {
        it.state == DeliveryOrder.STATE_CONFIRMED
    }

    override var printBillVisible: suspend (DeliveryOrder) -> Boolean = { false }

    override var printBillEnabled: suspend (DeliveryOrder) -> Boolean = { false }
}
