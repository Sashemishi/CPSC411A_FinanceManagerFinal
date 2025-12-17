package com.example.financetracker.data.model

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: Long = 0xFF2196F3,
    val userId: String = ""
)
