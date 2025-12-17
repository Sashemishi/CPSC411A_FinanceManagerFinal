package com.example.financetracker.viewmodel

sealed class AuthState {

    object Idle : AuthState()

    object Loading : AuthState()

    object Authenticated : AuthState()

    data class Error(
        val message: String
    ) : AuthState()

    object Unauthenticated : AuthState()
}
