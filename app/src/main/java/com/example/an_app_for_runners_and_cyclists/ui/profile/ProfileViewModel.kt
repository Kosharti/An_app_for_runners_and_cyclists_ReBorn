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
            userRepository.getCurrentUser()?.let { currentUser ->
                _user.value = currentUser
            }
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun saveUserData(
        name: String,
        email: String,
        height: Int?,
        weight: Int?,
        runningReason: String
    ) {
        val currentUser = _user.value ?: return

        if (name.isEmpty() || email.isEmpty()) {
            _saveState.value = SaveState.Error("Name and email are required")
            return
        }

        if (!isValidEmail(email)) {
            _saveState.value = SaveState.Error("Please enter a valid email")
            return
        }

        _saveState.value = SaveState.Loading

        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(
                    name = name,
                    email = email,
                    height = height,
                    weight = weight,
                    runningReason = runningReason
                )

                userRepository.updateUser(updatedUser)
                _user.value = updatedUser
                _isEditing.value = false
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error("Failed to save profile: ${e.message}")
            }
        }
    }

    fun cancelEditing() {
        _isEditing.value = false
        _saveState.value = SaveState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch
            val updatedUser = currentUser.copy(profileImage = imageUri)
            userRepository.updateUser(updatedUser)
            _user.value = updatedUser
        }
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}