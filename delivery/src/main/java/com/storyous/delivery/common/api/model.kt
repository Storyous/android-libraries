package com.storyous.delivery.common.api

import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

@SuppressWarnings("ConstructorParameterNaming")
data class DeliveryOrder(
    val orderId: String,
    val deliveryTime: Date,
    val deliveryOnTime: Boolean,
    @DeliveryType val deliveryType: String,
    val discountWithVat: Int?,
    val customer: Customer,
    val desk: Desk?,
    val items: List<DeliveryItem>,
    @DeliveryState val state: String,
    val alreadyPaid: Boolean,
    val autoConfirm: Boolean?,
    val provider: String,
    val note: String?,
    @SerializedName("_lastModifiedAt")
    val lastModifiedAt: Date
) {

    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @StringDef(TYPE_DELIVERY, TYPE_DISPATCH, TYPE_TAKEAWAY, TYPE_TABLE_ORDER)
        annotation class DeliveryType

        const val TYPE_DELIVERY = "delivery"
        const val TYPE_DISPATCH = "dispatch"
        const val TYPE_TAKEAWAY = "takeaway"
        const val TYPE_TABLE_ORDER = "orderToTable"

        @Retention(AnnotationRetention.SOURCE)
        @StringDef(STATE_NEW, STATE_CONFIRMED, STATE_DISPATCHED, STATE_DECLINED)
        annotation class DeliveryState

        const val STATE_NEW = "NEW"
        const val STATE_CONFIRMED = "CONFIRMED"
        const val STATE_DISPATCHED = "DISPATCHED"
        const val STATE_DECLINED = "DECLINED"
    }
}

@Suppress("LongParameterList")
open class DeliveryProduct(
    val itemId: String,
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

@Suppress("LongParameterList")
class DeliveryItem(
    itemId: String,
    productId: String,
    title: String,
    unitPriceWithVat: BigDecimal,
    vatRate: Double,
    vatId: Int,
    measure: String?,
    val count: Double,
    val additions: List<DeliveryAddition>?
) : DeliveryProduct(itemId, productId, title, unitPriceWithVat, vatRate, vatId, measure) {

    override fun toString(): String {
        return "DeliveryItem(count=$count, additions=$additions): ${super.toString()}"
    }
}

@Suppress("LongParameterList")
class DeliveryAddition(
    itemId: String,
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
        return "DeliveryItem(countPerMainItem=$countPerMainItem, additionId=$additionId): ${super.toString()}"
    }
}

data class Customer(
    val name: String,
    val deliveryAddress: String?,
    val phoneNumber: String?
)

data class Desk(
    val deskId: String,
    val code: String,
    val name: String?
)

data class RequestDeclineBody(val reason: String)

data class DeliveryErrorResponse(
    val name: String,
    val error: String,
    val code: Int,
    val httpStatus: Int,
    val order: DeliveryOrder? = null
) {
    companion object {
        val UNKNOWN_ERROR = DeliveryErrorResponse("", "", -1, -1)
    }
}

data class BaseDataResponse<T>(val data: T, val lastModificationAt: String? = null)
