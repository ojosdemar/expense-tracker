package com.example.expensetracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.usecase.DeleteExpenseUseCase
import com.example.expensetracker.domain.usecase.GetCategoryStatisticsUseCase
import com.example.expensetracker.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getExpensesUseCase: GetExpensesUseCase,
    private val statisticsUseCase: GetCategoryStatisticsUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events

    val expenses = _selectedMonth.flatMapLatest { month ->
        getExpensesUseCase.byMonth(month)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val statistics = _selectedMonth.flatMapLatest { month ->
        statisticsUseCase.getDetailedStatistics(month)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            deleteExpenseUseCase(expense)
                .onSuccess {
                    _events.emit(MainEvent.ShowMessage("Запись удалена"))
                }
                .onFailure { e ->
                    _events.emit(MainEvent.ShowMessage("Ошибка: ${e.message}"))
                }
        }
    }

    sealed class MainEvent {
        data class ShowMessage(val message: String) : MainEvent()
    }
}
