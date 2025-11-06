package com.example.an_app_for_runners_and_cyclists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    fun createUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Проверяем, нет ли уже пользователя с таким email
                val existingUser = userRepository.getUser(email).let { flow ->
                    // Получаем первого пользователя из Flow (в реальном приложении нужна правильная реализация)
                    // Временно создаем нового пользователя
                    null
                }

                if (existingUser != null) {
                    onError("User with this email already exists")
                    return@launch
                }

                // Создаем нового пользователя
                val user = User(
                    id = email, // Используем email как ID для простоты
                    name = name,
                    email = email,
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
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to create account: ${e.message}")
            }
        }
    }
}