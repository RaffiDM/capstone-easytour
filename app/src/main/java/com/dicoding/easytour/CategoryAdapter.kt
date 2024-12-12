package com.dicoding.easytour

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.easytour.data.pref.CategoryUI
import com.dicoding.easytour.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<CategoryUI>,
    private val onCategoryChecked: (CategoryUI, Boolean) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryUI) {
            binding.chipCategory.text = category.title
            binding.chipCategory.isChecked = category.isChecked

            // Listener for chip checked state
            binding.chipCategory.setOnCheckedChangeListener { _, isChecked ->
                onCategoryChecked(category, isChecked)
            }
        }
    }
}
