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
import com.github.mikephil.charting.components.Legend
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
        val textColor = getTextColor()
        
        binding.pieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = textColor
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 10f
                yEntrySpace = 5f
            }
        }
    }

    private fun getTextColor(): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        return typedValue.data
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

        val colorsList = categoryTotals.filter { it.value > 0 }.map { (category, _) ->
            Color.parseColor(category.color)
        }

        val textColor = getTextColor()

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colorsList)
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 10f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }

        binding.pieChart.apply {
            data = pieData
            centerText = "Всего\n${DateUtils.formatAmount(total)}"
            setCenterTextSize(16f)
            setCenterTextColor(textColor)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateDetails(categoryTotals: Map<com.example.expensetracker.domain.model.Category, Double>, total: Double) {
        val sortedCategories = categoryTotals.toList()
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        binding.categoriesContainer.removeAllViews()
        
        sortedCategories.forEach { (category, amount) ->
            val percentage = if (total > 0) (amount / total * 100) else 0.0
            
            val itemView = layoutInflater.inflate(R.layout.item_category_stat, binding.categoriesContainer, false)
            
            val colorIndicator = itemView.findViewById<View>(R.id.color_indicator)
            val tvCategory = itemView.findViewById<android.widget.TextView>(R.id.tv_category)
            val tvAmount = itemView.findViewById<android.widget.TextView>(R.id.tv_amount)
            val tvPercent = itemView.findViewById<android.widget.TextView>(R.id.tv_percent)
            val progressBar = itemView.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progress_bar)
            
            colorIndicator.setBackgroundColor(Color.parseColor(category.color))
            tvCategory.text = category.displayName
            tvAmount.text = DateUtils.formatAmount(amount)
            tvPercent.text = String.format("%.1f%%", percentage)
            progressBar.progress = percentage.toInt()
            progressBar.setIndicatorColor(Color.parseColor(category.color))
            
            binding.categoriesContainer.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
