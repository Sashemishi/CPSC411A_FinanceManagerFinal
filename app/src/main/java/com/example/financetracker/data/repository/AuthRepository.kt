package com.example.financetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    suspend fun login(email: String, password: String): FirebaseUser {
        return auth.signInWithEmailAndPassword(email, password)
            .await()
            .user
            ?: throw Exception("Login failed")
    }
    suspend fun signUp(email: String, password: String): FirebaseUser {
        return auth.createUserWithEmailAndPassword(email, password)
            .await()
            .user
            ?: throw Exception("Sign up failed")
    }
    fun logout() {
        auth.signOut()
    }
}
