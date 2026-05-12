package com.example.hastakalashop.ui

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.hastakalashop.R
import com.example.hastakalashop.data.model.Product
import com.example.hastakalashop.databinding.DialogAddProductBinding
import com.example.hastakalashop.databinding.FragmentStockAlertBinding
import com.example.hastakalashop.databinding.ItemStockAlertBinding
import com.example.hastakalashop.ui.viewmodel.ShopViewModel
import com.google.android.material.snackbar.Snackbar

class StockAlertFragment : Fragment() {

    private var _binding: FragmentStockAlertBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = StockAdapter { product ->
            showRestockDialog(product)
        }
        binding.recyclerStockAlerts.adapter = adapter
        
        viewModel.lowStockProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun showRestockDialog(product: Product) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter quantity to add"

        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle("Restock ${product.name}")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val qtyToAdd = input.text.toString().toIntOrNull() ?: 0
                if (qtyToAdd > 0) {
                    val updatedProduct = product.copy(stockQuantity = product.stockQuantity + qtyToAdd)
                    viewModel.updateProduct(updatedProduct)
                    Snackbar.make(binding.root, "Stock updated", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val name = dialogBinding.etProductName.text.toString()
            val priceStr = dialogBinding.etProductPrice.text.toString()
            val stockStr = dialogBinding.etProductStock.text.toString()
            val category = dialogBinding.etProductCategory.text.toString()
            val variants = dialogBinding.etProductVariants.text.toString()

            if (name.isNotEmpty() && priceStr.isNotEmpty() && stockStr.isNotEmpty()) {
                val product = Product(
                    name = name,
                    price = priceStr.toDouble(),
                    stockQuantity = stockStr.toInt(),
                    category = category,
                    variant = variants,
                    imageResId = R.drawable.img_bag
                )
                viewModel.insertProduct(product)
                dialog.dismiss()
                Snackbar.make(binding.root, getString(R.string.product_added_successfully), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(dialogBinding.root, getString(R.string.please_fill_required_fields), Snackbar.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StockAdapter(private val onClick: (Product) -> Unit) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {
    
    private var items: List<Product> = emptyList()
    
    fun submitList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ViewHolder(val binding: ItemStockAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemStockAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvItemName.text = item.name
        holder.binding.tvVariant.text = item.variant
        holder.binding.tvStockCount.text = "${item.stockQuantity} Left"
        
        if (item.stockQuantity <= 2) {
            holder.binding.badgeCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.stock_critical)
            )
        } else {
            holder.binding.badgeCard.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.stock_low)
            )
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
