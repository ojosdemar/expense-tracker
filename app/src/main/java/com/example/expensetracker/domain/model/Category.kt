package com.example.expensetracker.domain.model

import java.io.Serializable

data class Category(
    val id: String,
    val displayName: String,
    val color: String,
    val isDefault: Boolean = false
) : Serializable {
    companion object {
        val FOOD = Category("food", "Еда", "#FF6B6B", true)
        val TRANSPORT = Category("transport", "Транспорт", "#4ECDC4", true)
        val ENTERTAINMENT = Category("entertainment", "Развлечения", "#45B7D1", true)
        val SHOPPING = Category("shopping", "Покупки", "#96CEB4", true)
        val HEALTH = Category("health", "Здоровье", "#FFEAA7", true)
        val UTILITIES = Category("utilities", "Коммунальные", "#DDA0DD", true)
        val OTHER = Category("other", "Другое", "#98D8C8", true)

        fun getDefaultCategories(): List<Category> = listOf(
            FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, HEALTH, UTILITIES, OTHER
        )
    }
}
