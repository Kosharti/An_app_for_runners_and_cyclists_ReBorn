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
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentLoginBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""

            viewModel.login(email, password)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginViewModel.LoginState.Idle -> {
                        hideProgress()
                    }
                    is LoginViewModel.LoginState.Loading -> {
                        showProgress()
                    }
                    is LoginViewModel.LoginState.Success -> {
                        hideProgress()
                        navigateToMainApp()
                    }
                    is LoginViewModel.LoginState.Error -> {
                        hideProgress()
                        showLoginError(state.message)
                    }
                }
            }
        }
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    private fun showLoginError(message: String) {
        binding.etEmail.error = message
        binding.etPassword.error = message
    }

    private fun navigateToMainApp() {
        (requireActivity() as? com.example.an_app_for_runners_and_cyclists.SignInActivity)
            ?.navigateToMainApp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}