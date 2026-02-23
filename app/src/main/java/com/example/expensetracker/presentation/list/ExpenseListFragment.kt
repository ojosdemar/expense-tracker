package com.example.expensetracker.presentation.list

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentExpenseListBinding
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.add.AddExpenseFragment
import com.example.expensetracker.presentation.common.DateUtils
import com.example.expensetracker.presentation.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseListAdapter(
            onItemClick = { expense ->
                openEditExpense(expense)
            },
            onItemLongClick = { expense ->
                showDeleteDialog(expense)
            }
        )
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(context)
        binding.recyclerExpenses.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnPrevious.setOnClickListener {
            viewModel.previousMonth()
        }
        binding.btnNext.setOnClickListener {
            viewModel.nextMonth()
        }
    }

    private fun openEditExpense(expense: Expense) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AddExpenseFragment.newInstance(expense))
            .addToBackStack(null)
            .commit()
    }

    private fun showDeleteDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить запись")
            .setMessage("Вы уверены, что хотите удалить \"${expense.description}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                // TODO: Добавить DeleteExpenseUseCase в MainViewModel
                Toast.makeText(context, "Удаление пока не реализовано", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
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
                    viewModel.statistics.collect { stats ->
                        stats?.let {
                            binding.tvTotal.text = DateUtils.formatAmount(it.totalAmount)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
