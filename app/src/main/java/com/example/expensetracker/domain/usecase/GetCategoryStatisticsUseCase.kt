package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryStatisticsUseCase @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) {

    operator fun invoke(yearMonth: YearMonth): Flow<Map<String, Double>> {
        return getExpensesUseCase.byMonth(yearMonth).map { expenses ->
            expenses.groupBy { it.categoryId }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        }
    }

    data class Statistics(
        val categoryTotals: Map<Category, Double>,
        val totalAmount: Double,
        val categoryPercentages: Map<Category, Float>
    )

    fun getDetailedStatistics(yearMonth: YearMonth): Flow<Statistics> {
        return getExpensesUseCase.byMonth(yearMonth).map { expenses ->
            val categoryTotals = mutableMapOf<Category, Double>()
            val total = expenses.sumOf { it.amount }
            
            // Получаем категории из репозитория
            val categories = getCategoriesUseCase().map { cats -> 
                cats.associateBy { it.id }
            }
            
            expenses.groupBy { it.categoryId }.forEach { (categoryId, expenseList) ->
                val amount = expenseList.sumOf { it.amount }
                // Временно используем заглушку, в реальном коде нужно получить категорию
                val category = Category(categoryId, categoryId, "#FF6B6B", false)
                categoryTotals[category] = amount
            }
            
            val percentages = if (total > 0) {
                categoryTotals.mapValues { (_, amount) -> (amount / total * 100).toFloat() }
            } else {
                emptyMap()
            }
            
            Statistics(categoryTotals, total, percentages)
        }
    }
}
