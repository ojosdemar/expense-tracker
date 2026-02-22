package com.example.expensetracker.presentation.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.expensetracker.databinding.FragmentAddExpenseBinding
import com.example.expensetracker.domain.model.Category
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class AddExpenseFragment : Fragment() {
    
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddExpenseViewModel by viewModels()
    
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
            viewModel.saveExpense()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.error?.let { error ->
                            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
                
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddExpenseViewModel.AddExpenseEvent.Success -> {
                                findNavController().navigateUp()
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
