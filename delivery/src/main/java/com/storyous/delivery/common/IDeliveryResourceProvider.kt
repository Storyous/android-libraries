package com.storyous.delivery.common

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.storyous.delivery.common.repositories.DeliveryRepository
import java.math.BigDecimal

interface IDeliveryResourceProvider {
    var deliveryModel: DeliveryModel
    val deliveryRepository: DeliveryRepository
    fun getMerchantId(): String
    fun getPlaceId(): String
    fun canAutoConfirm(): Boolean
    fun formatCount(count: Double): String
    fun formatPrice(value: Double): String
    fun formatPriceWithoutCurrency(unitPrice: BigDecimal, quantity: Double): String
    fun defaultCurrency(): String
    fun onActivityToolbarCreate(toolbar: Toolbar, supportFragmentManager: FragmentManager)
}
