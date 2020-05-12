@file:Suppress("MatchingDeclarationName")

package com.storyous.delivery.common.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.storyous.delivery.common.api.DeliveryItem
import java.util.Date

@Suppress("LongParameterList")
@Entity
open class DeliveryOrder(
    @PrimaryKey var orderId: String,
    var deliveryTime: Date,
    var deliveryOnTime: Boolean,
    var deliveryType: String,
    var discountWithVat: Int?,
    var items: List<DeliveryItem> = listOf(),
    var state: String,
    var alreadyPaid: Boolean,
    var autoConfirm: Boolean?,
    var provider: String,
    var note: String?,
    var lastModifiedAt: Date,
    var customerName: String,
    var customerDeliveryAddress: String?,
    var customerPhoneNumber: String?,
    var deskId: String?,
    var deskCode: String?,
    var deskName: String?
)
