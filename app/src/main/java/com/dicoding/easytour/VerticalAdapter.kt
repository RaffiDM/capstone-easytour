package com.dicoding.easytour.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.easytour.GlideApp
import com.dicoding.easytour.R
import com.dicoding.easytour.databinding.ItemVerticalBinding
import com.dicoding.easytour.entity.HomeEntity

class VerticalAdapter(
    private val onItemClick: (HomeEntity) -> Unit
) : RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder>() {

    private val items = mutableListOf<HomeEntity>()

    fun submitList(data: List<HomeEntity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalViewHolder {
        val binding = ItemVerticalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VerticalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerticalViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size

    class VerticalViewHolder(private val binding: ItemVerticalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeEntity, onItemClick: (HomeEntity) -> Unit) {
            // Log the image URL to check if it's null or incorrect
            Log.d("VerticalAdapter", "Image URL: ${item.imageUrl}")

            // Load the image with Glide, using a default image if the URL is null
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(binding.mediaCover)

            // Set the text values
            binding.tvName.text = item.name
            binding.tvDescription.text = item.description
            binding.tvCity.text = item.city
            binding.tvRating.text = "${item.rating} of 5"
            binding.tvPrice.text = item.price

            // Log the other values to ensure they are not null
            Log.d("VerticalAdapter", "Name: ${item.name}")
            Log.d("VerticalAdapter", "Description: ${item.description}")
            Log.d("VerticalAdapter", "City: ${item.city}")
            Log.d("VerticalAdapter", "Price: ${item.price}")

            // Set the click listener for the item
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

}

