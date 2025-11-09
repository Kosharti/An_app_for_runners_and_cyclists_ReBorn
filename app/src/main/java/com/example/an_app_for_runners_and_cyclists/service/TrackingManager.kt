package com.example.an_app_for_runners_and_cyclists.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingManager @Inject constructor(
    private val context: Context
) {
    private var locationService: LocationService? = null
    private var isBound = false

    private val _trackingState = MutableStateFlow(LocationService.TrackingState.IDLE)
    val trackingState: StateFlow<LocationService.TrackingState> = _trackingState

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance

    private val _calories = MutableStateFlow(0)
    val calories: StateFlow<Int> = _calories

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocationBinder
            locationService = binder.getService()
            isBound = true

            // Подписываемся на обновления из сервиса
            observeServiceUpdates()
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            locationService = null
            isBound = false
        }
    }

    fun startTracking() {
        if (!isBound) {
            val intent = Intent(context, LocationService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        locationService?.startTracking()
    }

    fun stopTracking(): LocationService.RunData? {
        val runData = locationService?.stopTracking()

        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            locationService = null
        }

        return runData
    }

    fun pauseTracking() {
        locationService?.pauseTracking()
    }

    fun resumeTracking() {
        locationService?.resumeTracking()
    }

    private fun observeServiceUpdates() {
        // Здесь будем подписываться на обновления из LocationService
        // Для упрощения будем использовать прямые вызовы
    }

    fun updateUIValues(elapsed: Long, dist: Float, cals: Int) {
        _elapsedTime.value = elapsed
        _distance.value = dist
        _calories.value = cals
    }
}