package com.storyous.delivery.common.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.storyous.delivery.common.DeliveryConfiguration
import com.storyous.delivery.common.api.DeliveryItem
import com.storyous.delivery.common.api.DeliveryTiming
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date

class Converters {

    companion object {
        const val ARRAY_DELIMITER = ","
    }

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String? {
        return input?.toPlainString()
    }

    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal? {
        return input?.takeIf { it.isNotBlank() }?.toBigDecimalOrNull()
    }

    @TypeConverter
    fun arrayToString(input: Array<String>?): String? {
        return input?.joinToString(ARRAY_DELIMITER)
    }

    @TypeConverter
    fun stringToArray(input: String?): Array<String>? {
        return input?.split(ARRAY_DELIMITER)?.toTypedArray()
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun deliveryItemsToJson(items: List<DeliveryItem>?): String? {
        return DeliveryConfiguration.gson.toJson(items)
    }

    @TypeConverter
    fun jsonToDeliveryItems(value: String?): List<DeliveryItem>? {
        return runCatching {
            DeliveryConfiguration.gson.fromJson<List<DeliveryItem>>(
                value, object : TypeToken<List<DeliveryItem>>() {}.type
            )
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.recoverCatching {
            Gson().fromJson(value, object : TypeToken<List<DeliveryItem>>() {}.type)
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.getOrNull()
    }

    @TypeConverter
    fun deliveryTimingToJson(timing: DeliveryTiming?): String? {
        return DeliveryConfiguration.gson.toJson(timing)
    }

    @TypeConverter
    fun jsonToDeliveryTiming(value: String?): DeliveryTiming? {
        return runCatching {
            DeliveryConfiguration.gson.fromJson<DeliveryTiming>(
                value, object : TypeToken<DeliveryTiming>() {}.type
            )
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.recoverCatching {
            Gson().fromJson(value, object : TypeToken<DeliveryTiming>() {}.type)
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.getOrNull()
    }
}
