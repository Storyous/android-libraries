package com.storyous.delivery.common

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.Customer
import com.storyous.delivery.common.api.DeliveryAddressParts
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.DeliveryTiming
import com.storyous.delivery.common.api.Desk
import java.util.Date
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb() = DeliveryOrderDb(
    orderId,
    getSignificantTime(),
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    items,
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt,
    customer.name,
    customer.deliveryAddress,
    customer.deliveryAddressParts?.latitude,
    customer.deliveryAddressParts?.longitude,
    customer.phoneNumber,
    customer.email,
    desk?.deskId,
    desk?.code,
    desk?.name,
    tipWithVat,
    timing
)

fun DeliveryOrder.getSignificantTime(): Date {
    return when (timing?.showTime()) {
        DeliveryTiming.SHOW_ESTIMATED_PICKUP -> timing.estimatedPickupTime?.to
        DeliveryTiming.SHOW_REQUESTED_PICKUP -> timing.requestedPickupTime?.to
        DeliveryTiming.SHOW_MEAL_READY -> timing.estimatedMealReadyTime
        DeliveryTiming.SHOW_ESTIMATED_DELIVERY -> timing.estimatedDeliveryTime?.to
        DeliveryTiming.SHOW_REQUESTED_DELIVERY -> timing.requestedDeliveryTime?.to
        DeliveryTiming.SHOW_ASAP -> TimestampUtil.INSTANCE.now
        DeliveryTiming.SHOW_NOTHING -> TimestampUtil.INSTANCE.now
        else -> deliveryTime
    } ?: TimestampUtil.INSTANCE.now
}

fun DeliveryOrderDb.toApi() = DeliveryOrder(
    orderId,
    deliveryTime,
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    Customer(
        customerName,
        customerDeliveryAddress,
        DeliveryAddressParts(customerDeliveryAddressLat, customerDeliveryAddressLong),
        customerPhoneNumber,
        customerEmail
    ),
    deskId?.let { id -> deskCode?.let { code -> Desk(id, code, deskName) } },
    items,
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt,
    tipWithVat,
    timing
)

fun LiveData<List<DeliveryOrderDb>>.toApi() = map {
    it.map { order -> order.toApi() }
}

enum class IntegratedDispatch(val apiKey: String, val resId: Int) {
    ENABLED("enabled", R.string.settings_yes),
    ASK("ask", R.string.settings_ask),
    DISABLED("disabled", R.string.settings_no);

    companion object {
        fun valueFromApiKey(apiKey: String, context: Context): String {
            return values().firstOrNull { it.apiKey == apiKey }?.resId?.let { context.getString(it) }
                ?: context.getString(DISABLED.resId)
        }

        fun apiKeyFromValue(value: String, context: Context): String {
            return values().firstOrNull { context.getString(it.resId) == value }?.apiKey
                ?: DISABLED.apiKey
        }
    }
}
