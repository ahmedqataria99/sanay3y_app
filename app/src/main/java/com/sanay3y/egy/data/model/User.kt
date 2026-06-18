package com.sanay3y.egy.data.model

data class User(
    val id: String = "",
    val firebaseUid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole? = null,
    val phone: String = "",  // ← جديد
)
