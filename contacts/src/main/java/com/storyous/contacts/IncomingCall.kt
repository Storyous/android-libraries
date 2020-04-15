package com.storyous.contacts

import java.util.Date

data class IncomingCall(
    val phoneNumber: String,
    val date: Date = Date()
) {
    constructor() : this("")
}
