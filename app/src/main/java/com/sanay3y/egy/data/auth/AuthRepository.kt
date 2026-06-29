package com.sanay3y.egy.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // ================= LOGIN =================

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth
                .signInWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val uid = result.user?.uid

            if (uid != null) {
                AuthResult.Success(uid)
            } else {
                AuthResult.Error("Unable to retrieve user information.")
            }

        } catch (e: FirebaseAuthInvalidCredentialsException) {

            AuthResult.Error("Incorrect email or password.")

        } catch (e: FirebaseAuthInvalidUserException) {

            AuthResult.Error("No account found with this email.")

        } catch (e: FirebaseNetworkException) {

            AuthResult.Error("No internet connection.")

        } catch (e: Exception) {

            AuthResult.Error(e.localizedMessage ?: "Login failed.")
        }
    }

    // ================= REGISTER =================

    suspend fun register(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth
                .createUserWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val uid = result.user?.uid

            if (uid != null) {
                AuthResult.Success(uid)
            } else {
                AuthResult.Error("Unable to create user.")
            }

        } catch (e: FirebaseAuthUserCollisionException) {

            AuthResult.Error("This email is already registered.")

        } catch (e: FirebaseAuthInvalidCredentialsException) {

            AuthResult.Error("Please enter a valid email and a password of at least 6 characters.")

        } catch (e: FirebaseNetworkException) {

            AuthResult.Error("No internet connection.")

        } catch (e: Exception) {

            AuthResult.Error(e.localizedMessage ?: "Registration failed.")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}