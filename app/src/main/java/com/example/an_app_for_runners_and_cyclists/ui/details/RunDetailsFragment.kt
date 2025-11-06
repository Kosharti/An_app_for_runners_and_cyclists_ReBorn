package com.example.an_app_for_runners_and_cyclists.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
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
        observeViewModel()
        viewModel.loadRun(args.runId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.run.collectLatest { run ->
                run?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(run: com.example.an_app_for_runners_and_cyclists.data.model.Run) {
        // Update UI with run details
        // binding.tvDate.text = RunCalculator.formatDate(run.startTime)
        // binding.tvDistance.text = String.format("%.2f km", run.distance)
        // binding.tvDuration.text = RunCalculator.formatDuration(run.duration)
        // binding.tvCalories.text = "${run.calories} kCal"
        // binding.tvPace.text = RunCalculator.formatPace(run.pace)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}