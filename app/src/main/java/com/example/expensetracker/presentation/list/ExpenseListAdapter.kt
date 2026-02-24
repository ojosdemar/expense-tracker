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
    private val onItemClick: (Expense) -> Unit,
    private val getCategoryName: (String) -> String,
    private val getCategoryColor: (String) -> String
) : ListAdapter<Expense, ExpenseListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view, onItemClick, getCategoryName, getCategoryColor)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Expense) -> Unit,
        private val getCategoryName: (String) -> String,
        private val getCategoryColor: (String) -> String
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
            tvCategory.text = getCategoryName(expense.categoryId)
            tvAmount.text = DateUtils.formatAmount(expense.amount)
            tvDate.text = DateUtils.formatShortDate(expense.date)
            
            val color = getCategoryColor(expense.categoryId)
            val drawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.parseColor(color),
                    Color.parseColor(adjustColorBrightness(color, 0.7f))
                )
            )
            drawable.cornerRadius = 8f
            colorIndicator.background = drawable
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
