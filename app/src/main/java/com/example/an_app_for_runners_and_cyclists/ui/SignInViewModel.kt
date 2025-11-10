// SignInViewModel.kt
package com.example.an_app_for_runners_and_cyclists.ui

import androidx.lifecycle.ViewModel
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository

class SignInViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    // Пока оставляем пустым, можно добавить логику если нужно
}