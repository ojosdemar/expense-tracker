package com.example.expensetracker.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.usecase.AddCategoryUseCase
import com.example.expensetracker.domain.usecase.DeleteCategoryUseCase
import com.example.expensetracker.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    val categories: StateFlow<List<Category>> = getCategoriesUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _events = MutableSharedFlow<CategoryEvent>()
    val events: SharedFlow<CategoryEvent> = _events.asSharedFlow()

    fun addCategory(name: String, color: String) {
        viewModelScope.launch {
            addCategoryUseCase(name, color)
                .onSuccess {
                    _events.emit(CategoryEvent.ShowMessage("Категория добавлена"))
                }
                .onFailure { e ->
                    _events.emit(CategoryEvent.ShowMessage("Ошибка: ${e.message}"))
                }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            deleteCategoryUseCase(category)
                .onSuccess {
                    _events.emit(CategoryEvent.ShowMessage("Категория удалена"))
                }
                .onFailure { e ->
                    _events.emit(CategoryEvent.ShowMessage("Ошибка: ${e.message}"))
                }
        }
    }

    sealed class CategoryEvent {
        data class ShowMessage(val message: String) : CategoryEvent()
    }
}
