package com.storyous.delivery.common.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.storyous.delivery.common.api.model.DeliveryItem
import java.util.Date
import java.util.UUID

@Suppress("LongParameterList")
@Entity
open class DeliveryOrder @JvmOverloads constructor(
    @PrimaryKey var orderId: String,
    var deliveryTime: Date,
    var deliveryOnTime: Boolean,
    var deliveryType: String,
    var discountWithVat: Int?,
    @Ignore var customer: Customer? = null,
    var items: List<DeliveryItem> = listOf(),
    var state: String,
    var alreadyPaid: Boolean,
    var autoConfirm: Boolean?,
    var provider: String,
    var note: String?,
    var lastModifiedAt: Date,
    var customerId: String? = customer?.id
)

@Entity
open class Customer(
    val name: String,
    val deliveryAddress: String?,
    val phoneNumber: String?,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)
