package com.example.expensetracker.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.FragmentExpenseListBinding
import com.example.expensetracker.presentation.common.DateUtils
import com.example.expensetracker.presentation.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
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
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = ExpenseListAdapter()
        binding.recyclerExpenses.adapter = adapter
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO: Implement delete with confirmation
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerExpenses)
    }
    
    private fun setupButtons() {
        binding.btnPrevious.setOnClickListener {
            viewModel.previousMonth()
        }
        
        binding.btnNext.setOnClickListener {
            viewModel.nextMonth()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.expenses.collect { expenses ->
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
