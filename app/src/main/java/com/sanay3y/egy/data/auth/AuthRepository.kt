package com.sanay3y.egy.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    // 🔐 LOGIN
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid

            if (uid != null) {
                AuthResult.Success(uid)
            } else {
                AuthResult.Error("User ID is null")
            }

        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    // 📝 REGISTER
    suspend fun register(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid

            if (uid != null) {
                AuthResult.Success(uid)
            } else {
                AuthResult.Error("User ID is null")
            }

        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Register failed")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}