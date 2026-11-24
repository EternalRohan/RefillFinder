package com.refillfinder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.refillfinder.app.model.User
import com.refillfinder.app.repository.AuthRepository
import com.refillfinder.app.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isNewUser: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val currentUser get() = repository.currentUser

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            repository.authStateFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    loadUserData()
                } else {
                    _authState.value = AuthUiState(isLoggedIn = false)
                }
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            when (val result = repository.getCurrentUserData()) {
                is Result.Success -> _authState.value = AuthUiState(
                    isLoggedIn = true,
                    user = result.data
                )
                is Result.Error -> _authState.value = AuthUiState(
                    isLoggedIn = true,
                    error = result.message
                )
                else -> Unit
            }
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = repository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isNewUser = result.data
                    )
                    onResult(result.data)
                }
                is Result.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = repository.signInWithEmail(email, password)) {
                is Result.Success -> {
                    loadUserData()
                    onResult(true, null)
                }
                is Result.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    onResult(false, result.message)
                }
                else -> Unit
            }
        }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = repository.registerWithEmail(email, password, name)) {
                is Result.Success -> {
                    loadUserData()
                    onResult(true, null)
                }
                is Result.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    onResult(false, result.message)
                }
                else -> Unit
            }
        }
    }

    fun updateUserLocation(block: Int, floor: Int) {
        viewModelScope.launch {
            repository.updateUserLocation(block, floor)
            _authState.value = _authState.value.copy(
                user = _authState.value.user?.copy(defaultBlock = block, defaultFloor = floor)
            )
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthUiState(isLoggedIn = false)
    }

    // ── Convenience wrappers called by LoginScreen ────────────────────
    fun login(email: String, password: String) {
        signInWithEmail(email, password) { _, _ -> }
    }

    fun register(name: String, email: String, password: String) {
        registerWithEmail(email, password, name) { _, _ -> }
    }
}
