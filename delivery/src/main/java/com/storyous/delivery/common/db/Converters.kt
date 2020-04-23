package com.storyous.delivery.common.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.storyous.delivery.common.api.model.DeliveryItem
import java.math.BigDecimal
import java.util.Date

class Converters {

    companion object {
        const val ARRAY_DELIMITER = ","
    }

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String {
        return input?.toPlainString() ?: ""
    }

    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal {
        return input?.takeIf { !it.isBlank() }?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    @TypeConverter
    fun arrayToString(input: Array<String>?): String {
        return input?.joinToString(ARRAY_DELIMITER) ?: ""
    }

    @TypeConverter
    fun stringToArray(input: String?): Array<String> {
        return input?.split(ARRAY_DELIMITER)?.toTypedArray() ?: arrayOf()
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
        return Gson().toJson(items)
    }

    @TypeConverter
    fun jsonToDeliveryItems(value: String?): List<DeliveryItem>? {
        return Gson().fromJson(value, object : TypeToken<List<DeliveryItem>>() {}.type)
    }
}
