package com.dicoding.easytour.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.databinding.FragmentDashboardBinding
import com.dicoding.easytour.entity.HomeEntity
import com.dicoding.easytour.ui.adapter.HorizontalAdapter
import com.dicoding.easytour.ui.adapter.VerticalAdapter
import com.dicoding.easytour.ui.detail.DetailActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var verticalAdapter: VerticalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeFavoritePlaces()

        // Fetch the favorite places
        dashboardViewModel.fetchFavoritePlaces()
    }

    private fun setupViewModel() {
        dashboardViewModel = ViewModelProvider(
            this,
            ViewModelFactory.getInstance(requireContext())
        )[DashboardViewModel::class.java]
    }

    private fun setupRecyclerView() {
        verticalAdapter = VerticalAdapter { item ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("EXTRA_ID", item.iD)
                putExtra("EXTRA_PHOTO_URL", item.imageUrl)
                putExtra("EXTRA_NAME", item.name)
                putExtra("EXTRA_PRICE", item.price)
                putExtra("EXTRA_RATING", item.rating)
                putExtra("EXTRA_DESCRIPTION", item.description)
                putExtra("EXTRA_CITY", item.city)
            }
            startActivity(intent)
        }

        binding.rvHorizontal.apply { // This is the RecyclerView you're using
            adapter = verticalAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) // Change to vertical
        }
    }

    // Observe the favorite places
    private fun observeFavoritePlaces() {
        dashboardViewModel.favoritePlaces.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    verticalAdapter.submitList(result.data)
                // Set the list for the vertical adapter
                }
                is ResultState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun deleteFavorite(homeEntity: HomeEntity) {
        lifecycleScope.launch {
            dashboardViewModel.removeFavorite(homeEntity)
            Toast.makeText(context, "${homeEntity.name} removed from favorites", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

