package com.storyous.commonutils

fun Int.isEven() = this % 2 == 0

fun <T, R> T.onNonNull(fallback: R, block: (T) -> R): R {
    if (this != null) {
        return block(this)
    }
    return fallback
}

suspend fun <T, R, S> onNonNull(value1: T?, value2: R?, block: suspend (T, R) -> S): S? {
    return if (value1 != null && value2 != null) {
        block(value1, value2)
    } else {
        null
    }
}
