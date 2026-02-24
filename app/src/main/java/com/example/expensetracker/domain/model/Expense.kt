package com.example.expensetracker.domain.model

import java.io.Serializable
import java.time.LocalDate

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val categoryId: String,
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {
    init {
        require(amount > 0) { "Amount must be positive" }
        require(description.isNotBlank()) { "Description cannot be blank" }
    }
}
