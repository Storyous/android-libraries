package com.storyous.delivery.common.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Entity
open class DeliveryOrder @JvmOverloads constructor(
    @PrimaryKey var orderId: String,
    var deliveryTime: Date,
    var deliveryOnTime: Boolean,
    var deliveryType: String,
    var discountWithVat: Int?,
    @Ignore var customer: Customer? = null,
    @Ignore var items: List<DeliveryItem> = listOf(),
    var state: String,
    var alreadyPaid: Boolean,
    var autoConfirm: Boolean?,
    var provider: String,
    var note: String,
    var lastModifiedAt: Date,
    var customerId: String? = customer?.id
)

@Entity
open class DeliveryProduct(
    @PrimaryKey val itemId: String,
    val productId: String,
    val title: String,
    val unitPriceWithVat: BigDecimal,
    val vatRate: Double,
    val vatId: Int,
    val measure: String?
) {
    @Suppress("MaxLineLength")
    override fun toString(): String {
        return "DeliveryProduct(itemId=$itemId, productId=$productId, title: $title, unitPriceWithVat: $unitPriceWithVat, vatRate=$vatRate, vatId=$vatId, measure=$measure)"
    }
}

@Entity
class DeliveryItem @JvmOverloads constructor(
    itemId: String,
    val orderId: String,
    productId: String,
    title: String,
    unitPriceWithVat: BigDecimal,
    vatRate: Double,
    vatId: Int,
    measure: String?,
    val count: Double,
    @Ignore var additions: List<DeliveryAddition>? = null
) : DeliveryProduct(itemId, productId, title, unitPriceWithVat, vatRate, vatId, measure) {

    override fun toString(): String {
        return "DeliveryItem(orderId=$orderId, count=$count, additions=$additions): ${super.toString()}"
    }
}

@Entity
class DeliveryAddition(
    itemId: String,
    val parentItemId: String,
    productId: String,
    title: String,
    unitPriceWithVat: BigDecimal,
    vatRate: Double,
    vatId: Int,
    measure: String?,
    val countPerMainItem: Int,
    val additionId: String? = null
) : DeliveryProduct(itemId, productId, title, unitPriceWithVat, vatRate, vatId, measure) {

    override fun toString(): String {
        return "DeliveryItem(parentItemId=$parentItemId, countPerMainItem=$countPerMainItem, additionId=$additionId): ${super.toString()}"
    }
}

@Entity
open class Customer(
    val name: String,
    val deliveryAddress: String?,
    val phoneNumber: String,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)
