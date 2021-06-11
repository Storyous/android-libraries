package com.storyous.delivery.common.api

import androidx.annotation.IntDef
import androidx.annotation.StringDef
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

@SuppressWarnings("ConstructorParameterNaming")
data class DeliveryOrder(
    val orderId: String,
    val deliveryTime: Date?,
    val deliveryOnTime: Boolean?,
    @DeliveryType val deliveryType: String,
    val discountWithVat: BigDecimal?,
    val customer: Customer,
    val desk: Desk?,
    val items: List<DeliveryItem>,
    @DeliveryState val state: String,
    val alreadyPaid: Boolean,
    val autoConfirm: Boolean?,
    val provider: String,
    val note: String?,
    @SerializedName("_lastModifiedAt")
    val lastModifiedAt: Date,
    val tipWithVat: BigDecimal?,
    val timing: DeliveryTiming?
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
        @StringDef(
            STATE_NEW,
            STATE_SCHEDULING_DELIVERY,
            STATE_CONFIRMED,
            STATE_DISPATCHED,
            STATE_DECLINED
        )
        annotation class DeliveryState

        const val STATE_NEW = "NEW"
        const val STATE_SCHEDULING_DELIVERY = "SCHEDULING_DELIVERY"
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
    val deliveryAddressParts: DeliveryAddressParts?,
    val phoneNumber: String?,
    var email: String?
)

data class DeliveryAddressParts(
    val latitude: Double?,
    val longitude: Double?
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

data class DeliveryTiming(
    val asSoonAsPossible: Boolean,
    val requestedDeliveryTime: DeliveryDateRange?,
    val requestedPickupTime: DeliveryDateRange?,
    val estimatedMealReadyTime: Date?,
    val estimatedPickupTime: DeliveryDateRange?,
    val estimatedDeliveryTime: DeliveryDateRange?,
    val autoDeclineAfter: Date?
) {
    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            SHOW_ESTIMATED_PICKUP, SHOW_REQUESTED_PICKUP, SHOW_MEAL_READY,
            SHOW_ESTIMATED_DELIVERY, SHOW_REQUESTED_DELIVERY, SHOW_ASAP, SHOW_NOTHING
        )
        annotation class TimeDisplayType

        const val SHOW_ESTIMATED_PICKUP = 0
        const val SHOW_REQUESTED_PICKUP = 1
        const val SHOW_MEAL_READY = 2
        const val SHOW_ESTIMATED_DELIVERY = 3
        const val SHOW_REQUESTED_DELIVERY = 4
        const val SHOW_ASAP = 5
        const val SHOW_NOTHING = 6
    }

    @TimeDisplayType
    fun showTime(): Int {
        return when {
            estimatedPickupTime != null -> SHOW_ESTIMATED_PICKUP
            requestedPickupTime != null -> SHOW_REQUESTED_PICKUP
            estimatedMealReadyTime != null -> SHOW_MEAL_READY
            estimatedDeliveryTime != null -> SHOW_ESTIMATED_DELIVERY
            requestedDeliveryTime != null -> SHOW_REQUESTED_DELIVERY
            asSoonAsPossible -> SHOW_ASAP
            else -> SHOW_NOTHING
        }
    }

    val mostImportantTime: Date?
        get() = estimatedPickupTime?.from
            ?: requestedPickupTime?.from
            ?: estimatedMealReadyTime
            ?: estimatedDeliveryTime?.from
            ?: requestedDeliveryTime?.from
}

data class DeliveryDateRange(
    val from: Date,
    val to: Date
)

class DeliverySettings(
    var acceptNewOrders: Boolean,
    var mealPrepTime: Int,
    @DispatchValue var integratedDispatch: String
) {
    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @StringDef(VALUE_ENABLED, VALUE_ASK, VALUE_DISABLED)
        annotation class DispatchValue

        const val VALUE_ENABLED = "enabled"
        const val VALUE_ASK = "ask"
        const val VALUE_DISABLED = "disabled"

    }
}
