package com.example.an_app_for_runners_and_cyclists.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.LatLng
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RunTrackingViewModel(
    application: Application,
    private val runRepository: RunRepository
) : AndroidViewModel(application) {

    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _calories = MutableStateFlow(0)
    val calories: StateFlow<Int> = _calories.asStateFlow()

    private val _pace = MutableStateFlow(0f)
    val pace: StateFlow<Float> = _pace.asStateFlow()

    private val _heartRate = MutableStateFlow(87) // Mock data
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _weatherInfo = MutableStateFlow(WeatherInfo(30, "Thunder Storm")) // Mock data
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private var startTime: Long = 0L
    private var trackedLocations: List<LatLng> = emptyList()

    fun startTracking() {
        _trackingState.value = TrackingState.TRACKING
        startTime = System.currentTimeMillis()
        _elapsedTime.value = 0L
        _distance.value = 0f
        _calories.value = 0
        _pace.value = 0f
        trackedLocations = emptyList()

        // In a real app, we would start the LocationService here
        // For now, we'll simulate tracking with a coroutine
        simulateTracking()
    }

    fun stopTracking() {
        _trackingState.value = TrackingState.IDLE
        saveRun()
    }

    fun pauseTracking() {
        _trackingState.value = TrackingState.PAUSED
    }

    fun resumeTracking() {
        _trackingState.value = TrackingState.TRACKING
        simulateTracking()
    }

    private fun simulateTracking() {
        viewModelScope.launch {
            while (trackingState.value == TrackingState.TRACKING) {
                // Update elapsed time
                _elapsedTime.value = System.currentTimeMillis() - startTime

                // Simulate distance increase (in km)
                _distance.value += 0.01f

                // Calculate calories (simplified)
                _calories.value = RunCalculator.calculateCalories(_distance.value)

                // Calculate pace (min per km)
                _pace.value = RunCalculator.calculatePace(_distance.value, _elapsedTime.value)

                // Simulate location updates
                trackedLocations = trackedLocations + LatLng(
                    latitude = 37.7749 + (trackedLocations.size * 0.0001),
                    longitude = -122.4194 + (trackedLocations.size * 0.0001),
                    timestamp = System.currentTimeMillis()
                )

                // Wait 1 second
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun saveRun() {
        viewModelScope.launch {
            val run = Run(
                id = UUID.randomUUID().toString(),
                userId = "user1", // Hardcoded for now
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                distance = _distance.value,
                duration = _elapsedTime.value,
                calories = _calories.value,
                pace = _pace.value,
                coordinates = trackedLocations,
                weatherCondition = _weatherInfo.value.condition,
                temperature = _weatherInfo.value.temperature,
                averageHeartRate = _heartRate.value
            )
            runRepository.saveRun(run)
        }
    }

    enum class TrackingState {
        IDLE, TRACKING, PAUSED
    }

    data class WeatherInfo(
        val temperature: Int,
        val condition: String
    )
}