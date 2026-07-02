package com.sanay3y.egy.data.model

data class Provider(
    val id: String = "",
    val firebaseUid: String = "",
    val name: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val phone: String = "",
    val imageUrl: String = "", // Legacy field or general profile image
    val bio: String = "",
    val experienceYears: Int = 0,
    val isOnline: Boolean = false,
    val location: String = "",
    val governorate: String = "",
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourlyPrice: Double = 0.0,
    // Storage Download URLs
    val profilePhotoUrl: String = "",
    val nationalIdFrontUrl: String = "",
    val nationalIdBackUrl: String = "",
    val policeClearanceUrl: String = ""
)
