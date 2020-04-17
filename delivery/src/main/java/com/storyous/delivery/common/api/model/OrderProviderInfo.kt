package com.storyous.delivery.common.api.model

data class OrderProviderInfo(
        val orderId: String,
        val code: String,
        val customerName: String?,
        val customerPhone: String?,
        val customerAddress: String?)
