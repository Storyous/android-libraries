package com.storyous.bills.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date

@Suppress("TooManyFunctions")
class Converters {

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
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
    fun personToJson(person: Person?) = objectToJson(person)

    @TypeConverter
    fun jsonToPerson(value: String?): Person? = jsonToObject(value)

    @TypeConverter
    fun fiscalDataToJson(fiscalData: FiscalData?) = objectToJson(fiscalData)

    @TypeConverter
    fun jsonToFiscalData(value: String?): FiscalData? = jsonToObject(value)

    @TypeConverter
    fun deskToJson(desk: Desk?) = objectToJson(desk)

    @TypeConverter
    fun jsonToDesk(value: String?): Desk? = jsonToObject(value)

    @TypeConverter
    fun invoiceDataToJson(invoiceData: InvoiceData?) = objectToJson(invoiceData)

    @TypeConverter
    fun jsonToInvoiceData(value: String?): InvoiceData? = jsonToObject(value)

    @TypeConverter
    fun orderProviderToJson(orderProvider: OrderProvider?) = objectToJson(orderProvider)

    @TypeConverter
    fun jsonToOrderProvider(value: String?): OrderProvider? = jsonToObject(value)

    @TypeConverter
    fun transactionToJson(transaction: Transaction?) = objectToJson(transaction)

    @TypeConverter
    fun jsonToTransaction(value: String?): Transaction? = jsonToObject(value)

    @TypeConverter
    fun additionalPrintDataToJson(additionalPrintData: AdditionalPrintData?) = objectToJson(additionalPrintData)

    @TypeConverter
    fun jsonToAdditionalPrintData(value: String?): AdditionalPrintData? = jsonToObject(value)

    @TypeConverter
    fun billItemsToJson(items: List<CachedBillItem>?): String? {
        return BillsDatabase.gson.toJson(items)
    }

    @TypeConverter
    fun jsonToBillItems(value: String?): List<CachedBillItem>? {
        return runCatching {
            BillsDatabase.gson.fromJson<List<CachedBillItem>>(
                value, object : TypeToken<List<CachedBillItem>>() {}.type
            )
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.recoverCatching {
            Gson().fromJson(value, object : TypeToken<List<CachedBillItem>>() {}.type)
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.getOrNull()
    }

    @TypeConverter
    fun taxSummariesToJson(value: Map<String, BigDecimal>?): String? {
        return BillsDatabase.gson.toJson(value)
    }

    @TypeConverter
    fun jsonToTaxSummaries(value: String?): Map<String, BigDecimal>? {
        return runCatching {
            BillsDatabase.gson.fromJson<Map<String, BigDecimal>>(
                value, object : TypeToken<Map<String, BigDecimal>>() {}.type
            )
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.getOrNull()
    }

    private inline fun <reified T> objectToJson(items: T?): String? {
        return BillsDatabase.gson.toJson(items)
    }

    private inline fun <reified T> jsonToObject(value: String?): T? {
        return runCatching {
            val output = BillsDatabase.gson.fromJson<T>(
                value, object : TypeToken<T>() {}.type
            )
            println("convert output $value -- $output -- ${object : TypeToken<T>() {}.type}")
            output
        }.onFailure {
            Timber.e(it, "Failed convert of $value")
        }.getOrNull()
    }
}
