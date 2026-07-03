package com.perez.horushealth.model

data class Country(
    val name: String,
    val code: String,
    val flag: String
) {
    override fun toString(): String {
        return "$flag $name ($code)"
    }
}
