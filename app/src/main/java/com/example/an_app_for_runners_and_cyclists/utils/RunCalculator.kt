package com.example.an_app_for_runners_and_cyclists.utils

import com.example.an_app_for_runners_and_cyclists.data.model.LatLng
import java.text.SimpleDateFormat
import java.util.Locale

object RunCalculator {

    fun calculateDistance(locations: List<LatLng>): Float {
        var totalDistance = 0f

        for (i in 0 until locations.size - 1) {
            val start = locations[i]
            val end = locations[i + 1]
            totalDistance += calculateHaversineDistance(
                start.latitude, start.longitude,
                end.latitude, end.longitude
            )
        }

        return totalDistance
    }

    fun calculatePace(distance: Float, timeMillis: Long): Float {
        if (distance == 0f || timeMillis == 0L) return 0f
        val timeMinutes = timeMillis / 60000.0
        return (timeMinutes / distance).toFloat()
    }

    fun calculateCalories(distance: Float, weight: Float = 70f): Int {
        return (distance * weight * 1.036).toInt()
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (R * c).toFloat()
    }

    fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatPace(pace: Float): String {
        if (pace == 0f) return "0:00"
        val minutes = pace.toInt()
        val seconds = ((pace - minutes) * 60).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }

    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy 'at' hh:mm a", Locale.getDefault())
        return dateFormat.format(timestamp)
    }
}