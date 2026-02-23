package com.example.expensetracker.presentation.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expensetracker.databinding.FragmentAddExpenseBinding
import com.example.expensetracker.domain.model.Category
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddExpenseViewModel by viewModels()
    private var expenseId: Long = 0
    
    companion object {
        private const val ARG_EXPENSE_ID = "expense_id"
        
        fun newInstance(expenseId: Long = 0): AddExpenseFragment {
            return AddExpenseFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_EXPENSE_ID, expenseId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expenseId = arguments?.getLong(ARG_EXPENSE_ID) ?: 0
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (expenseId > 0) {
            binding.btnSave.text = "Обновить"
            // Загружаем данные для редактирования
            viewModel.loadExpense(expenseId)
        }
        
        setupCategoryDropdown()
        setupDatePicker()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupCategoryDropdown() {
        val categories = Category.values().map { it.displayName }.toTypedArray()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.dropdownCategory.setAdapter(adapter)
        binding.dropdownCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.onCategorySelected(Category.values()[position])
        }
    }
    
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val today = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = LocalDate.of(year, month + 1, day)
                    viewModel.onDateSelected(date)
                    binding.etDate.setText(date.toString())
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            ).show()
        }
    }
    
    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            viewModel.onAmountChanged(binding.etAmount.text.toString())
            viewModel.onDescriptionChanged(binding.etDescription.text.toString())
            
            if (expenseId > 0) {
                viewModel.updateExpense(expenseId)
            } else {
                viewModel.saveExpense()
            }
        }
        
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun observeViewModel() {
 private fun observeViewModel() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.uiState.collect { state ->
                    // Заполняем поля при редактировании
                    if (expenseId > 0 && state.amount.isNotEmpty() && binding.etAmount.text?.isEmpty() != false) {
                        binding.etAmount.setText(state.amount)
                        binding.etDescription.setText(state.description)
                        binding.etDate.setText(state.selectedDate.toString())
                        binding.dropdownCategory.setText(state.selectedCategory.displayName, false)
                    }
                    
                    state.error?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
            
            launch {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddExpenseViewModel.AddExpenseEvent.Success -> {
                            parentFragmentManager.popBackStack()
                        }
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
