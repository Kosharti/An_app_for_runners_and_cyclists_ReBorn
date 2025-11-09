package com.example.an_app_for_runners_and_cyclists.ui.tracking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
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

    private val _heartRate = MutableStateFlow(87)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    private val _weatherInfo = MutableStateFlow(WeatherInfo(25, "Sunny"))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private var trackingStartTime: Long = 0L
    private var simulationJob: kotlinx.coroutines.Job? = null

    // ПРОСТАЯ СИМУЛЯЦИЯ - без TrackingManager и LocationService
    fun startTracking() {
        Timber.d("🚀 SIMULATION: Starting tracking simulation")

        _trackingState.value = TrackingState.TRACKING
        trackingStartTime = System.currentTimeMillis()

        // Сбрасываем значения
        _elapsedTime.value = 0L
        _distance.value = 0f
        _calories.value = 0
        _pace.value = 0f

        // Запускаем симуляцию
        simulationJob = viewModelScope.launch {
            var simulatedDistance = 0f
            var lastUpdateTime = trackingStartTime

            while (_trackingState.value == TrackingState.TRACKING) {
                val currentTime = System.currentTimeMillis()
                val timePassed = currentTime - lastUpdateTime

                if (timePassed >= 1000) { // Обновляем каждую секунду
                    // Симулируем бег: примерно 10 км/ч = 2.78 м/с
                    val distanceIncrement = 2.78f // метры в секунду
                    simulatedDistance += distanceIncrement / 1000 // переводим в км

                    _elapsedTime.value = currentTime - trackingStartTime
                    _distance.value = simulatedDistance
                    _calories.value = (simulatedDistance * 60).toInt() // упрощенная формула
                    _pace.value = RunCalculator.calculatePace(simulatedDistance, _elapsedTime.value)

                    Timber.d("🏃 SIMULATION: Time=${_elapsedTime.value}ms, Distance=${String.format("%.3f", simulatedDistance)}km, Pace=${_pace.value}")

                    lastUpdateTime = currentTime
                }

                kotlinx.coroutines.delay(100) // Небольшая задержка для оптимизации
            }
        }
    }

    fun stopTracking() {
        Timber.d("🛑 SIMULATION: Stopping tracking simulation")

        _trackingState.value = TrackingState.IDLE
        simulationJob?.cancel()

        // Сохраняем пробежку
        saveSimulatedRun()

        // Не сбрасываем значения сразу, чтобы пользователь видел финальные результаты
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
                    coordinates = emptyList(), // В симуляции нет реальных координат
                    weatherCondition = _weatherInfo.value.condition,
                    temperature = _weatherInfo.value.temperature,
                    averageHeartRate = _heartRate.value
                )

                runRepository.saveRun(run)
                Timber.d("💾 SIMULATION: Run saved to database - ID: ${run.id}")

                // Показываем уведомление о сохранении
                _showSaveConfirmation.value = true
            } else {
                Timber.e("❌ SIMULATION: No current user - cannot save run")
            }
        }
    }

    // Для сброса значений после сохранения
    fun resetTrackingData() {
        _elapsedTime.value = 0L
        _distance.value = 0f
        _calories.value = 0
        _pace.value = 0f
        _showSaveConfirmation.value = false
    }

    // Flow для показа подтверждения сохранения
    private val _showSaveConfirmation = MutableStateFlow(false)
    val showSaveConfirmation: StateFlow<Boolean> = _showSaveConfirmation.asStateFlow()

    enum class TrackingState {
        IDLE, TRACKING
    }

    data class WeatherInfo(
        val temperature: Int,
        val condition: String
    )
}