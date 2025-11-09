package com.example.an_app_for_runners_and_cyclists.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtherRunnersViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _runners = MutableStateFlow<List<User>>(emptyList())
    val runners: StateFlow<List<User>> = _runners.asStateFlow()

    init {
        loadRunners()
    }

    private fun loadRunners() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    // Получаем всех пользователей, кроме текущего
                    val allUsers = userRepository.getAllUsers()
                    _runners.value = allUsers.filter { it.id != currentUser.id }
                } else {
                    _runners.value = emptyList()
                }
            } catch (e: Exception) {
                // В случае ошибки показываем пустой список
                _runners.value = emptyList()
            }
        }
    }
}