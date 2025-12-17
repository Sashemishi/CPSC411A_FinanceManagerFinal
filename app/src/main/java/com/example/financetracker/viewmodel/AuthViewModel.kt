package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _authState.value = if (authRepository.isUserLoggedIn()) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)

            _authState.value = result.fold(
                onSuccess = {
                    AuthState.Authenticated
                },
                onFailure = {
                    AuthState.Error(it.message ?: "Login failed")
                }
            )
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.signUp(email, password)

            _authState.value = result.fold(
                onSuccess = {
                    AuthState.Authenticated
                },
                onFailure = {
                    AuthState.Error(it.message ?: "Sign up failed")
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState.Unauthenticated
    }
}