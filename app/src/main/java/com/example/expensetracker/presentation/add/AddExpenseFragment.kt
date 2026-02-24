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
    private var isFirstLoad = true

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    if (categories.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categories.map { it.displayName }
                        )
                        binding.dropdownCategory.setAdapter(adapter)
                        binding.dropdownCategory.setOnItemClickListener { _, _, position, _ ->
                            viewModel.onCategorySelected(categories[position])
                        }
                        
                        if (isFirstLoad && viewModel.uiState.value.selectedCategory == null) {
                            viewModel.onCategorySelected(categories.first())
                        }
                    }
                }
            }
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
                    binding.etDate.setText(DateUtils.formatDate(date))
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
                        if (isFirstLoad) {
                            binding.etAmount.setText(state.amount)
                            binding.etDescription.setText(state.description)
                            state.selectedCategory?.let { category ->
                                binding.dropdownCategory.setText(category.displayName, false)
                            }
                            binding.etDate.setText(DateUtils.formatDate(state.selectedDate))
                            binding.btnSave.text = if (state.isEditing) "Обновить" else "Сохранить"
                            isFirstLoad = false
                        } else {
                            binding.etDate.setText(DateUtils.formatDate(state.selectedDate))
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
