package com.storyous.delivery.common

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.storyous.delivery.common.repositories.DeliveryRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object DeliveryConfiguration {
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

class ConfigurationInvalidException(message: String) : Exception(message)

data class PlaceInfo(val placeId: String, val merchantId: String, val autoConfirm: Boolean)

interface Formatter {
    fun formatCount(count: Double): String
    fun formatPrice(value: Double): String
    fun formatPriceWithoutCurrency(unitPrice: BigDecimal, quantity: Double): String
    fun defaultCurrency(context: Context): String
}

class DefaultFormatter : Formatter {
    companion object {
        private const val SPACE = '\u0020'
        private const val NON_BREAKING_SPACE = '\u00a0'
    }

    private val formatter = (DecimalFormat.getCurrencyInstance() as DecimalFormat).apply {
        minimumFractionDigits = 2
    }

    override fun formatCount(count: Double): String {
        return format(BigDecimal.valueOf(count))
    }

    override fun formatPrice(value: Double): String {
        return format(BigDecimal.valueOf(value))
    }

    override fun formatPriceWithoutCurrency(unitPrice: BigDecimal, quantity: Double): String {
        val value = unitPrice
            .multiply(BigDecimal.valueOf(quantity))
        return format(value)
            .replace(Regex("[^0-9]*$"), "")
            .replace(formatter.positivePrefix, "")
            .trim { it <= ' ' }
    }

    private fun format(value: BigDecimal): String {
        return formatter.format(value.setScale(2, RoundingMode.HALF_UP))
            .replace(NON_BREAKING_SPACE, SPACE)
    }

    override fun defaultCurrency(context: Context): String {
        return context.getString(R.string.default_currency)
    }
}
