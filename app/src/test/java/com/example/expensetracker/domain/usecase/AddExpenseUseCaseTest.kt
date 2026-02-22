package com.example.expensetracker.domain.usecase

import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddExpenseUseCaseTest {
    
    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: AddExpenseUseCase
    
    @Before
    fun setup() {
        repository = mockk()
        useCase = AddExpenseUseCase(repository)
    }
    
    @Test
    fun `invoke should return success with id when repository succeeds`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            description = "Test",
            category = Category.FOOD,
            date = LocalDate.now()
        )
        coEvery { repository.addExpense(expense) } returns 1L
        
        // When
        val result = useCase(expense)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { repository.addExpense(expense) }
    }
    
    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val expense = Expense(
            amount = 100.0,
            description = "Test",
            category = Category.FOOD,
            date = LocalDate.now()
        )
        val exception = RuntimeException("Database error")
        coEvery { repository.addExpense(expense) } throws exception
        
        // When
        val result = useCase(expense)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
