package com.example.expensetracker.presentation.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expensetracker.databinding.FragmentAddExpenseBinding
import com.example.expensetracker.domain.model.Category
import com.example.expensetracker.domain.model.Expense
import com.example.expensetracker.presentation.common.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddExpenseViewModel by viewModels()

    companion object {
        private const val ARG_EXPENSE = "expense"
        
        fun newInstance(expense: Expense? = null): AddExpenseFragment {
            return AddExpenseFragment().apply {
                arguments = bundleOf(ARG_EXPENSE to expense)
            }
        }
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
        
        @Suppress("DEPRECATION")
        val expense = arguments?.getSerializable(ARG_EXPENSE) as? Expense
        expense?.let {
            viewModel.loadExpense(it)
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
            val currentDate = viewModel.uiState.value.selectedDate
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = LocalDate.of(year, month + 1, day)
                    viewModel.onDateSelected(date)
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            viewModel.onAmountChanged(binding.etAmount.text.toString())
            viewModel.onDescriptionChanged(binding.etDescription.text.toString())
            viewModel.saveExpense()
        }
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        if (binding.etAmount.text.toString() != state.amount) {
                            binding.etAmount.setText(state.amount)
                            binding.etAmount.setSelection(state.amount.length)
                        }
                        
                        if (binding.etDescription.text.toString() != state.description) {
                            binding.etDescription.setText(state.description)
                            binding.etDescription.setSelection(state.description.length)
                        }
                        
                        if (binding.dropdownCategory.text.toString() != state.selectedCategory.displayName) {
                            binding.dropdownCategory.setText(state.selectedCategory.displayName, false)
                        }
                        
                        val dateText = DateUtils.formatDate(state.selectedDate)
                        if (binding.etDate.text.toString() != dateText) {
                            binding.etDate.setText(dateText)
                        }
                        
                        binding.btnSave.text = if (state.isEditing) "Обновить" else "Сохранить"
                        
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
