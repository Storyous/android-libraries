package com.storyous.delivery.common.repositories

internal const val ERR_BASE = "error"
internal const val ERR_CONFLICT = "conflict"
internal const val ERR_NO_AUTH = "noAuth"

class DeliveryException(val error: String = ERR_BASE, cause: Throwable? = null) : Exception(cause) {

    var consumed = false
        private set

    fun consume() {
        consumed = true
    }
}
