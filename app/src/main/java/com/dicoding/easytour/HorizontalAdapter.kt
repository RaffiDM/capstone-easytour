package com.dicoding.easytour.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.easytour.databinding.ItemHorizontalBinding
import com.dicoding.easytour.entity.HomeEntity

class HorizontalAdapter(
    private val onItemClick: (HomeEntity) -> Unit
) : RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    private val items = mutableListOf<HomeEntity>()

    fun submitList(data: List<HomeEntity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size

    class HorizontalViewHolder(private val binding: ItemHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeEntity, onItemClick: (HomeEntity) -> Unit) {
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(binding.mediaCover)
            Log.d("adapter", "item.${item.imageUrl}")
            binding.tvName.text = item.name
            binding.tvRating.text = "${item.rating} of 5"
            //binding.tvDescription.text = item.description

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}

