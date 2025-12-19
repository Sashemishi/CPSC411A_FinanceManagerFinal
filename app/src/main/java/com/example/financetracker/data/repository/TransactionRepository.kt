package com.example.financetracker.data.repository

import com.example.financetracker.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionsCollection = firestore.collection("transactions")

    suspend fun addTransaction(transaction: Transaction) {
        transactionsCollection
            .document(transaction.id)
            .set(transaction)
            .await()
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionsCollection
            .document(transaction.id)
            .set(transaction)
            .await()
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionsCollection
            .document(transactionId)
            .delete()
            .await()
    }

    suspend fun updateCategoryForTransactions(transactionIds: List<String>, newCategoryId: String) {
        val batch = firestore.batch()
        transactionIds.forEach { transactionId ->
            val docRef = transactionsCollection.document(transactionId)
            batch.update(docRef, "categoryId", newCategoryId)
        }
        batch.commit().await()
    }

    suspend fun deleteTransactions(transactionIds: List<String>) {
        val batch = firestore.batch()
        transactionIds.forEach { transactionId ->
            val docRef = transactionsCollection.document(transactionId)
            batch.delete(docRef)
        }
        batch.commit().await()
    }
}
