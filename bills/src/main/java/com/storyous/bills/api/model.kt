package com.storyous.bills.api

interface Bill {
    val billId: String
    val createdAt: String
    val createdBy: Person
    val currencyCode: String
    val customerId: Any
    val deskId: Any
    val discount: String
    val finalPrice: String
    val finalPriceWithoutTax: String
    val fiscalData: FiscalData
    val fiscalizedAt: String
    val invoiceData: Any
    val issuedAsVatPayer: Boolean
    val orderProvider: OrderProvider
    val paidAt: String
    val paidBy: Person
    val paymentMethod: String
    val personCount: Int
    val refunded: Boolean
    val rounding: String
    val sessionCreated: String
    val taxSummaries: Map<String, String>
    val tips: String
}

data class BillWithItems(
    override val billId: String,
    override val createdAt: String,
    override val createdBy: Person,
    override val currencyCode: String,
    override val customerId: Any,
    override val deskId: Any,
    override val discount: String,
    override val finalPrice: String,
    override val finalPriceWithoutTax: String,
    override val fiscalData: FiscalData,
    override val fiscalizedAt: String,
    override val invoiceData: Any,
    override val issuedAsVatPayer: Boolean,
    override val orderProvider: OrderProvider,
    override val paidAt: String,
    override val paidBy: Person,
    override val paymentMethod: String,
    override val personCount: Int,
    override val refunded: Boolean,
    override val rounding: String,
    override val sessionCreated: String,
    override val taxSummaries: Map<String, String>,
    override val tips: String,
    val items: List<Item>
) : Bill

data class Person(
    val fullName: String,
    val personId: Int,
    val userName: String
)

data class FiscalData(
    val bkp: String,
    val endpoint: String,
    val fik: String,
    val httpStatusCode: Int,
    val mode: Int,
    val pkp: String
)

data class Item(
    val amount: Double,
    val categoryId: String,
    val id: Int,
    val measure: String,
    val name: String,
    val price: Int,
    val productId: String,
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
