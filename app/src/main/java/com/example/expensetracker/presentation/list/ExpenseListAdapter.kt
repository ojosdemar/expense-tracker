package com.example.expensetracker.presentation.list

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemExpenseBinding
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.common.DateUtils

class ExpenseListAdapter(
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemExpenseBinding,
        private val onItemClick: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.root.setOnClickListener {
                onItemClick(expense)
            }

            binding.tvDescription.text = expense.description
            binding.tvCategory.text = expense.category.displayName
            binding.tvAmount.text = DateUtils.formatAmount(expense.amount)
            binding.tvDate.text = DateUtils.formatShortDate(expense.date)
            
            val drawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor(expense.category.color),
                    Color.parseColor(adjustColorBrightness(expense.category.color, 0.7f))
                )
            )
            drawable.cornerRadius = 8f
            binding.colorIndicator.background = drawable
        }

        private fun adjustColorBrightness(colorHex: String, factor: Float): String {
            val color = Color.parseColor(colorHex)
            val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
            val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
            val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
            return String.format("#%02X%02X%02X", r, g, b)
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
