package com.sanay3y.egy.data.model

data class Review(
    val id: String = "",
    val requestId: String = "",
    val userId: String = "",
    val providerId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = 0L
)
