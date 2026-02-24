package com.example.expensetracker.presentation.list

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentExpenseListBinding
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.add.AddExpenseFragment
import com.example.expensetracker.presentation.categories.CategoriesFragment
import com.example.expensetracker.presentation.common.DateUtils
import com.example.expensetracker.presentation.main.MainViewModel
import com.example.expensetracker.presentation.statistics.StatisticsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: ExpenseListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("ExpenseTracker", "ExpenseListFragment onCreateView")
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ExpenseTracker", "ExpenseListFragment onViewCreated")
        setupRecyclerView()
        setupSwipeToDelete()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseListAdapter(
            onItemClick = { expense ->
                openEditExpense(expense)
            }
        )
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(context)
        binding.recyclerExpenses.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expense = adapter.currentList[position]
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Удалить запись")
                    .setMessage("Вы уверены, что хотите удалить \"${expense.description}\"?")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewModel.deleteExpense(expense)
                    }
                    .setNegativeButton("Отмена") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.parseColor("#FF4444"))
                val deleteIcon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_delete)
                
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + deleteIcon.intrinsicHeight

                when {
                    dX > 0 -> {
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + deleteIcon.intrinsicWidth
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        background.setBounds(
                            itemView.left,
                            itemView.top,
                            itemView.left + dX.toInt(),
                            itemView.bottom
                        )
                    }
                    dX < 0 -> {
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - deleteIcon.intrinsicWidth
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        background.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                    }
                    else -> {
                        background.setBounds(0, 0, 0, 0)
                    }
                }

                background.draw(c)
                deleteIcon.draw(c)
                
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerExpenses)
    }

    private fun setupButtons() {
        binding.btnPrevious.setOnClickListener {
            viewModel.previousMonth()
        }
        binding.btnNext.setOnClickListener {
            viewModel.nextMonth()
        }
        binding.btnStatistics.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, StatisticsFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.btnCategories.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, CategoriesFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openEditExpense(expense: Expense) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AddExpenseFragment.newInstance(expense))
            .addToBackStack(null)
            .commit()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.expenses.collect { expenses ->
                        Log.d("ExpenseTracker", "Got ${expenses.size} expenses")
                        adapter.submitList(expenses)
                        binding.tvEmpty.visibility =
                            if (expenses.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerExpenses.visibility =
                            if (expenses.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.categories.collect { categories ->
                        adapter.setCategories(categories)
                    }
                }
                launch {
                    viewModel.statistics.collect { stats ->
                        stats?.let {
                            binding.tvTotal.text = DateUtils.formatAmount(it.totalAmount)
                        }
                    }
                }
                launch {
                    viewModel.selectedMonth.collect { month ->
                        updateMonthText(month)
                    }
                }
            }
        }
    }

    private fun updateMonthText(yearMonth: YearMonth) {
        val monthName = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        val capitalized = monthName.replaceFirstChar { it.uppercase() }
        binding.tvMonth.text = "$capitalized ${yearMonth.year}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
