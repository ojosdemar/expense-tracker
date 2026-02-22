package com.example.expensetracker.presentation.list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemExpenseBinding
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.common.DateUtils

class ExpenseListAdapter : ListAdapter<Expense, ExpenseListAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(expense: Expense) {
            binding.apply {
                tvDescription.text = expense.description
                tvCategory.text = expense.category.displayName
                tvAmount.text = DateUtils.formatAmount(expense.amount)
                tvDate.text = DateUtils.formatShortDate(expense.date)
                colorIndicator.setBackgroundColor(Color.parseColor(expense.category.color))
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
