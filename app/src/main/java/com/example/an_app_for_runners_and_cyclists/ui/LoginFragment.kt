package com.example.an_app_for_runners_and_cyclists.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim()
            val password = binding.etPassword.text?.toString()

            if (isValidLogin(email, password)) {
                // TODO: Реальная проверка с базой данных
                navigateToMainApp()
            } else {
                showLoginError()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun isValidLogin(email: String?, password: String?): Boolean {
        // TODO: Заменить на реальную проверку с базой данных
        return !email.isNullOrEmpty() && !password.isNullOrEmpty() && password.length >= 6
    }

    private fun showLoginError() {
        binding.etEmail.error = "Invalid email or password"
        binding.etPassword.error = "Invalid email or password"
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