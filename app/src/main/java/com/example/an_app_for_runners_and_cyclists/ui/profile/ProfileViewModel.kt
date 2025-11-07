package com.example.an_app_for_runners_and_cyclists.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            // Используем нового пользователя из сессии
            val currentUser = userRepository.getCurrentUser()
            _user.value = currentUser
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun saveUser(updatedUser: User) {
        if (!validateUserData(updatedUser)) {
            _saveState.value = SaveState.Error("Please fill all required fields correctly")
            return
        }

        _saveState.value = SaveState.Loading

        viewModelScope.launch {
            try {
                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _isEditing.value = false
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
        _saveState.value = SaveState.Idle
    }

    private fun validateUserData(user: User): Boolean {
        return when {
            user.name.isBlank() -> false
            user.email.isBlank() || !isValidEmail(user.email) -> false
            user.height == null || user.height <= 0 -> false
            user.weight == null || user.weight <= 0 -> false
            user.runningReason.isNullOrBlank() -> false
            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun createDefaultUser(userId: String): User {
        return User(
            id = userId,
            name = "Bobby A. Munson",
            email = "munson1450@gmail.com",
            password = "password123", // ДОБАВЬТЕ ЭТУ СТРОЧКУ
            address = "4865 Plainfield Avenue\nSyracuse, NY 13202",
            profileImage = null,
            height = 180,
            weight = 75,
            runningReason = "Fitness and health",
            totalDistance = 15f,
            totalTime = 10251L, // 2:50:51 in seconds
            totalCalories = 1540
        )
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}