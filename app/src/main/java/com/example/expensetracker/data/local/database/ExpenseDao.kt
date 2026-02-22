package com.example.expensetracker.data.local.database

import androidx.room.*
import com.example.expensetracker.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long
    
    @Update
    suspend fun update(expense: ExpenseEntity)
    
    @Delete
    suspend fun delete(expense: ExpenseEntity)
    
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, createdAt DESC")
    fun getAll(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startDate AND :endDate ORDER BY dateMillis DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getById(id: Long): Flow<ExpenseEntity?>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis BETWEEN :startDate AND :endDate")
    fun getTotalForDateRange(startDate: Long, endDate: Long): Flow<Double?>
}
