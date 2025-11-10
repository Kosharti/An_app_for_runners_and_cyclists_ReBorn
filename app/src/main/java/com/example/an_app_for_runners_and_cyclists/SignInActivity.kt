package com.example.an_app_for_runners_and_cyclists

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.an_app_for_runners_and_cyclists.databinding.ActivitySignInBinding
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIfUserAlreadyLoggedIn()
        setupNavigation()
    }

    private fun checkIfUserAlreadyLoggedIn() {
        lifecycleScope.launch {
            val userRepository = (application as RunnersExchangeApplication).userRepository
            val isLoggedIn = userRepository.isLoggedIn()

            if (isLoggedIn) {
                navigateToMainApp()
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_sign_in) as NavHostFragment
    }

    fun navigateToMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}