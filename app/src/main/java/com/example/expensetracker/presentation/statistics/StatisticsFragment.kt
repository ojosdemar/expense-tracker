package com.example.expensetracker.presentation.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expensetracker.databinding.FragmentStatisticsBinding
import com.example.expensetracker.presentation.common.DateUtils
import com.example.expensetracker.presentation.main.MainViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        setupButtons()
        observeViewModel()
    }

    private fun setupChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = true
            legend.textSize = 12f
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setUsePercentValues(true)
            setDrawEntryLabels(false)
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.statistics.collect { stats ->
                    stats?.let { data ->
                        updateChart(data.categoryTotals, data.totalAmount)
                        updateDetails(data.categoryTotals, data.totalAmount)
                    }
                }
            }
        }
    }

    private fun updateChart(categoryTotals: Map<com.example.expensetracker.domain.model.Category, Double>, total: Double) {
        val entries = categoryTotals.filter { it.value > 0 }.map { (category, amount) ->
            PieEntry(amount.toFloat(), category.displayName)
        }

        if (entries.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "Нет данных"
            return
        }

        val colors = categoryTotals.filter { it.value > 0 }.map { (category, _) ->
            Color.parseColor(category.color)
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }

        binding.pieChart.apply {
            this.data = pieData
            centerText = "Всего\n${DateUtils.formatAmount(total)}"
            setCenterTextSize(16f)
            invalidate()
        }
    }

    private fun updateDetails(categoryTotals: Map<com.example.expensetracker.domain.model.Category, Double>, total: Double) {
        val sortedCategories = categoryTotals.toList()
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        val detailsBuilder = StringBuilder()
        sortedCategories.forEach { (category, amount) ->
            val percentage = if (total > 0) (amount / total * 100) else 0.0
            detailsBuilder.append("${category.displayName}: ${DateUtils.formatAmount(amount)} (${String.format("%.1f", percentage)}%)\n")
        }

        binding.tvDetails.text = detailsBuilder.toString().trim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
