package com.sanay3y.egy.data.model

data class User(
    val id: String = "",
    val firebaseUid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole? = null,
    val phone: String = "",
    val governorate: String = "",
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val category: String = ""
)
