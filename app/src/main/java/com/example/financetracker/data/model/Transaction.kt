package com.example.financetracker.data.model

import java.util.UUID

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: String = "",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val userId: String = ""
)