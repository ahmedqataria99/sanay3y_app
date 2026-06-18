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
                    val userResult = userRepository.getUserByUid(result.uid)
                    val user = userResult.getOrNull()
                    
                    if (user != null) {
                        _authState.value = AuthState.Success(result.uid, user.role != null)
                    } else {
                        // User exists in Auth but not in Firestore, sync it
                        userRepository.syncUser(result.uid, "", email)
                        _authState.value = AuthState.Success(result.uid, false)
                    }
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    // 📝 REGISTER
    fun register(email: String, name: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> {
                    val user = User(
                        id = result.uid,
                        firebaseUid = result.uid,
                        name = name,
                        email = email,
                        role = null // No role at registration
                    )

                    val syncResult = userRepository.createUser(user)

                    if (syncResult.isSuccess) {
                        _authState.value = AuthState.Success(result.uid, false)
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

    // 🛠 SELECT ROLE
    fun selectRole(uid: String, role: UserRole) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = userRepository.updateRole(uid, role)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(uid, true)
            } else {
                _authState.value = AuthState.Error("Failed to update role")
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
    data class Success(val uid: String, val hasRole: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}