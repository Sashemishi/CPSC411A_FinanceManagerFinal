package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authStatus = MutableStateFlow(AuthStatus.UNAUTHENTICATED)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authStatus.value =
            if (repository.getCurrentUser() != null) {
                AuthStatus.AUTHENTICATED
            } else {
                AuthStatus.UNAUTHENTICATED
            }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password)
            _authStatus.value = AuthStatus.AUTHENTICATED
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            repository.signUp(email, password)
            _authStatus.value = AuthStatus.AUTHENTICATED
        }
    }

    fun logout() {
        repository.logout()
        _authStatus.value = AuthStatus.UNAUTHENTICATED
    }
}