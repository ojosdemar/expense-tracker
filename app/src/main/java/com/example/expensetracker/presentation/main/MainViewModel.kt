package com.example.expensetracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.usecase.GetExpensesUseCase
import com.example.expensetracker.domain.usecase.GetCategoryStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getExpensesUseCase: GetExpensesUseCase,
    private val statisticsUseCase: GetCategoryStatisticsUseCase
) : ViewModel() {
    
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()
    
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
}
