package com.example.financetracker.data.repository

import com.example.financetracker.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val categoriesCollection = firestore.collection("categories")

    fun getCategories(userId: String): Flow<List<Category>> = callbackFlow {
        val listener = categoriesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val categories = snapshot.documents.mapNotNull { document ->
                    Category(
                        id = document.getString("id") ?: "",
                        name = document.getString("name") ?: "",
                        color = document.getLong("color") ?: 0xFF2196F3,
                        userId = document.getString("userId") ?: ""
                    )
                }
                trySend(categories)
            }

        awaitClose { listener.remove() }
    }

    suspend fun addCategory(category: Category) {
        categoriesCollection
            .document(category.id)
            .set(category)
            .await()
    }

    suspend fun updateCategory(category: Category) {
        categoriesCollection
            .document(category.id)
            .set(category)
            .await()
    }

    suspend fun deleteCategory(categoryId: String) {
        categoriesCollection
            .document(categoryId)
            .delete()
            .await()
    }
}
