package com.example.financetracker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val username: String = "User",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val username = document.getString("name") ?: "User"
            _uiState.value = _uiState.value.copy(username = username)
        }

        val transactionsRef = db.collection("transactions").whereEqualTo("userId", userId)
        transactionsRef.addSnapshotListener { snapshot, _ ->
            val transactions = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
            processDashboardData(transactions)
        }
    }

    private fun processDashboardData(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpense
        val recentTransactions = transactions.sortedByDescending { it.date }.take(5)

        _uiState.value = _uiState.value.copy(
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            recentTransactions = recentTransactions,
            isLoading = false
        )
    }
}
