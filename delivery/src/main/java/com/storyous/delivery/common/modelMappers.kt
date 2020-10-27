package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.storyous.commonutils.DateUtils
import com.storyous.commonutils.TimestampUtil
import com.storyous.delivery.common.api.Customer
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
    Customer(customerName, customerDeliveryAddress, customerPhoneNumber, customerEmail),
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

fun LiveData<List<DeliveryOrderDb>>.toApi() = Transformations.map(this) {
    it.map { order -> order.toApi() }
}
