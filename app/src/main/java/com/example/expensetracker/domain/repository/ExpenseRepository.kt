package com.example.expensetracker.domain.repository

import com.example.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>>
    fun getExpenseById(id: Long): Flow<Expense?>
    fun getTotalForMonth(yearMonth: YearMonth): Flow<Double>
}
