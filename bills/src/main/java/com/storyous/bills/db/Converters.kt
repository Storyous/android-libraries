package com.storyous.bills.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.util.*

@Suppress("TooManyFunctions")
@ProvidedTypeConverter
class Converters(private val fieldConversionErrorHandler: (Throwable, String?) -> Unit) {

    private var gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun timestampToDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun bigDecimalToString(input: BigDecimal?): String? = input?.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(input: String?): BigDecimal? =
        input?.takeIf { it.isNotBlank() }?.toBigDecimalOrNull()

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
    fun additionalPrintDataToJson(additionalPrintData: AdditionalPrintData?) =
        objectToJson(additionalPrintData)

    @TypeConverter
    fun jsonToAdditionalPrintData(value: String?): AdditionalPrintData? = jsonToObject(value)

    @TypeConverter
    fun billItemsToJson(items: List<CachedBillItem>?): String? = gson.toJson(items)

    @TypeConverter
    fun jsonToBillItems(value: String?): List<CachedBillItem>? = jsonToObject(value)

    @TypeConverter
    fun taxSummariesToJson(value: Map<String, BigDecimal>?): String? = gson.toJson(value)

    @TypeConverter
    fun jsonToTaxSummaries(value: String?): Map<String, BigDecimal>? = jsonToObject(value)

    private inline fun <reified T> objectToJson(items: T?): String? = gson.toJson(items)

    private inline fun <reified T> jsonToObject(value: String?): T? =
        runCatching {
            gson.fromJson<T>(value, object : TypeToken<T>() {}.type)
        }.onFailure {
            fieldConversionErrorHandler(it, value)
        }.getOrNull()
}
