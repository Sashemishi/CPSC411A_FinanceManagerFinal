package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authStatus = MutableStateFlow(AuthStatus.UNAUTHENTICATED)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

    fun clearAllUserData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                // Delete all transactions for the user
                val transactionsQuery = db.collection("transactions").whereEqualTo("userId", userId)
                transactionsQuery.get().await().documents.forEach { doc ->
                    doc.reference.delete()
                }
                // Delete all categories for the user
                val categoriesQuery = db.collection("categories").whereEqualTo("userId", userId)
                categoriesQuery.get().await().documents.forEach { doc ->
                    doc.reference.delete()
                }
            } catch (e: Exception) {
                // Handle or log the exception if needed
            }
        }
    }
}
