package com.example.an_app_for_runners_and_cyclists.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

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
                Timber.d("🔄 Loading runners from database...")
                val currentUser = userRepository.getCurrentUser()
                Timber.d("👤 Current user: ${currentUser?.name} (ID: ${currentUser?.id})")

                if (currentUser != null) {
                    val allUsers = userRepository.getAllUsers()
                    Timber.d("📊 Found ${allUsers.size} total users in database")

                    // Логируем всех пользователей
                    allUsers.forEach { user ->
                        Timber.d("👤 User: ${user.name}, Photo: ${user.profileImage}, Distance: ${user.totalDistance}km")
                    }

                    val filteredUsers = allUsers.filter { it.id != currentUser.id }
                    Timber.d("👥 Showing ${filteredUsers.size} other users (excluding current)")

                    _runners.value = filteredUsers

                    if (filteredUsers.isEmpty()) {
                        Timber.d("ℹ️ No other users found in database")
                    }
                } else {
                    Timber.e("❌ No current user - cannot load community")
                    _runners.value = emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "💥 Error loading runners")
                _runners.value = emptyList()
            }
        }
    }
}