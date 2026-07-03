package com.perez.horushealth.model

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val birthDate: String,
    val password: String,
    val countryCode: String = "+593"
)

data class Doctor(
    val name: String,
    val specialty: String,
    val schedule: String,
    val rating: Double
)

data class Appointment(
    val id: String,
    val specialty: String,
    val doctorName: String,
    val dateLabel: String,
    val time: String,
    val status: String
)
