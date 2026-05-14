package com.example.hastakalashop.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hastakalashop.R
import com.example.hastakalashop.data.model.BillRecord
import com.example.hastakalashop.databinding.FragmentDashboardBinding
import com.example.hastakalashop.ui.viewmodel.ShopViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.allBills.observe(viewLifecycleOwner) { bills ->
            updateCharts(bills)
        }
    }

    private fun updateCharts(bills: List<BillRecord>) {
        if (bills.isEmpty()) {
            setupEmptyCharts()
            return
        }
        
        // Calculate top products from bills
        val productSales = mutableMapOf<String, Float>()
        bills.forEach { bill ->
            val name = bill.itemsSold.substringBefore(" [").substringBefore(" (x")
            val qty = bill.itemsSold.substringAfter("(x").substringBefore(")").toFloatOrNull() ?: 1f
            productSales[name] = (productSales[name] ?: 0f) + qty
        }
        
        val sortedSales = productSales.toList().sortedByDescending { it.second }
        
        setupPieChart(sortedSales.take(4))
        setupBarChart(sortedSales.take(4))
    }

    private fun setupEmptyCharts() {
        binding.pieChart.clear()
        binding.barChart.clear()
        binding.pieChart.centerText = "No Sales Yet"
    }

    private fun setupPieChart(topSales: List<Pair<String, Float>>) {
        val entries = topSales.map { PieEntry(it.second, it.first) }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.chart_color_1),
            ContextCompat.getColor(requireContext(), R.color.chart_color_2),
            ContextCompat.getColor(requireContext(), R.color.chart_color_3),
            ContextCompat.getColor(requireContext(), R.color.chart_color_other)
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setHoleColor(Color.TRANSPARENT)
        binding.pieChart.setTransparentCircleAlpha(0)
        binding.pieChart.centerText = "Sales\nBy Product"
        binding.pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        binding.pieChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        binding.pieChart.animateY(1000)
    }

    private fun setupBarChart(topSales: List<Pair<String, Float>>) {
        val entries = topSales.mapIndexed { index, pair -> BarEntry(index.toFloat() + 1, pair.second) }

        val dataSet = BarDataSet(entries, "Units Sold")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.chart_color_1),
            ContextCompat.getColor(requireContext(), R.color.chart_color_2),
            ContextCompat.getColor(requireContext(), R.color.chart_color_3),
            ContextCompat.getColor(requireContext(), R.color.chart_color_other)
        )
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        dataSet.valueTextSize = 12f

        binding.barChart.data = BarData(dataSet)
        binding.barChart.description.isEnabled = false
        binding.barChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        binding.barChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        binding.barChart.animateY(1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
