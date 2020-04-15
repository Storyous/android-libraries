package com.storyous.commonutils.terminal

data class TerminalTransactionResult(
    val resultCode: Int = 1,
    val message: String? = null,
    val txCode: String? = null,
    val receiptSent: Boolean? = false,
    val foreignTxId: String? = null,
    val cardBrand: String? = null,
    val cardNumber: String? = null,
    val customerReceipt: String? = null,
    val merchantReceipt: String? = null
) {
    constructor(
        resultCode: Int = 1,
        message: String? = null,
        txCode: String? = null,
        receiptSent: Boolean? = false,
        foreignTxId: String? = null
    ) : this(
        resultCode,
        message,
        txCode,
        receiptSent,
        foreignTxId,
        null,
        null,
        null,
        null
    )
}
