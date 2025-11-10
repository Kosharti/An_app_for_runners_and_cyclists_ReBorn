package com.example.an_app_for_runners_and_cyclists.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.auth.AuthResult
import com.example.an_app_for_runners_and_cyclists.auth.GoogleAuthManager
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.databinding.FragmentSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignInViewModel by viewModels {
        ViewModelFactory(requireActivity().application as RunnersExchangeApplication)
    }

    private val userRepository: UserRepository by lazy {
        (requireActivity().application as RunnersExchangeApplication).userRepository
    }

    private lateinit var googleAuthManager: GoogleAuthManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d("🔄 Google Sign-In result received: ${result.resultCode}")
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    handleGoogleSignInResult(result.data)
                }
            }
            Activity.RESULT_CANCELED -> {
                Timber.d("❌ Google Sign-In was canceled by user")
                hideProgress()
                showError("Google Sign-In was canceled")
            }
            else -> {
                Timber.d("❌ Google Sign-In failed with result code: ${result.resultCode}")
                hideProgress()
                showError("Google Sign-In failed")
            }
        }
    }

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

        googleAuthManager = GoogleAuthManager(requireActivity(), userRepository)

        setupClickListeners()
        observeViewModel()

        checkExistingGoogleSignIn()
    }

    private fun setupClickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.tvLoginWith.setOnClickListener {
            navigateToMainApp()
        }

        binding.vk.setOnClickListener {
            Snackbar.make(binding.root, "VK login will be implemented soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.google.setOnClickListener {
            Timber.d("👉 Google Sign-In button clicked")
            showProgress()
            try {
                val signInIntent = googleAuthManager.getSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to start Google Sign-In")
                hideProgress()
                showError("Failed to start Google Sign-In: ${e.message}")
            }
        }

        binding.email.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_loginFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
        }
    }

    private fun checkExistingGoogleSignIn() {
        viewLifecycleOwner.lifecycleScope.launch {
            val account = googleAuthManager.getCurrentGoogleAccount()
            account?.let {
                Timber.d("🔍 Found existing Google account: ${it.email}")
                handleGoogleSignInSuccess(createUserFromGoogleAccount(it))
            }
        }
    }

    private suspend fun handleGoogleSignInResult(data: Intent?) {
        Timber.d("🔄 Processing Google Sign-In result")

        when (val result = googleAuthManager.handleSignInResult(data)) {
            is AuthResult.Success -> {
                Timber.d("✅ Google Sign-In successful, handling user")
                handleGoogleSignInSuccess(result.user)
            }
            is AuthResult.Error -> {
                Timber.e("❌ Google Sign-In failed: ${result.message}")
                hideProgress()
                showError(result.message)
            }
        }
    }

    private fun handleGoogleSignInSuccess(googleUser: User) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val existingUser = userRepository.findUserByProvider("google", googleUser.providerId ?: "")

                if (existingUser != null) {
                    Timber.d("✅ Existing Google user found, logging in")
                    completeAuthentication(existingUser)
                } else {
                    Timber.d("🆕 Creating new Google user")
                    val newUser = userRepository.createUserFromOAuth(googleUser)
                    completeAuthentication(newUser)
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ Error handling Google Sign-In success")
                hideProgress()
                showError("Failed to process Google Sign-In: ${e.message}")
            }
        }
    }

    private fun completeAuthentication(user: User) {
        hideProgress()
        Timber.d("🎉 Authentication complete for user: ${user.email}")

        viewLifecycleOwner.lifecycleScope.launch {
            userRepository.setCurrentUser(user)
        }

        navigateToMainApp()
    }

    private fun showProgress() {
        _binding?.let {
            it.progressBar.visibility = View.VISIBLE
            it.google.isEnabled = false
            it.google.alpha = 0.5f
        }
    }

    private fun hideProgress() {
        _binding?.let {
            it.progressBar.visibility = View.GONE
            it.google.isEnabled = true
            it.google.alpha = 1.0f
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun createUserFromGoogleAccount(account: GoogleSignInAccount): User {
        return User(
            id = "google_${account.id ?: System.currentTimeMillis()}",
            name = account.displayName ?: "Google User",
            email = account.email ?: "",
            password = "",
            profileImage = account.photoUrl?.toString(),
            authProvider = "google",
            providerId = account.id,
            accessToken = account.idToken
        )
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