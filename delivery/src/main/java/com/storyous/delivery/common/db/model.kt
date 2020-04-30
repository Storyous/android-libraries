package com.storyous.delivery.common.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.storyous.delivery.common.api.model.DeliveryItem
import java.util.Date
import java.util.UUID

@Suppress("LongParameterList")
@Entity
open class DeliveryOrder(
    @PrimaryKey var orderId: String,
    var deliveryTime: Date,
    var deliveryOnTime: Boolean,
    var deliveryType: String,
    var discountWithVat: Int?,
    var deskId: String?,
    var items: List<DeliveryItem> = listOf(),
    var state: String,
    var alreadyPaid: Boolean,
    var autoConfirm: Boolean?,
    var provider: String,
    var note: String?,
    var lastModifiedAt: Date,
    var customerId: String?,
    var merchantId: String,
    var placeId: String
)

@Entity
open class Customer(
    val name: String,
    val deliveryAddress: String?,
    val phoneNumber: String?,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)

data class DeliveryOrderWithCustomer(
    @Embedded
    val order: DeliveryOrder,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: Customer
)
