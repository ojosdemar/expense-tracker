package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryStatisticsUseCase @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<Category, Double>> {
        return getExpensesUseCase.byMonth(yearMonth).map { expenses ->
            expenses.groupBy { it.category }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        }
    }
    
    data class Statistics(
        val categoryTotals: Map<Category, Double>,
        val totalAmount: Double,
        val categoryPercentages: Map<Category, Float>
    )
    
    fun getDetailedStatistics(yearMonth: YearMonth): Flow<Statistics> {
        return invoke(yearMonth).map { categoryTotals ->
            val total = categoryTotals.values.sum()
            val percentages = if (total > 0) {
                categoryTotals.mapValues { (_, amount) -> (amount / total * 100).toFloat() }
            } else {
                emptyMap()
            }
            Statistics(categoryTotals, total, percentages)
        }
    }
}
