package com.example.an_app_for_runners_and_cyclists.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentSignUpBinding
import com.example.an_app_for_runners_and_cyclists.ui.ViewModelFactory

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
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val name = binding.etFullName.text?.toString()?.trim()
            val email = binding.etEmail.text?.toString()?.trim()
            val password = binding.etPassword.text?.toString()

            if (name.isNullOrEmpty() || email.isNullOrEmpty() || password.isNullOrEmpty()) {
                // Показываем ошибку
                return@setOnClickListener
            }

            // Показываем прогресс
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSubmit.isEnabled = false

            viewModel.createUser(
                name = name,
                email = email,
                password = password,
                onSuccess = {
                    // Успешная регистрация - переходим к основному приложению
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    navigateToMainApp()
                },
                onError = { errorMessage ->
                    // Показываем ошибку
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    // TODO: Показать Snackbar или Toast с ошибкой
                }
            )
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
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