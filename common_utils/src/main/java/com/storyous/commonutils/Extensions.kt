package com.storyous.commonutils

import java.io.InputStream
import java.nio.charset.Charset

fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
    return this.bufferedReader(charset).use { it.readText() }
}

inline fun <reified T : Any> Any.castOrNull() = if (this is T) {
    this
} else {
    null
}
