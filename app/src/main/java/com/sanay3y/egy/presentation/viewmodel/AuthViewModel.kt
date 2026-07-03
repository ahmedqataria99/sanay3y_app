package com.sanay3y.egy.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sanay3y.egy.R
import com.sanay3y.egy.data.auth.AuthRepository
import com.sanay3y.egy.data.auth.AuthResult
import com.sanay3y.egy.data.model.User
import com.sanay3y.egy.data.model.UserRole
import com.sanay3y.egy.data.repository.UserRepository
import com.sanay3y.egy.utils.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkSession()
    }

    fun checkSession() {
        val firebaseUser = authRepository.getCurrentUser()
        val savedUid = preferenceManager.getUserUid()
        
        if (firebaseUser != null && savedUid == firebaseUser.uid && preferenceManager.isLoggedIn()) {
            viewModelScope.launch {
                val userResult = userRepository.getUserByUid(firebaseUser.uid)
                val user = userResult.getOrNull()
                if (user != null) {
                    preferenceManager.saveUserSession(user.firebaseUid, user.role)
                    val isSetupCompleted = if (user.role == UserRole.PROVIDER) user.category.isNotBlank() else true
                    _authState.value = AuthState.Success(user.firebaseUid, user.role != null, user.role, isSetupCompleted)
                } else {
                    // Fallback to saved role if Firestore fails but user is still authenticated
                    val role = preferenceManager.getUserRole()
                    val isSetupCompleted = role == UserRole.CLIENT // Providers must have Firestore data to be "completed"
                    _authState.value = AuthState.Success(firebaseUser.uid, role != null, role, isSetupCompleted)
                }
            }
        } else {
            preferenceManager.clearSession()
            _authState.value = AuthState.Idle
        }
    }

    // 🔐 LOGIN
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    val userResult = userRepository.getUserByUid(result.uid)
                    val user = userResult.getOrNull()
                    
                    if (user != null) {
                        preferenceManager.saveUserSession(result.uid, user.role)
                        val isSetupCompleted = if (user.role == UserRole.PROVIDER) user.category.isNotBlank() else true
                        _authState.value = AuthState.Success(result.uid, user.role != null, user.role, isSetupCompleted)
                    } else {
                        // User exists in Auth but not in Firestore, sync it
                        val syncResult = userRepository.syncUser(result.uid, "", email)

                        if (syncResult.isSuccess) {
                            preferenceManager.saveUserSession(result.uid, null)
                            _authState.value = AuthState.Success(result.uid, false)
                        } else {
                        _authState.value = AuthState.Error(
                            getApplication<Application>().getString(R.string.error_sync_user)
                        )
                        }

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
                        preferenceManager.saveUserSession(result.uid, null)
                        _authState.value = AuthState.Success(result.uid, false)
                    } else {
                        android.util.Log.e(
                            "REGISTER",
                            "Firestore createUser failed",
                            syncResult.exceptionOrNull()
                        )

                        _authState.value = AuthState.Error(
                            getApplication<Application>().getString(R.string.error_save_user)
                        )
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
                preferenceManager.saveUserSession(uid, role)
                val isSetupCompleted = role == UserRole.CLIENT
                _authState.value = AuthState.Success(uid, true, role, isSetupCompleted)
            } else {
                _authState.value = AuthState.Error(
                    getApplication<Application>().getString(R.string.error_update_role)
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        preferenceManager.clearSession()
        _authState.value = AuthState.Idle
    }
}

// ================= STATE =================

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(
        val uid: String, 
        val hasRole: Boolean, 
        val role: UserRole? = null,
        val isSetupCompleted: Boolean = false
    ) : AuthState()
    data class Error(val message: String) : AuthState()
}
