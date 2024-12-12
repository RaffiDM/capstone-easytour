package com.dicoding.easytour.ui.notifications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.easytour.GridAdapter
import com.dicoding.easytour.R
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.databinding.FilterDialogBinding
import com.dicoding.easytour.databinding.FragmentNotificationsBinding
import com.dicoding.easytour.request.FilterRequest
import com.dicoding.easytour.ui.adapter.VerticalAdapter
import com.dicoding.easytour.ui.detail.DetailActivity
import com.google.android.material.chip.ChipGroup

class NotificationsFragment : Fragment() {

    private val viewModel by viewModels<NotificationsViewModel> {
        val factory = ViewModelFactory.getInstance(requireContext())
        factory
    }
    private lateinit var verticalAdapter: VerticalAdapter
    private lateinit var gridAdapter: GridAdapter
    private lateinit var binding: FragmentNotificationsBinding
    private var lastFilterRequest: FilterRequest? = null
    private var lastQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Adapter
        verticalAdapter = VerticalAdapter { selectedItem ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("EXTRA_ID", selectedItem.iD)
                putExtra("EXTRA_PHOTO_URL", selectedItem.imageUrl)
                putExtra("EXTRA_NAME", selectedItem.name)
                putExtra("EXTRA_RATING", selectedItem.rating)
                putExtra("EXTRA_PRICE", selectedItem.price)
                putExtra("EXTRA_DESCRIPTION", selectedItem.description)
                putExtra("EXTRA_CITY", selectedItem.city)
            }
            startActivity(intent)
        }
        binding.rvSearch.adapter = verticalAdapter
        binding.rvSearch.layoutManager = LinearLayoutManager(context)

        binding.tvCancel.visibility = View.GONE

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            binding.tvCancel.visibility = if (hasFocus) View.VISIBLE else View.GONE
            if (!hasFocus) binding.searchEditText.text.clear()
            binding.rvGrid.visibility = if (hasFocus) View.GONE else View.VISIBLE
            binding.rvSearch.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }

        binding.searchEditText.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.searchEditText.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.bounds.width()
                    val isTouched = motionEvent.rawX >= (binding.searchEditText.right - binding.searchEditText.paddingEnd - drawableWidth)
                    if (isTouched) {
                        showFilterDialog()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        binding.tvCancel.setOnClickListener {
            binding.searchEditText.clearFocus()
            binding.tvCancel.visibility = View.GONE
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                lastQuery = query
                viewModel.searchPlace(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    gridAdapter.submitList(state.data)
                    verticalAdapter.submitList(state.data)
                }
                is ResultState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        gridAdapter = GridAdapter { selectedItem ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("EXTRA_ID", selectedItem.iD)
                putExtra("EXTRA_PHOTO_URL", selectedItem.imageUrl)
                putExtra("EXTRA_NAME", selectedItem.name)
                putExtra("EXTRA_RATING", selectedItem.rating)
                putExtra("EXTRA_PRICE", selectedItem.price)
                putExtra("EXTRA_DESCRIPTION", selectedItem.description)
                putExtra("EXTRA_CITY", selectedItem.city)
            }
            startActivity(intent)
        }

        binding.rvGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvGrid.adapter = gridAdapter

        viewModel.searchPlace("")
    }

    private fun showFilterDialog() {
        val binding = FilterDialogBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        val lastFilterReq = lastFilterRequest

        if (lastFilterReq != null) {
            when (lastFilterReq.city) {
                "Jakarta" -> binding.cityRadioGroup.check(R.id.rb_jakarta)
                "Bandung" -> binding.cityRadioGroup.check(R.id.rb_bandung)
                "Semarang" -> binding.cityRadioGroup.check(R.id.rb_semarang)
                "Surabaya" -> binding.cityRadioGroup.check(R.id.rb_surabaya)
                else -> binding.cityRadioGroup.check(R.id.rb_all)
            }

            binding.priceRangeSlider.setValues(lastFilterReq.ratingMin, lastFilterReq.ratingMax)

            when (lastFilterReq.sorting) {
                "rating_desc" -> binding.filterChipGroup.check(R.id.chip_rating_high)
                "rating_asc" -> binding.filterChipGroup.check(R.id.chip_rating_low)
                "price_desc" -> binding.filterChipGroup.check(R.id.chip_price_high)
                "price_asc" -> binding.filterChipGroup.check(R.id.chip_price_low)
            }
        }

        binding.priceRangeSlider.valueFrom = 0f
        binding.priceRangeSlider.valueTo = 100000f
        binding.priceRangeSlider.setValues(0f, 100000f)
        binding.priceRangeSlider.stepSize = 5000f

        binding.btnClear.setOnClickListener {
            lastFilterRequest = null
            viewModel.searchPlace(lastQuery.orEmpty())
            dialog.dismiss()
        }

        binding.btnApplyFilter.setOnClickListener {
            val city = when (binding.cityRadioGroup.checkedRadioButtonId) {
                R.id.rb_jakarta -> "Jakarta"
                R.id.rb_bandung -> "Bandung"
                R.id.rb_semarang -> "Semarang"
                R.id.rb_surabaya -> "Surabaya"
                else -> ""
            }

            val sorting = when (binding.filterChipGroup.checkedChipId) {
                R.id.chip_rating_high -> "rating_desc"
                R.id.chip_rating_low -> "rating_asc"
                R.id.chip_price_high -> "price_desc"
                R.id.chip_price_low -> "price_asc"
                else -> ""
            }

            val priceRange = binding.priceRangeSlider.values
            val priceMin = priceRange[0].toInt()
            val priceMax = priceRange[1].toInt()

            val newFilterRequest = FilterRequest(
                userID = "11",
                city = city,
                priceMin = priceMin,
                priceMax = priceMax,
                ratingMin = 0f,
                ratingMax = 5f,
                sorting = sorting
            )
            lastFilterRequest = newFilterRequest

            viewModel.filterPlace(newFilterRequest)
            dialog.dismiss()
        }

        dialog.show()
    }
}
