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
import timber.log.Timber

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
            Timber.d("👉 START button clicked")
            viewModel.startTracking()
        }

        binding.stopButton.setOnClickListener {
            Timber.d("🛑 STOP button clicked")
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
            viewModel.showSaveConfirmation.collectLatest { show ->
                if (show) {
                    showSaveConfirmation()
                }
            }
        }
    }

    private fun updateUI(state: RunTrackingViewModel.TrackingState) {
        when (state) {
            RunTrackingViewModel.TrackingState.IDLE -> {
                binding.startButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.GONE
                Timber.d("🔄 UI: IDLE state - showing START button")
            }
            RunTrackingViewModel.TrackingState.TRACKING -> {
                binding.startButton.visibility = View.GONE
                binding.stopButton.visibility = View.VISIBLE
                Timber.d("🔄 UI: TRACKING state - showing STOP button")
            }
        }
    }

    private fun showSaveConfirmation() {
        // Показываем Snackbar или Toast о сохранении пробежки
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Run Saved!")
            .setMessage("Your run has been saved to history.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                viewModel.resetTrackingData()
            }
            .show()

        Timber.d("📋 Showed save confirmation dialog")
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