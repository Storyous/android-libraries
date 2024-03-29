package com.storyous.bills.api

import com.storyous.bills.db.AdditionalPrintData
import com.storyous.bills.db.Transaction
import java.math.BigDecimal
import java.util.Date

interface Bill {
    val billId: String
    val createdAt: Date
    val createdBy: Person
    val currencyCode: String
    val customerId: String
    val deskId: String
    val discount: BigDecimal
    val finalPrice: BigDecimal
    val finalPriceWithoutTax: BigDecimal
    val fiscalData: FiscalData
    val fiscalized: Boolean
    val fiscalizedAt: Date
    val invoiceData: Any?
    val issuedAsVatPayer: Boolean
    val orderProvider: OrderProvider?
    val paidAt: Date
    val paidBy: Person
    val paymentMethod: String
    val personCount: Int
    val refunded: Boolean
    val refundedBillIdentifier: String?
    val rounding: BigDecimal
    val sessionCreated: Date
    val taxSummaries: Map<String, BigDecimal>
    val tips: BigDecimal
    val transactionId: String?
    val transaction: Transaction?
    val additionalPrintData: AdditionalPrintData?
}

data class BillWithItems(
    override val billId: String,
    override val createdAt: Date,
    override val createdBy: Person,
    override val currencyCode: String,
    override val customerId: String,
    override val deskId: String,
    override val discount: BigDecimal,
    override val finalPrice: BigDecimal,
    override val finalPriceWithoutTax: BigDecimal,
    override val fiscalData: FiscalData,
    override val fiscalized: Boolean,
    override val fiscalizedAt: Date,
    override val invoiceData: Any?,
    override val issuedAsVatPayer: Boolean,
    override val orderProvider: OrderProvider?,
    override val paidAt: Date,
    override val paidBy: Person,
    override val paymentMethod: String,
    override val personCount: Int,
    override val refunded: Boolean,
    override val refundedBillIdentifier: String?,
    override val rounding: BigDecimal,
    override val sessionCreated: Date,
    override val taxSummaries: Map<String, BigDecimal>,
    override val tips: BigDecimal,
    override val transactionId: String?,
    override val transaction: Transaction?,
    override val additionalPrintData: AdditionalPrintData?,
    val items: List<BillItem>
) : Bill

data class Person(
    val fullName: String,
    val personId: Int,
    val userName: String
)

data class FiscalData(
    val bkp: String?,
    val endpoint: String?,
    val fik: String?,
    val httpStatusCode: Int?,
    val mode: Int?,
    val pkp: String?,
    val customType: String?,
    val crc: String?,
    val okp: String?,
    val billId: String?,
    val qr: String?,
    val uuid: String?
)

data class BillItem(
    val amount: Double,
    val categoryId: String,
    val id: String,
    val measure: String,
    val name: String,
    val price: BigDecimal,
    val productId: String,
    val vatId: Int,
    val vatRate: Double
)

data class OrderProvider(
    val code: String,
    val orderId: String
)

data class BillsResponse(
    val data: List<Bill>,
    val nextPage: String
)

data class RefundBillRequest(
    val reasonId: Int?,
    val fiscalData: Map<String, String?>?,
    val isReopen: Boolean,
    val refundedBillIdentifier: String?
)

data class UpdateBillRequest(
    val fiscalData: Map<String, String?>?
)
