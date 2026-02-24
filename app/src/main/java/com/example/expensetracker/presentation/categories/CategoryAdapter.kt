package com.example.expensetracker.presentation.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.domain.model.Category

class CategoryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onDeleteClick: (Category) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val colorIndicator: View = itemView.findViewById(R.id.color_indicator)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(category: Category) {
            colorIndicator.setBackgroundColor(Color.parseColor(category.color))
            tvName.text = category.displayName
            tvType.text = if (category.isDefault) "Стандартная" else "Пользовательская"
            
            btnDelete.visibility = if (category.isDefault) View.GONE else View.VISIBLE
            
            btnDelete.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
