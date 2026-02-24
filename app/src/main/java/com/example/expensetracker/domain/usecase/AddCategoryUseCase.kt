package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(name: String, color: String): Result<Unit> {
        return try {
            val category = Category(
                id = "custom_${System.currentTimeMillis()}",
                displayName = name,
                color = color,
                isDefault = false
            )
            repository.addCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
