package com.example.expensetracker.presentation.common

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru"))
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)
    fun formatMonth(yearMonth: YearMonth): String = yearMonth.format(monthFormatter)
    fun formatShortDate(date: LocalDate): String = date.format(shortDateFormatter)
    fun formatAmount(amount: Double): String = String.format("%,.2f ₽", amount)
}
