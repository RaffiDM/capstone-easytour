package com.dicoding.easytour

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.easytour.databinding.ItemGridBinding
import com.dicoding.easytour.entity.HomeEntity

class GridAdapter(private val onItemClicked: (HomeEntity) -> Unit) : RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    private val items: MutableList<HomeEntity> = mutableListOf()

    inner class GridViewHolder(private val binding: ItemGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeEntity) {
            Glide.with(binding.ivImage.context)
                .load(item.imageUrl)
                .into(binding.ivImage)
            binding.tvName.text = item.name
            binding.tvRating.text = "${item.rating} of 5"
            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<HomeEntity>) {
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }
}