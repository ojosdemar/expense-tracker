package com.example.expensetracker.data.mapper

import com.example.expensetracker.data.local.entity.ExpenseEntity
import com.example.expensetracker.domain.model.Expense
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object ExpenseMapper {
    fun toDomain(entity: ExpenseEntity): Expense {
        return Expense(
            id = entity.id,
            amount = entity.amount,
            description = entity.description,
            categoryId = entity.categoryId,
            date = Instant.ofEpochMilli(entity.dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Expense): ExpenseEntity {
        return ExpenseEntity(
            id = domain.id,
            amount = domain.amount,
            description = domain.description,
            categoryId = domain.categoryId,
            dateMillis = domain.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            createdAt = domain.createdAt
        )
    }
}
