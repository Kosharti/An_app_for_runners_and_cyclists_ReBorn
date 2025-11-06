package com.example.an_app_for_runners_and_cyclists.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentProfileBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            viewModel.user.value?.let { user ->
                viewModel.saveUser(user)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectLatest { user ->
                user?.let { updateUI(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditing.collectLatest { isEditing ->
                updateEditMode(isEditing)
            }
        }
    }

    private fun updateUI(user: com.example.an_app_for_runners_and_cyclists.data.model.User) {
        // Update profile info - эти поля должны существовать в вашем XML
        // binding.tvUserName.text = user.name
        // binding.tvUserAddress.text = user.address

        // Update stats - эти поля должны существовать в вашем XML
        // binding.tvDistanceValue.text = String.format("%.1f km", user.totalDistance)
        // binding.tvDurationValue.text = RunCalculator.formatDuration(user.totalTime)
        // binding.tvCaloriesValue.text = "${user.totalCalories} kCal"
    }

    private fun updateEditMode(isEditing: Boolean) {
        // Implement edit mode logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}