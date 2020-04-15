package com.storyous.contacts

data class Contact(
    val phoneNumber: String,
    val name: String,
    val streetAddress: String,
    val city: String
) {
    constructor() : this("", "", "", "")
}
