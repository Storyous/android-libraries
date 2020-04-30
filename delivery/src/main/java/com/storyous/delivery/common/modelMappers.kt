package com.storyous.delivery.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.storyous.delivery.common.api.model.Customer
import com.storyous.delivery.common.api.model.DeliveryOrder
import com.storyous.delivery.common.db.DeliveryOrderWithCustomer
import com.storyous.delivery.common.db.Customer as CustomerDb
import com.storyous.delivery.common.db.DeliveryOrder as DeliveryOrderDb

fun DeliveryOrder.toDb(merchantId: String, placeId: String): DeliveryOrderWithCustomer {
    val customer = customer.toDb()
    return DeliveryOrderWithCustomer(
        DeliveryOrderDb(
            orderId,
            deliveryTime,
            deliveryOnTime,
            deliveryType,
            discountWithVat,
            deskId,
            items,
            state,
            alreadyPaid,
            autoConfirm,
            provider,
            note,
            lastModifiedAt,
            customer.id,
            merchantId,
            placeId
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
    order.deskId,
    order.items,
    order.state,
    order.alreadyPaid,
    order.autoConfirm,
    order.provider,
    order.note,
    order.lastModifiedAt
)

fun CustomerDb.toApi() = Customer(name, deliveryAddress, phoneNumber)

fun LiveData<List<DeliveryOrderWithCustomer>>.toApi() = Transformations.map(this) {
    it.map { orderWithCustomer -> orderWithCustomer.toApi() }
}
