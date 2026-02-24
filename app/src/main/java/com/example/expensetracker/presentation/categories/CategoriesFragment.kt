package com.example.expensetracker.presentation.categories

import android.app.AlertDialog
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker.databinding.FragmentCategoriesBinding
import com.example.expensetracker.domain.model.Category
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoriesViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    private val colors = listOf(
        "#FF6B6B" to "Красный",
        "#4ECDC4" to "Бирюзовый",
        "#45B7D1" to "Голубой",
        "#96CEB4" to "Зеленый",
        "#FFEAA7" to "Желтый",
        "#DDA0DD" to "Сиреневый",
        "#98D8C8" to "Мятный",
        "#F7DC6F" to "Золотой",
        "#BB8FCE" to "Фиолетовый",
        "#85C1E9" to "Небесный",
        "#F8C471" to "Оранжевый",
        "#82E0AA" to "Салатовый"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupColorDropdown()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onDeleteClick = { category ->
                if (category.isDefault) {
                    Toast.makeText(context, "Нельзя удалить стандартную категорию", Toast.LENGTH_SHORT).show()
                } else {
                    showDeleteDialog(category)
                }
            }
        )
        binding.recyclerCategories.layoutManager = LinearLayoutManager(context)
        binding.recyclerCategories.adapter = adapter
    }

    private fun setupColorDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            colors.map { it.second }
        )
        binding.dropdownColor.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnAdd.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            val colorName = binding.dropdownColor.text.toString()
            
            if (name.isBlank()) {
                Toast.makeText(context, "Введите название категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val color = colors.find { it.second == colorName }?.first ?: colors.first().first
            viewModel.addCategory(name, color)
            binding.etCategoryName.text?.clear()
        }
    }

    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить категорию")
            .setMessage("Вы уверены, что хотите удалить \"${category.displayName}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { categories ->
                        adapter.submitList(categories)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is CategoriesViewModel.CategoryEvent.ShowMessage -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
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
