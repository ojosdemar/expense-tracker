package com.example.expensetracker.presentation.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.common.DateUtils

class ExpenseListAdapter(
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val colorIndicator: View = itemView.findViewById(R.id.color_indicator)

        fun bind(expense: Expense) {
            itemView.setOnClickListener {
                onItemClick(expense)
            }

            tvDescription.text = expense.description
            tvCategory.text = expense.category.displayName
            tvAmount.text = DateUtils.formatAmount(expense.amount)
            tvDate.text = DateUtils.formatShortDate(expense.date)
            
           
