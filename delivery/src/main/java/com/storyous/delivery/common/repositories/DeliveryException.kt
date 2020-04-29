package com.storyous.delivery.common.repositories

class DeliveryException(cause: Throwable) : Exception(cause) {

    var consumed = false
        private set

    fun consume() {
        consumed = true
    }
}
