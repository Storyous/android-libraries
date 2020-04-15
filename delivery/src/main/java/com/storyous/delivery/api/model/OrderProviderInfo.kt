package com.storyous.delivery.api.model

data class OrderProviderInfo(
        val orderId: String,
        val code: String,
        val customerName: String?,
        val customerPhone: String?,
        val customerAddress: String?)
