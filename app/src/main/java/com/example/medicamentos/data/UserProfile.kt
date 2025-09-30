package com.example.medicamentos.data

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "" // "Date of Birth" - Data de Nascimento
)