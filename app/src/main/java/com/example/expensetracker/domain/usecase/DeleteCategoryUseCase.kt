package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Unit> {
        return try {
            repository.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
