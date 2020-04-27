package com.storyous.delivery.common.repositories

class DeliveryException(cause: Throwable) : Exception(cause) {
    private var consumed = false

    fun consume(): Boolean {
        consumed = true
        return !consumed
    }
}
