package com.example.expensetracker.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.usecase.AddExpenseUseCase
import com.example.expensetracker.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    val categories = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState

    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events: SharedFlow<AddExpenseEvent> = _events.asSharedFlow()

    private var editingExpenseId: Long = 0

    fun loadExpense(expense: Expense) {
        editingExpenseId = expense.id
        _uiState.value = AddExpenseUiState(
            amount = expense.amount.toString(),
            description = expense.description,
            selectedCategoryId = expense.categoryId,
            selectedDate = expense.date,
            isEditing = true
        )
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun saveExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()
        val categoryId = state.selectedCategoryId
        
        when {
            amount == null || amount <= 0 -> {
                _uiState.value = state.copy(error = "Введите корректную сумму")
            }
            state.description.isBlank() -> {
                _uiState.value = state.copy(error = "Введите описание")
            }
            categoryId == null -> {
                _uiState.value = state.copy(error = "Выберите категорию")
            }
            else -> {
                viewModelScope.launch {
                    val expense = Expense(
                        id = editingExpenseId,
                        amount = amount,
                        description = state.description,
                        categoryId = categoryId,
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    data class AddExpenseUiState(
        val amount: String = "",
        val description: String = "",
        val selectedCategoryId: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val isLoading: Boolean = false,
        val isEditing: Boolean = false,
        val error: String? = null
    )

    sealed class AddExpenseEvent {
        object Success : AddExpenseEvent()
    }
}
