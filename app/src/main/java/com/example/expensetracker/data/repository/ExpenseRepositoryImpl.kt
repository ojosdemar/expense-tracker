package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.database.ExpenseDao
import com.example.expensetracker.data.mapper.ExpenseMapper
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insert(ExpenseMapper.toEntity(expense))
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.update(ExpenseMapper.toEntity(expense))
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(ExpenseMapper.toEntity(expense))
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAll().map { entities ->
            entities.map { ExpenseMapper.toDomain(it) }
        }
    }

    override fun getExpensesByMonth(yearMonth: YearMonth): Flow<List<Expense>> {
        val startOfMonth = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endOfMonth = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return expenseDao.getByDateRange(startOfMonth, endOfMonth).map { entities ->
            entities.map { ExpenseMapper.toDomain(it) }
        }
    }

    override fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getById(id).map { entity ->
            entity?.let { ExpenseMapper.toDomain(it) }
        }
    }

    override fun getTotalForMonth(yearMonth: YearMonth): Flow<Double> {
        val startOfMonth = yearMonth.atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endOfMonth = yearMonth.atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return expenseDao.getTotalForDateRange(startOfMonth, endOfMonth).map { it ?: 0.0 }
    }
}
