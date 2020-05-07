package com.storyous.delivery.common

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.repositories.DeliveryRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object DeliveryConfiguration : DeliveryOrderFunctions by DefaultOrderFunctions {
    var deliveryModel: DeliveryModel = DeliveryModel()
    var deliveryRepository: DeliveryRepository? = null
    var placeInfo: PlaceInfo? = null
    var formatter: Formatter = DefaultFormatter()
    var onActivityToolbarCreate: (Toolbar, FragmentManager) -> Unit = { _, _ -> }

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
}

object DefaultOrderFunctions : DeliveryOrderFunctions {
    override var acceptVisible: suspend (DeliveryOrder) -> Boolean = { order ->
        order.state == DeliveryOrder.STATE_NEW
    }

    override var acceptEnabled: suspend (DeliveryOrder) -> Boolean = { order ->
        order.state == DeliveryOrder.STATE_NEW
    }

    override var dispatchVisible: suspend (DeliveryOrder) -> Boolean = { order ->
        order.state != DeliveryOrder.STATE_NEW
    }

    override var dispatchEnabled: suspend (DeliveryOrder) -> Boolean = { order ->
        order.state == DeliveryOrder.STATE_CONFIRMED
    }
}
