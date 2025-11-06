package com.example.an_app_for_runners_and_cyclists.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка "Create a New Account" - переход к регистрации
        binding.btnCreateAccount.setOnClickListener {
            // Навигация к экрану регистрации через NavController
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        // ВРЕМЕННО: любая социальная кнопка ведет к основному приложению
        // В реальном приложении здесь будет проверка логина/пароля
        binding.tvLoginWith.setOnClickListener {
            navigateToMainApp()
        }

        // В setupClickListeners SignInFragment.kt добавляем:
        binding.facebook.setOnClickListener {
            // TODO: Implement Facebook login
            navigateToMainApp()
        }

        binding.google.setOnClickListener {
            // TODO: Implement Google login
            navigateToMainApp()
        }

        binding.facebook.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_loginFragment)
        }
        // Или можно добавить кнопку "Skip" для тестирования
    }

    private fun navigateToMainApp() {
        // Вызываем метод у Activity для перехода к основному приложению
        (requireActivity() as? com.example.an_app_for_runners_and_cyclists.SignInActivity)
            ?.navigateToMainApp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}