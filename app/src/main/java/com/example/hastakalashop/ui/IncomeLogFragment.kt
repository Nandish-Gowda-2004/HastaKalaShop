package com.example.hastakalashop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.hastakalashop.R
import com.example.hastakalashop.data.model.BillRecord
import com.example.hastakalashop.databinding.FragmentIncomeLogBinding
import com.example.hastakalashop.databinding.ItemIncomeLogBinding
import com.example.hastakalashop.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IncomeLogFragment : Fragment() {

    private var _binding: FragmentIncomeLogBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()
    private var allBills: List<BillRecord> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = IncomeAdapter()
        binding.recyclerIncomeLog.adapter = adapter
        
        viewModel.allBills.observe(viewLifecycleOwner) { bills ->
            allBills = bills
            applyFilter(binding.chipGroupFilter.checkedChipId, adapter)
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                applyFilter(checkedIds[0], adapter)
            }
        }
    }

    private fun applyFilter(chipId: Int, adapter: IncomeAdapter) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        val filteredList = when (chipId) {
            R.id.chip_week -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startOfWeek = calendar.timeInMillis
                allBills.filter { it.date >= startOfWeek }
            }
            R.id.chip_month -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startOfMonth = calendar.timeInMillis
                allBills.filter { it.date >= startOfMonth }
            }
            else -> allBills // All Time
        }

        adapter.submitList(filteredList)
        val total = filteredList.sumOf { it.totalAmount }
        binding.tvTotalIncome.text = "₹ $total"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class IncomeAdapter : RecyclerView.Adapter<IncomeAdapter.ViewHolder>() {
    
    private var items: List<BillRecord> = emptyList()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
    fun submitList(newItems: List<BillRecord>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ViewHolder(val binding: ItemIncomeLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemIncomeLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvLogDate.text = dateFormat.format(Date(item.date))
        holder.binding.tvLogItem.text = item.itemsSold
        holder.binding.tvLogTotal.text = "₹ ${item.totalAmount}"
    }

    override fun getItemCount() = items.size
}
