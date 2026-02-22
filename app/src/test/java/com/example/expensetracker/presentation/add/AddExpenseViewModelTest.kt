package com.example.expensetracker.presentation.add

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.usecase.AddExpenseUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddExpenseViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var viewModel: AddExpenseViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addExpenseUseCase = mockk()
        viewModel = AddExpenseViewModel(addExpenseUseCase)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `saveExpense should emit success when use case succeeds`() = runTest {
        // Given
        viewModel.onAmountChanged("100.50")
        viewModel.onDescriptionChanged("Test expense")
        viewModel.onCategorySelected(Category.FOOD)
        
        coEvery { addExpenseUseCase(any()) } returns Result.success(1L)
        
        // When & Then
        viewModel.events.test {
            viewModel.saveExpense()
            testDispatcher.scheduler.advanceUntilIdle()
            
            assertEquals(AddExpenseViewModel.AddExpenseEvent.Success, awaitItem())
            coVerify { 
                addExpenseUseCase(match { 
                    it.amount == 100.50 && 
                    it.description == "Test expense" &&
                    it.category == Category.FOOD 
                })
            }
        }
    }
    
    @Test
    fun `saveExpense should show error for invalid amount`() = runTest {
        // Given
        viewModel.onAmountChanged("invalid")
        viewModel.onDescriptionChanged("Test")
        
        // When
        viewModel.saveExpense()
        
        // Then
        assertEquals("Введите корректную сумму", viewModel.uiState.value.error)
    }
    
    @Test
    fun `saveExpense should show error for blank description`() = runTest {
        // Given
        viewModel.onAmountChanged("100")
        viewModel.onDescriptionChanged("   ")
        
        // When
        viewModel.saveExpense()
        
        // Then
        assertEquals("Введите описание", viewModel.uiState.value.error)
    }
}
