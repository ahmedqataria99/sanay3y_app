package com.sanay3y.egy.data.model

data class Request(
    val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val description: String = "",
    val status: String = "",
    val estimatedPrice: Double = 0.0,
    val date: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val providerCompleted: Boolean = false,
    val clientConfirmed: Boolean = false,
    val timestamp: Long = 0L // 🔥 أضف دي
)