package com.storyous.delivery.common

import com.storyous.delivery.common.api.model.Customer
import com.storyous.delivery.common.api.model.DeliveryAddition
import com.storyous.delivery.common.api.model.DeliveryItem
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.db.Customer as CustomerDb
import com.storyous.delivery.common.db.DeliveryAddition as DeliveryAdditionDb
import com.storyous.delivery.common.db.DeliveryItem as DeliveryItemDb
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb() = DeliveryOrderDb(
    orderId,
    deliveryTime,
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    customer.toDb(),
    items.map { it.toDb(orderId) },
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt
)

fun Customer.toDb() = CustomerDb(name, deliveryAddress, phoneNumber)

fun DeliveryItem.toDb(orderId: String) = DeliveryItemDb(
    itemId,
    orderId,
    productId,
    title,
    unitPriceWithVat,
    vatRate,
    vatId,
    measure,
    count,
    additions?.map { it.toDb(itemId) })

fun DeliveryAddition.toDb(parentItemId: String) = DeliveryAdditionDb(
    itemId,
    parentItemId,
    productId,
    title,
    unitPriceWithVat,
    vatRate,
    vatId,
    measure,
    countPerMainItem,
    additionId
)

fun DeliveryOrderDb.toApi() = DeliveryOrder(
    orderId,
    deliveryTime,
    deliveryOnTime,
    deliveryType,
    discountWithVat,
    customer!!.toApi(),
    items.map { it.toApi() },
    state,
    alreadyPaid,
    autoConfirm,
    provider,
    note,
    lastModifiedAt
)

fun CustomerDb.toApi() = Customer(name, deliveryAddress, phoneNumber)

fun DeliveryItemDb.toApi() = DeliveryItem(
    itemId,
    orderId,
    title,
    unitPriceWithVat,
    vatRate,
    vatId,
    measure,
    count,
    additions?.map { it.toApi() })

fun DeliveryAdditionDb.toApi() = DeliveryAddition(
    itemId,
    productId,
    title,
    unitPriceWithVat,
    vatRate,
    vatId,
    measure,
    countPerMainItem,
    additionId
)
