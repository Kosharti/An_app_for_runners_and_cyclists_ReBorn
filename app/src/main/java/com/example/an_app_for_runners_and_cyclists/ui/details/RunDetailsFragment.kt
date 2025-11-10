package com.example.an_app_for_runners_and_cyclists.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentRunDetailsBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RunDetailsFragment : Fragment() {

    private var _binding: FragmentRunDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: RunDetailsFragmentArgs by navArgs()
    private val viewModel: RunDetailsViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        observeViewModel()
        viewModel.loadRun(args.runId)
    }

    private fun setupHeader() {
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDropdownMenu() {
        val popup = PopupMenu(requireContext(), binding.menuIcon)
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_tracking -> {
                    findNavController().navigate(R.id.runTrackingFragment)
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.run.collectLatest { run ->
                run?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(run: com.example.an_app_for_runners_and_cyclists.data.model.Run) {
        binding.tvDate.text = RunCalculator.formatDate(run.startTime)
        binding.tvDistance.text = String.format("%.2f km", run.distance)
        binding.tvDuration.text = RunCalculator.formatDuration(run.duration)
        binding.tvCalories.text = "${run.calories} kCal"
        binding.tvPace.text = "${RunCalculator.formatPace(run.pace)} min/km"

        val additionalInfoLayout = binding.root.findViewById<LinearLayout>(R.id.additional_info_layout)

        run.weatherCondition?.let { weather ->
            binding.tvWeather.text = "Weather: $weather"
            binding.tvWeather.visibility = View.VISIBLE
        } ?: run { binding.tvWeather.visibility = View.GONE }

        run.temperature?.let { temp ->
            binding.tvTemperature.text = "Temperature: ${temp}°C"
            binding.tvTemperature.visibility = View.VISIBLE
        } ?: run { binding.tvTemperature.visibility = View.GONE }

        run.averageHeartRate?.let { heartRate ->
            binding.tvHeartRate.text = "Avg Heart Rate: ${heartRate} BPM"
            binding.tvHeartRate.visibility = View.VISIBLE
        } ?: run { binding.tvHeartRate.visibility = View.GONE }

        val hasAdditionalInfo = run.weatherCondition != null || run.temperature != null || run.averageHeartRate != null
        additionalInfoLayout?.visibility = if (hasAdditionalInfo) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}