package com.example.expensetracker.data.mapper

import com.example.expensetracker.data.local.entity.CategoryEntity
import com.example.expensetracker.domain.model.Category

object CategoryMapper {
    fun toDomain(entity: CategoryEntity): Category {
        return Category(
            id = entity.id,
            displayName = entity.displayName,
            color = entity.color,
            isDefault = entity.isDefault
        )
    }

    fun toEntity(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = domain.id,
            displayName = domain.displayName,
            color = domain.color,
            isDefault = domain.isDefault
        )
    }
}
