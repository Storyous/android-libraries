package com.storyous.delivery.common

import com.storyous.delivery.common.api.model.Customer
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.db.DeliveryOrderWithCustomer
import com.storyous.delivery.common.db.Customer as CustomerDb
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb(): DeliveryOrderWithCustomer {
    val customer = customer.toDb()
    return DeliveryOrderWithCustomer(
        DeliveryOrderDb(
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
            customer.id
        ),
        customer
    )
}

fun Customer.toDb() = CustomerDb(name, deliveryAddress, phoneNumber)

fun DeliveryOrderWithCustomer.toApi() = DeliveryOrder(
    order.orderId,
    order.deliveryTime,
    order.deliveryOnTime,
    order.deliveryType,
    order.discountWithVat,
    customer.toApi(),
    order.items,
    order.state,
    order.alreadyPaid,
    order.autoConfirm,
    order.provider,
    order.note,
    order.lastModifiedAt
)

fun CustomerDb.toApi() = Customer(name, deliveryAddress, phoneNumber)
