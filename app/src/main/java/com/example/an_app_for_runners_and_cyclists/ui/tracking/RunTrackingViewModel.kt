package com.example.an_app_for_runners_and_cyclists.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class RunTrackingViewModel(
    application: Application,
    private val runRepository: RunRepository,
    private val userRepository: UserRepository
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

    private val _heartRate = MutableStateFlow(75)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _weatherInfo = MutableStateFlow(WeatherInfo(22, "Sunny", "☀️"))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private var trackingStartTime: Long = 0L
    private var simulationJob: kotlinx.coroutines.Job? = null
    private var heartRateSimulationJob: kotlinx.coroutines.Job? = null
    private var weatherSimulationJob: kotlinx.coroutines.Job? = null

    private val weatherConditions = listOf(
        WeatherInfo(22, "Sunny", "☀️"),
        WeatherInfo(18, "Cloudy", "☁️"),
        WeatherInfo(15, "Rainy", "🌧️"),
        WeatherInfo(20, "Partly Cloudy", "⛅"),
        WeatherInfo(25, "Clear", "🌤️"),
        WeatherInfo(12, "Windy", "💨")
    )

    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation: StateFlow<Boolean> = _showSaveConfirmation.asStateFlow()

    fun startTracking() {
        Timber.d("🚀 SIMULATION: Starting tracking simulation")

        _trackingState.value = TrackingState.TRACKING
        trackingStartTime = System.currentTimeMillis()

        _elapsedTime.value = 0L
        _distance.value = 0f
        _calories.value = 0
        _pace.value = 0f
        _heartRate.value = 75

        simulationJob = viewModelScope.launch {
            var simulatedDistance = 0f
            var lastUpdateTime = trackingStartTime

            while (_trackingState.value == TrackingState.TRACKING) {
                val currentTime = System.currentTimeMillis()
                val timePassed = currentTime - lastUpdateTime

                if (timePassed >= 1000) {
                    val distanceIncrement = 2.78f
                    simulatedDistance += distanceIncrement / 1000

                    _elapsedTime.value = currentTime - trackingStartTime
                    _distance.value = simulatedDistance
                    _calories.value = (simulatedDistance * 60).toInt()
                    _pace.value = RunCalculator.calculatePace(simulatedDistance, _elapsedTime.value)

                    Timber.d("🏃 SIMULATION: Time=${_elapsedTime.value}ms, Distance=${String.format("%.3f", simulatedDistance)}km")

                    lastUpdateTime = currentTime
                }

                kotlinx.coroutines.delay(100)
            }
        }

        startHeartRateSimulation()

        startWeatherSimulation()
    }

    private fun startHeartRateSimulation() {
        heartRateSimulationJob?.cancel()

        heartRateSimulationJob = viewModelScope.launch {
            var baseHeartRate = 75

            while (_trackingState.value == TrackingState.TRACKING) {
                val targetHeartRate = 120 + (Math.random() * 40).toInt()

                if (baseHeartRate < targetHeartRate) {
                    baseHeartRate += 2
                }

                val currentHeartRate = baseHeartRate + (Math.random() * 10 - 5).toInt()
                _heartRate.value = currentHeartRate.coerceIn(70, 170)

                Timber.d("💓 Heart rate: ${_heartRate.value} BPM")
                delay(3000)
            }

            while (baseHeartRate > 75) {
                baseHeartRate -= 1
                _heartRate.value = baseHeartRate + (Math.random() * 10 - 5).toInt()
                delay(1000)
            }
        }
    }

    private fun startWeatherSimulation() {
        weatherSimulationJob?.cancel()

        weatherSimulationJob = viewModelScope.launch {
            var currentWeatherIndex = 0

            while (true) {
                delay(60000)

                if (_trackingState.value != TrackingState.TRACKING) {
                    continue
                }

                val randomIndex = (Math.random() * weatherConditions.size).toInt()
                _weatherInfo.value = weatherConditions[randomIndex]

                Timber.d("🌤️ Weather changed to: ${_weatherInfo.value.condition} ${_weatherInfo.value.emoji}")
            }
        }
    }

    fun stopTracking() {
        Timber.d("🛑 SIMULATION: Stopping tracking simulation")

        _trackingState.value = TrackingState.IDLE
        simulationJob?.cancel()
        heartRateSimulationJob?.cancel()
        weatherSimulationJob?.cancel()

        saveSimulatedRun()

        Timber.d("✅ SIMULATION: Final stats - Distance: ${_distance.value}km, Time: ${_elapsedTime.value}ms")
    }

    private fun saveSimulatedRun() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                val run = Run(
                    id = UUID.randomUUID().toString(),
                    userId = currentUser.id,
                    startTime = trackingStartTime,
                    endTime = System.currentTimeMillis(),
                    distance = _distance.value,
                    duration = _elapsedTime.value,
                    calories = _calories.value,
                    pace = _pace.value,
                    coordinates = emptyList(),
                    weatherCondition = _weatherInfo.value.condition,
                    temperature = _weatherInfo.value.temperature,
                    averageHeartRate = _heartRate.value
                )

                runRepository.saveRun(run)
                Timber.d("💾 SIMULATION: Run saved to database - ID: ${run.id}")

                _showSaveConfirmation.value = true
            } else {
                Timber.e("❌ SIMULATION: No current user - cannot save run")
            }
        }
    }

    fun resetTrackingData() {
        _elapsedTime.value = 0L
        _distance.value = 0f
        _calories.value = 0
        _pace.value = 0f
        _showSaveConfirmation.value = false
    }

    enum class TrackingState {
        IDLE, TRACKING
    }

    data class WeatherInfo(
        val temperature: Int,
        val condition: String,
        val emoji: String
    )
}