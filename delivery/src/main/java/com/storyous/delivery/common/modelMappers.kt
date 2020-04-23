package com.storyous.delivery.common

import com.storyous.delivery.common.api.model.Customer
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.db.Customer as CustomerDb
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb() = DeliveryOrderDb(
    orderId,
    deliveryTime,
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    customer.toDb(),
    items,
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt
)

fun Customer.toDb() = CustomerDb(name, deliveryAddress, phoneNumber)


fun DeliveryOrderDb.toApi() = DeliveryOrder(
    orderId,
    deliveryTime,
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    customer!!.toApi(),
    items,
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt
)

fun CustomerDb.toApi() = Customer(name, deliveryAddress, phoneNumber)

