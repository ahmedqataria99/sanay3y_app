package com.sanay3y.egy.data.model

data class Provider(
    val id: String = "",
    val firebaseUid: String = "",
    val name: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val phone: String = "",
    val imageUrl: String = "",
    val bio: String = "",
    val experienceYears: Int = 0,
    val isOnline: Boolean = false,
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
