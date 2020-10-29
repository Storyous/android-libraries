@file:Suppress("MatchingDeclarationName")

package com.storyous.delivery.common

import android.content.Context
import androidx.annotation.StringRes
import com.storyous.commonutils.DateUtils
import com.storyous.delivery.common.api.DeliveryDateRange
import com.storyous.delivery.common.api.DeliveryOrder
import com.storyous.delivery.common.api.DeliveryTiming
import com.storyous.delivery.common.api.DeliveryTiming.Companion.TimeDisplayType

interface StringResProvider {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String
}

@Suppress("FunctionName", "SpreadOperator")
fun ContextStringResProvider(context: Context) = object : StringResProvider {
    override fun getString(resId: Int, vararg formatArgs: Any?): String {
        return context.getString(resId, *formatArgs)
    }
}

fun DeliveryOrder.getLegacyDeliveryTime(provider: StringResProvider): String {
    val prefix = if (deliveryOnTime == true) R.string.time_at else R.string.time_till
    val date = DateUtils.HM.format(deliveryTime)
    return provider.getString(prefix, date)
}

fun DeliveryOrder.getDeliveryTypeTranslation(provider: StringResProvider): String {
    return when {
        DeliveryConfiguration.useOrderTimingField &&
            (timing?.showTime() == DeliveryTiming.SHOW_MEAL_READY || timing?.showTime() == DeliveryTiming.SHOW_ASAP) ->
            provider.getString(R.string.delivery_meal_ready)
        deliveryType == DeliveryOrder.TYPE_DELIVERY -> provider.getString(R.string.delivery_type_delivery)
        deliveryType == DeliveryOrder.TYPE_TAKEAWAY -> provider.getString(R.string.delivery_type_takeaway)
        deliveryType == DeliveryOrder.TYPE_DISPATCH -> provider.getString(R.string.delivery_type_dispatch)
        deliveryType == DeliveryOrder.TYPE_TABLE_ORDER -> provider.getString(R.string.delivery_type_order_to_table)
        else -> ""
    }
}

fun DeliveryOrder.getImportantTimingTranslation(provider: StringResProvider): Pair<String, DeliveryDateRange?>? {
    return getTimingTranslation(provider, timing?.showTime() ?: DeliveryTiming.SHOW_NOTHING)
}

@Suppress("ComplexMethod")
fun DeliveryOrder.getTimingTranslation(
    provider: StringResProvider, @TimeDisplayType type: Int
): Pair<String, DeliveryDateRange?>? {
    return when (type) {
        DeliveryTiming.SHOW_ESTIMATED_PICKUP ->
            timing?.estimatedPickupTime?.let {
                provider.getString(R.string.delivery_time_estimated_pickup_by, getDeliveryPerson(provider)) to it
            }
        DeliveryTiming.SHOW_REQUESTED_PICKUP ->
            timing?.requestedPickupTime?.let {
                provider.getString(R.string.delivery_time_requested_pickup_by, getDeliveryPerson(provider)) to it
            }
        DeliveryTiming.SHOW_MEAL_READY ->
            timing?.estimatedMealReadyTime?.let {
                provider.getString(R.string.delivery_time_meal_ready) to DeliveryDateRange(it, it)
            }
        DeliveryTiming.SHOW_ESTIMATED_DELIVERY ->
            timing?.estimatedDeliveryTime?.let {
                provider.getString(R.string.delivery_time_estimated_delivery) to it
            }
        DeliveryTiming.SHOW_REQUESTED_DELIVERY ->
            timing?.requestedDeliveryTime?.let {
                provider.getString(R.string.delivery_time_requested_delivery) to it
            }
        DeliveryTiming.SHOW_ASAP ->
            timing?.asSoonAsPossible?.takeIf { it }?.let {
                provider.getString(R.string.delivery_time_asap) to null
            }
        DeliveryTiming.SHOW_NOTHING -> null
        else -> null
    }
}

fun DeliveryOrder.getTimingTranslations(provider: StringResProvider): List<Pair<String, String>> {
    return listOfNotNull(
        getTimingTranslation(provider, DeliveryTiming.SHOW_ESTIMATED_PICKUP),
        getTimingTranslation(provider, DeliveryTiming.SHOW_REQUESTED_PICKUP),
        getTimingTranslation(provider, DeliveryTiming.SHOW_MEAL_READY),
        getTimingTranslation(provider, DeliveryTiming.SHOW_ESTIMATED_DELIVERY),
        getTimingTranslation(provider, DeliveryTiming.SHOW_REQUESTED_DELIVERY),
        getTimingTranslation(provider, DeliveryTiming.SHOW_ASAP)
    ).map { it.first to (it.second?.getTranslation(provider)?.join() ?: "") }
}

fun Pair<String, String>.join() = "$first $second".trim()

fun DeliveryDateRange.getTranslation(provider: StringResProvider): Pair<String, String> {
    val from = DateUtils.HM.format(from)
    val till = DateUtils.HM.format(to)

    return if (from == till) {
        provider.getString(R.string.time_at, from) to ""
    } else {
        provider.getString(R.string.time_from, from) to provider.getString(R.string.time_till, till)
    }
}

fun DeliveryOrder.getDeliveryPerson(provider: StringResProvider): String {
    return provider.getString(
        if (deliveryType == DeliveryOrder.TYPE_TAKEAWAY) {
            R.string.delivery_time_customer
        } else {
            R.string.delivery_time_courier
        }
    )
}

