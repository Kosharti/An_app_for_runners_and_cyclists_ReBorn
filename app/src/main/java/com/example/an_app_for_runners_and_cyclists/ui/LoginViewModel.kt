package com.example.an_app_for_runners_and_cyclists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Please fill all fields")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val user = userRepository.getUserByEmail(email)
                if (user == null) {
                    _loginState.value = LoginState.Error("User not found")
                    return@launch
                }

                if (user.password != password) {
                    _loginState.value = LoginState.Error("Invalid password")
                    return@launch
                }

                _loginState.value = LoginState.Success(user)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: com.example.an_app_for_runners_and_cyclists.data.model.User) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}