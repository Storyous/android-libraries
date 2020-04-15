package com.storyous.contacts

import com.google.firebase.firestore.Query

fun Query.startsWith(field: String, value: String): Query {
    if (value.isEmpty()) {
        throw IllegalArgumentException("Value to search by startWith can not be empty.")
    }
    val until = value.substring(0, value.length - 1) + (value.toCharArray()[value.length - 1] + 1).toString()
    return whereGreaterThanOrEqualTo(field, value).whereLessThan(field, until)
}
