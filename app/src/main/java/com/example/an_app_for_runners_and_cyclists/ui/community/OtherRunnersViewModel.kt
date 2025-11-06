package com.example.an_app_for_runners_and_cyclists.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtherRunnersViewModel : ViewModel() {

    private val _runners = MutableStateFlow<List<User>>(emptyList())
    val runners: StateFlow<List<User>> = _runners.asStateFlow()

    init {
        loadRunners()
    }

    private fun loadRunners() {
        viewModelScope.launch {
            // Mock data for now
            val mockRunners = listOf(
                User(
                    id = "2",
                    name = "John Doe",
                    email = "john@example.com",
                    address = "123 Main St, New York, NY",
                    totalDistance = 25.5f,
                    totalTime = 15000L,
                    totalCalories = 2100
                ),
                User(
                    id = "3",
                    name = "Jane Smith",
                    email = "jane@example.com",
                    address = "456 Oak Ave, Los Angeles, CA",
                    totalDistance = 18.7f,
                    totalTime = 12000L,
                    totalCalories = 1800
                ),
                User(
                    id = "4",
                    name = "Mike Johnson",
                    email = "mike@example.com",
                    address = "789 Pine Rd, Chicago, IL",
                    totalDistance = 32.1f,
                    totalTime = 18000L,
                    totalCalories = 2500
                )
            )
            _runners.value = mockRunners
        }
    }
}