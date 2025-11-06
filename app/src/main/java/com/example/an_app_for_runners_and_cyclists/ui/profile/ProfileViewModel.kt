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

    init {
        loadUser()
    }

    private fun loadUser() {
        // For now, using hardcoded user ID. In real app, get from auth
        val userId = "user1"

        viewModelScope.launch {
            userRepository.getUser(userId).collect { user ->
                _user.value = user ?: createDefaultUser(userId)
            }
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun saveUser(updatedUser: User) {
        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
            _user.value = updatedUser
            _isEditing.value = false
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    private fun createDefaultUser(userId: String): User {
        return User(
            id = userId,
            name = "Bobby A. Munson",
            email = "munson1450@gmail.com",
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
}