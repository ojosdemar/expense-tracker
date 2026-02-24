package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.database.CategoryDao
import com.example.expensetracker.data.mapper.CategoryMapper
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { entities ->
            entities.map(CategoryMapper::toDomain)
        }
    }

    override suspend fun addCategory(category: Category) {
        categoryDao.insert(CategoryMapper.toEntity(category))
    }

    override suspend fun deleteCategory(category: Category) {
        if (!category.isDefault) {
            categoryDao.delete(CategoryMapper.toEntity(category))
        }
    }
}
