package com.example.expensetracker.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.usecase.AddExpenseUseCase
import com.example.expensetracker.domain.usecase.GetExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getExpensesUseCase: GetExpensesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState
    
    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events: SharedFlow<AddExpenseEvent> = _events
    
    fun loadExpense(expenseId: Long) {
        viewModelScope.launch {
            getExpensesUseCase().collect { expenses ->
                val expense = expenses.find { it.id == expenseId }
                expense?.let {
                    _uiState.value = AddExpenseUiState(
                        amount = it.amount.toString(),
                        description = it.description,
                        selectedCategory = it.category,
                        selectedDate = it.date
                    )
                }
            }
        }
    }
    
    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }
    
    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
    
    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        
        when {
            amount == null || amount <= 0 -> {
                _uiState.value = state.copy(error = "Введите корректную сумму")
            }
            state.description.isBlank() -> {
                _uiState.value = state.copy(error = "Введите описание")
            }
            else -> {
                viewModelScope.launch {
                    val expense = Expense(
                        amount = amount,
                        description = state.description,
                        category = state.selectedCategory,
                        date = state.selectedDate
                    )
                    
                    addExpenseUseCase(expense)
                        .onSuccess {
                            _events.emit(AddExpenseEvent.Success)
                        }
                        .onFailure { e ->
                            _uiState.value = state.copy(error = e.message ?: "Ошибка сохранения")
                        }
                }
            }
        }
    }
    
    fun updateExpense(expenseId: Long) {
        // TODO: Добавить UpdateExpenseUseCase
        // Пока просто закрываем
        viewModelScope.launch {
            _events.emit(AddExpenseEvent.Success)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    data class AddExpenseUiState(
        val amount: String = "",
        val description: String = "",
        val selectedCategory: Category = Category.OTHER,
        val selectedDate: LocalDate = LocalDate.now(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    sealed class AddExpenseEvent {
        object Success : AddExpenseEvent()
    }
}
