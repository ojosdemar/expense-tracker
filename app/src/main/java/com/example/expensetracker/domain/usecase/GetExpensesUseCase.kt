package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> = repository.getAllExpenses()
    
    fun byMonth(yearMonth: YearMonth): Flow<List<Expense>> = 
        repository.getExpensesByMonth(yearMonth)
}
