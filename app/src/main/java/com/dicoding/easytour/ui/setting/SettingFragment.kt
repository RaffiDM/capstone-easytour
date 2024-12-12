package com.dicoding.easytour.ui.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.dicoding.easytour.MyWorker
import com.dicoding.easytour.R
import com.dicoding.easytour.SettingViewModelFactory
import com.dicoding.easytour.ViewModelFactory
import com.dicoding.easytour.data.EasyTourRepository
import com.dicoding.easytour.data.ResultState
import com.dicoding.easytour.data.pref.SettingPreference
import com.dicoding.easytour.databinding.FragmentSettingBinding
import com.dicoding.easytour.ui.login.LoginActivity
import com.dicoding.easytour.ui.notifications.NotificationsViewModel
import java.util.concurrent.TimeUnit

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var workManager: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    // Menggunakan SettingViewModelFactory untuk mendapatkan instance SettingViewModel
    private val viewModel by viewModels<SettingViewModel> {
        val factory = SettingViewModelFactory.getInstance(requireContext())
        factory
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted.")
            startPeriodicNotificationTask()
        } else {
            Toast.makeText(requireContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workManager = WorkManager.getInstance(requireContext())

        setupObservers()
        setupActions()
    }

    private fun setupObservers() {
        viewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive ->
            binding.switchTheme.isChecked = isDarkModeActive
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        viewModel.getNotificationSetting().observe(viewLifecycleOwner) { isNotifOn ->
            binding.switchNotif.isChecked = isNotifOn
        }

        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvUsername.text = result.data.username
                    binding.tvEmail.text = result.data.email
                }
                is ResultState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.logoutStatus.observe(viewLifecycleOwner) { isLoggedOut ->
            if (isLoggedOut) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun setupActions() {
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveThemeSetting(isChecked)
        }

        binding.switchNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    startPeriodicNotificationTask()
                }
            } else {
                cancelPeriodicNotificationTask()
            }
            viewModel.saveNotificationSetting(isChecked)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun startPeriodicNotificationTask() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        periodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "EventNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Log.d(TAG, "Periodic notification task started")
    }

    private fun cancelPeriodicNotificationTask() {
        workManager.cancelUniqueWork("EventNotificationWork")
        Log.d(TAG, "Periodic notification task canceled")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SettingFragment"
    }
}
