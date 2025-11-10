package com.example.an_app_for_runners_and_cyclists.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.an_app_for_runners_and_cyclists.R
import com.example.an_app_for_runners_and_cyclists.data.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

class LocationService : Service() {

    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState

    private val _trackedLocations = MutableStateFlow<List<LatLng>>(emptyList())
    val trackedLocations: StateFlow<List<LatLng>> = _trackedLocations

    private var startTime: Long = 0L
    private var totalDistance: Float = 0f
    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun startTracking() {
        _trackingState.value = TrackingState.TRACKING
        startTime = System.currentTimeMillis()
        totalDistance = 0f
        lastLocation = null
        _trackedLocations.value = emptyList()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            _trackingState.value = TrackingState.ERROR
        }
    }

    fun stopTracking(): RunData {
        _trackingState.value = TrackingState.IDLE
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        val calories = (totalDistance * 60).roundToInt()

        return RunData(
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            distance = totalDistance,
            calories = calories,
            locations = _trackedLocations.value
        )
    }

    fun pauseTracking() {
        _trackingState.value = TrackingState.PAUSED
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun resumeTracking() {
        _trackingState.value = TrackingState.TRACKING
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            _trackingState.value = TrackingState.ERROR
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).setMinUpdateIntervalMillis(3000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )

                    _currentLocation.value = latLng

                    lastLocation?.let { last ->
                        val distance = last.distanceTo(location)
                        totalDistance += distance
                    }

                    lastLocation = location

                    _trackedLocations.value = _trackedLocations.value + latLng
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            LOCATION_CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
            .setContentTitle("Tracking your run")
            .setContentText("Recording your route and stats")
            .setSmallIcon(R.drawable.ic_run)
            .build()
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    fun updateTrackingData(elapsedTime: Long, distance: Float, calories: Int) {
    }

    data class RunData(
        val startTime: Long,
        val endTime: Long,
        val duration: Long,
        val distance: Float,
        val calories: Int,
        val locations: List<LatLng>
    )

    enum class TrackingState {
        IDLE, TRACKING, PAUSED, ERROR
    }

    companion object {
        const val NOTIFICATION_ID = 1234
        const val LOCATION_CHANNEL_ID = "location_tracking"
    }
}