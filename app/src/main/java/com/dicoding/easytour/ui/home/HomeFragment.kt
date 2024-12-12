package com.dicoding.easytour.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.data.pref.UserPreference
import com.dicoding.easytour.data.pref.dataStore
import com.dicoding.easytour.databinding.FragmentHomeBinding
import com.dicoding.easytour.ui.adapter.HorizontalAdapter
import com.dicoding.easytour.ui.adapter.VerticalAdapter
import com.dicoding.easytour.ui.detail.DetailActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel> {
        val factory = ViewModelFactory.getInstance(requireContext())
        factory
    }

    private lateinit var verticalAdapter: VerticalAdapter
    private lateinit var horizontalAdapter: HorizontalAdapter
    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext().dataStore)

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

        horizontalAdapter = HorizontalAdapter { item ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("EXTRA_ID", item.iD)
                putExtra("EXTRA_PHOTO_URL", item.imageUrl)
                putExtra("EXTRA_NAME", item.name)
               putExtra("EXTRA_RATING", item.rating)
                putExtra("EXTRA_PRICE", item.price)
                putExtra("EXTRA_DESCRIPTION", item.description)
                putExtra("EXTRA_CITY", item.city)
            }
            startActivity(intent)
        }

        setupRecyclerViews()
        observeHomeData()
    }

    private fun setupRecyclerViews() {
        binding.rvHorizontal.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = horizontalAdapter
        }
        binding.rvVertical.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = verticalAdapter
        }
    }

    private fun observeHomeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                val userId = user.userId
                val username = user.username // Ambil username
                if (userId.isNotEmpty()) {
                    // Call getHomeData only once
                    binding.tvWelcome.text = "Welcome, $username"
                    viewModel.getHomeData(userId)

                    // Observe the homeData LiveData
                    viewModel.homeData.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is ResultState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                Log.d("HomeFragment", "Loading data...")
                            }
                            is ResultState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Log.d("HomeFragment", "Data loaded successfully: ${result.data}")
                                horizontalAdapter.submitList(result.data)
                            }
                            is ResultState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Log.e("HomeFragment", "Error loading data: ${result.error}")
                                Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                } else {
                    Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    verticalAdapter.submitList(state.data)
                }
                is ResultState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.searchPlace("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}