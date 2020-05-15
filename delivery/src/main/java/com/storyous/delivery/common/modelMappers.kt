package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.storyous.delivery.common.api.Customer
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.Desk
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb() = DeliveryOrderDb(
    orderId,
    deliveryTime,
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
    desk?.name
)

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
    lastModifiedAt
)

fun LiveData<List<DeliveryOrderDb>>.toApi() = Transformations.map(this) {
    it.map { order -> order.toApi() }
}
