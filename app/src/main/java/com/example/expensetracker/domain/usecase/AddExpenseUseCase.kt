package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Long> {
        return try {
            val id = if (expense.id == 0L) {
                // Новая запись
                repository.addExpense(expense)
            } else {
                // Обновление существующей
                repository.updateExpense(expense)
                expense.id
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
