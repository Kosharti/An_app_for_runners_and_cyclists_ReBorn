package com.example.an_app_for_runners_and_cyclists.ui.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentRunTrackingBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RunTrackingFragment : Fragment() {

    private var _binding: FragmentRunTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RunTrackingViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupHeader()
        observeViewModel()
    }

    private fun setupHeader() {
        // Обработчик меню (три полоски)
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }

        // Обработчик иконки истории (справа)
        binding.historyIcon.setOnClickListener {
            findNavController().navigate(R.id.runHistoryFragment)
        }
    }

    private fun showDropdownMenu() {
        val popup = PopupMenu(requireContext(), binding.menuIcon)
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tracking -> {
                    // Уже в трекинге
                    true
                }
                R.id.action_history -> {
                    findNavController().navigate(R.id.runHistoryFragment)
                    true
                }
                R.id.action_profile -> {
                    findNavController().navigate(R.id.profileFragment)
                    true
                }
                R.id.action_community -> {
                    findNavController().navigate(R.id.otherRunnersFragment)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupClickListeners() {
        binding.startButton.setOnClickListener {
            viewModel.startTracking()
        }

        binding.stopButton.setOnClickListener {
            viewModel.stopTracking()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackingState.collectLatest { state ->
                updateUI(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.elapsedTime.collectLatest { time ->
                binding.tvDuration.text = RunCalculator.formatDuration(time)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.distance.collectLatest { distance ->
                binding.tvDistance.text = String.format("%.2f km", distance)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.calories.collectLatest { calories ->
                binding.tvCalories.text = "$calories kCal"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.heartRate.collectLatest { heartRate ->
                binding.tvHeartRate.text = heartRate.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weatherInfo.collectLatest { weather ->
                binding.tvTemperature.text = "${weather.temperature}°C"
                binding.tvWeatherCondition.text = weather.condition
            }
        }
    }

    private fun updateUI(state: RunTrackingViewModel.TrackingState) {
        when (state) {
            RunTrackingViewModel.TrackingState.IDLE -> {
                binding.startButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.GONE
                resetStats()
            }
            RunTrackingViewModel.TrackingState.TRACKING -> {
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
            }
            RunTrackingViewModel.TrackingState.PAUSED -> {
                // Можно добавить кнопку возобновления
            }
        }
    }

    private fun resetStats() {
        binding.tvDuration.text = "00:00"
        binding.tvDistance.text = "0.00 km"
        binding.tvCalories.text = "0 kCal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}