package com.example.an_app_for_runners_and_cyclists.ui.profile

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
import com.example.an_app_for_runners_and_cyclists.data.model.User
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

    private var isEditing = false
    private var originalUser: User? = null

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
        setupHeader()
        observeViewModel()
    }

    private fun setupHeader() {
        binding.menuIcon.setOnClickListener {
            showDropdownMenu()
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
                R.id.action_profile -> true
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
        binding.btnEdit.setOnClickListener {
            toggleEditMode()
        }

        binding.btnSave.setOnClickListener {
            saveUserData()
        }

        // Кнопка назад
        binding.btnBack.setOnClickListener {
            if (isEditing) {
                cancelEditMode()
            } else {
                requireActivity().onBackPressed()
            }
        }

        binding.btnCamera.setOnClickListener {
            // TODO: Реализовать выбор фото
        }
    }

    private fun toggleEditMode() {
        isEditing = !isEditing
        updateEditModeUI()

        if (isEditing) {
            originalUser = viewModel.user.value
        }
    }

    private fun updateEditModeUI() {
        if (isEditing) {
            binding.btnSave.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
            enableEditing(true)
        } else {
            binding.btnSave.visibility = View.GONE
            binding.btnEdit.visibility = View.VISIBLE
            enableEditing(false)
        }
    }

    // ⚠️ УБЕРИ ЭТУ ФУНКЦИЮ - ОНА ДУБЛИРУЕТСЯ!
    // private fun enableEditing(editable: Boolean) {
    //     // TODO: Сделать поля редактируемыми когда будут ID
    // }

    private fun saveUserData() {
        val currentUser = viewModel.user.value ?: return

        val updatedUser = currentUser.copy(
            name = binding.etUserName.text.toString(),
            email = binding.etEmail.text.toString(),
            height = binding.etHeight.text.toString().toIntOrNull() ?: 0,
            weight = binding.etWeight.text.toString().toIntOrNull() ?: 0,
            runningReason = binding.etRunningReason.text.toString()
        )

        viewModel.saveUser(updatedUser)
        isEditing = false
        updateEditModeUI()
    }

    // ⚠️ Если хочешь оставить отмену редактирования, раскомментируй:

    private fun cancelEditMode() {
        originalUser?.let { viewModel.saveUser(it) }
        isEditing = false
        updateEditModeUI()
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectLatest { user ->
                user?.let { updateUI(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditing.collectLatest { editing ->
                if (editing != isEditing) {
                    isEditing = editing
                    updateEditModeUI()
                }
            }
        }
    }

    // ✅ ОСТАВЬ ТОЛЬКО ЭТУ ОДНУ ФУНКЦИЮ enableEditing
    private fun enableEditing(editable: Boolean) {
        binding.etUserName.isEnabled = editable
        binding.etEmail.isEnabled = editable
        binding.etHeight.isEnabled = editable
        binding.etWeight.isEnabled = editable
        binding.etRunningReason.isEnabled = editable
    }

    private fun updateUI(user: User) {
        updateStatsUI(user)

        binding.etUserName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etHeight.setText(user.height?.toString() ?: "")
        binding.etWeight.setText(user.weight?.toString() ?: "")
        binding.etRunningReason.setText(user.runningReason ?: "")
    }

    private fun updateStatsUI(user: User) {
        binding.tvDistanceValue.text = String.format("%.1f km", user.totalDistance)
        binding.tvDurationValue.text = RunCalculator.formatDuration(user.totalTime)
        binding.tvCaloriesValue.text = "${user.totalCalories} kCal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}