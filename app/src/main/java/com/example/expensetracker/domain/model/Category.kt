package com.example.expensetracker.domain.model

enum class Category(val displayName: String, val color: String) {
    FOOD("Еда", "#FF6B6B"),
    TRANSPORT("Транспорт", "#4ECDC4"),
    ENTERTAINMENT("Развлечения", "#45B7D1"),
    SHOPPING("Покупки", "#96CEB4"),
    HEALTH("Здоровье", "#FFEAA7"),
    UTILITIES("Коммунальные", "#DDA0DD"),
    OTHER("Другое", "#98D8C8")
}
