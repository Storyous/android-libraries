@file:Suppress("MatchingDeclarationName")

package com.storyous.bills.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity(
    indices = [Index(value = ["createdAt"])]
)
@Suppress("LongParameterList")
data class CachedBill(
    @PrimaryKey val billId: String,
    val paymentBillId: Int?,
    val billImage: String?,
    val billImageLores: String?,
    val createdAt: Date,
    val createdBy: Person,
    val currencyCode: String,
    val customerId: String,
    val customTitle: String?,
    val customText: String?,
    val desk: Desk?,
    val discount: BigDecimal,
    val finalPrice: BigDecimal,
    val finalPriceWithoutTax: BigDecimal,
    val fiscalData: FiscalData?,
    val fiscalized: Boolean,
    val fiscalizedAt: Date?,
    val invoiceData: InvoiceData?,
    val issuedAsVatPayer: Boolean,
    val merchant: Merchant?,
    val orderProvider: OrderProvider?,
    val paidAt: Date?,
    val paidBy: Person?,
    val paymentMethod: String,
    val personCount: Int,
    val refunded: Boolean,
    val refundedBillIdentifier: String?,
    val rounding: BigDecimal,
    val sessionCreated: Date,
    val taxSummaries: Map<String, BigDecimal>?,
    val tips: BigDecimal,
    val transactionId: String?,
    val transaction: Transaction?,
    val additionalPrintData: AdditionalPrintData?,
    val items: List<CachedBillItem> = listOf()
)

data class Merchant(
    val merchantId: String?,
    val company: String?,
    val street: String?,
    val town: String?,
    val zip: String?,
    val isVatPayer: Boolean,
    val businessId: String?,
    val vatId: String?,
    val countryCode: String?
)

data class Person(
    val personId: Int,
    val fullName: String,
    val userName: String
)

data class Desk(
    val deskId: String,
    val code: String,
    val name: String?,
    val color: String?,
    val sectionColor: String?
)

data class CachedBillItem(
    val id: String,
    val name: String,       // title
    val amount: Double,     // count
    val price: BigDecimal,  // == price
    val categoryId: String,
    val measure: String?,    // == measure
    val productId: String,  //
    val vatId: Int,         // == vatId
    val vatRate: Double,    // vat

    val note: String?,
    val separator: Boolean,
    val menuSet: MenuSet?,
    val attributes: List<String>,
    val menuType: String?
)

data class MenuSet(
    val id: String,
    val name: String,
    val instanceId: String,
    val sectionId: String
)

data class InvoiceData(
    val invoiceInfo: InvoiceInfo?,
    val customer: Customer?
)

data class Customer(
    val name: String?,
    val address: String?,
    val postCode: String?,
    val city: String?,
    val businessId: String?,
    val vatNumber: String,
    val invoiceNumber: String?,
    val additionalVatNumber: String?
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

data class OrderProvider(
    val code: String,
    val orderId: String,
    val customerName: String?,
    val customerPhone: String?,
    val customerAddress: String?
)

data class Transaction(
    val preauthData: String?,
    val appId: Int,
    val createDate: Date?,
    val transactionId: String?,
    val switchTransactionId: String?,
    val originalAmount: Int,
    val originalCurrency: String?,
    val poiTransactionId: String?,
    val authCode: String?
)

data class AdditionalPrintData(
    val customer: Customer? = null,
    val invoiceInfo: InvoiceInfo? = null,
    val customerLocationQR: String? = null,
    val provider: String? = null,
    val lines: List<NoteLine>? = null,
    val bonIdentifier: String? = null,
    val ekasaData: EkasaData? = null,
    val ekasaOfflineOverview: List<String>? = null
)

data class InvoiceInfo(
    var invoiceNumber: String?,
    var issueDate: String?,
    var vatDate: String?,
    var paymentDate: String?
)

data class NoteLine(
    val text: String,
    val meta: Map<String, String>
)

data class EkasaData(
    val fiscalData: EkasaFiscalData? = null,
    val voucher: Voucher? = null,
    val identification: IdentificationData
)

data class EkasaFiscalData(
    val paragon: Paragon? = null,
    val email: String? = null,
    val paidInvoice: PaidInvoice? = null,
    val voidReceipt: Boolean = false
)

data class Paragon(
    val number: Int,
    val date: Date
)

data class PaidInvoice(
    val amount: BigDecimal,
    val number: String
)

data class Voucher(
    val amount: BigDecimal,
    val number: String
)

data class IdentificationData(
    val dic: String,
    val ico: String?,
    val icDph: String?,
    val corporateBodyFullName: String,
    val organizationUnit: OrganizationUnit,
    val physicalAddress: PhysicalAddress
)

data class OrganizationUnit(
    val organizationUnitName: String?,
    val cashRegisterCode: String,
    val cashRegisterType: String,
    val physicalAddress: PhysicalAddress?
)

data class PhysicalAddress(
    val country: String,
    val municipality: String,
    val streetName: String,
    val buildingNumber: String?,
    val propertyRegistrationNumber: String,
    val deliveryAddress: DeliveryAddress?
)

data class DeliveryAddress(
    val postalCode: String
)
