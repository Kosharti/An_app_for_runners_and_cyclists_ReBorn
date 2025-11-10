package com.example.an_app_for_runners_and_cyclists.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.SignInActivity
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentSignUpBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val name = binding.etFullName.text?.toString()?.trim()
            val email = binding.etEmail.text?.toString()?.trim()
            val password = binding.etPassword.text?.toString()

            if (name.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
                showError("Please fill all fields")
                return@setOnClickListener
            }

            viewModel.createUser(name, email, password)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signUpState.collectLatest { state ->
                when (state) {
                    is SignUpViewModel.SignUpState.Idle -> {
                        hideProgress()
                    }
                    is SignUpViewModel.SignUpState.Loading -> {
                        showProgress()
                    }
                    is SignUpViewModel.SignUpState.Success -> {
                        hideProgress()
                        navigateToMainApp(state.user)
                    }
                    is SignUpViewModel.SignUpState.Error -> {
                        hideProgress()
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
            .show()
    }


    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.btnSubmit.isEnabled = true
    }

    private fun navigateToMainApp(user: User) {
        (requireActivity() as? SignInActivity)?.navigateToMainApp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}