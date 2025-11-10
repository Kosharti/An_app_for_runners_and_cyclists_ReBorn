package com.example.an_app_for_runners_and_cyclists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    fun createUser(name: String, email: String, password: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _signUpState.value = SignUpState.Error("Please fill all fields")
            return
        }

        if (password.length < 6) {
            _signUpState.value = SignUpState.Error("Password must be at least 6 characters")
            return
        }

        _signUpState.value = SignUpState.Loading

        viewModelScope.launch {
            try {
                val existingUser = userRepository.getUserByEmail(email)
                if (existingUser != null) {
                    _signUpState.value = SignUpState.Error("User with this email already exists")
                    return@launch
                }

                val user = User(
                    id = email,
                    name = name,
                    email = email,
                    password = password,
                    address = null,
                    profileImage = null,
                    height = null,
                    weight = null,
                    runningReason = "Fitness and health",
                    totalDistance = 0f,
                    totalTime = 0L,
                    totalCalories = 0
                )

                userRepository.createUser(user)

                val loggedInUser = userRepository.login(email, password)
                if (loggedInUser != null) {
                    _signUpState.value = SignUpState.Success(loggedInUser)
                } else {
                    _signUpState.value = SignUpState.Error("Registration successful but login failed")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("Failed to create account: ${e.message}")
            }
        }
    }

    sealed class SignUpState {
        object Idle : SignUpState()
        object Loading : SignUpState()
        data class Success(val user: User) : SignUpState()
        data class Error(val message: String) : SignUpState()
    }
}