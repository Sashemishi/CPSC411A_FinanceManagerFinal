package com.example.financetracker.data.repository

import com.example.financetracker.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionsCollection = firestore.collection("transactions")

    fun getTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot
                    ?.toObjects(Transaction::class.java)
                    ?: emptyList()

                trySend(transactions)
            }

        awaitClose { listener.remove() }
    }

    fun getTransactionsByCategory(
        userId: String,
        categoryId: String
    ): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot
                    ?.toObjects(Transaction::class.java)
                    ?: emptyList()

                trySend(transactions)
            }

        awaitClose { listener.remove() }
    }

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
