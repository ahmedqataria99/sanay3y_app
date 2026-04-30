package com.sanay3y.egy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.data.auth.AuthRepository
import com.sanay3y.egy.data.auth.AuthResult
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // 🔐 LOGIN
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(email, password)) {

                is AuthResult.Success -> {
                    // 🔥 sync user with Firestore
                    userRepository.syncUser(
                        firebaseUid = result.uid,
                        name = "",
                        email = email
                    )

                    _authState.value = AuthState.Success(result.uid)
                }

                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // 📝 REGISTER
    fun register(email: String, name: String, role: UserRole, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.register(email, password)) {

                is AuthResult.Success -> {

                    val user = User(
                        id = result.uid,
                        firebaseUid = result.uid,
                        name = name,
                        email = email,
                        role = role
                    )

                    val syncResult = userRepository.createUser(user)

                    if (syncResult.isSuccess) {
                        _authState.value = AuthState.Success(result.uid)
                    } else {
                        _authState.value = AuthState.Error("Failed to save user")
                    }
                }

                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Idle
    }
}

// ================= STATE =================

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}