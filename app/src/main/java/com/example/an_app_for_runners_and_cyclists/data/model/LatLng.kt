package com.example.an_app_for_runners_and_cyclists.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LatLng(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class LatLngListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<LatLng> {
        if (value.isEmpty()) return emptyList()
        val type = object : TypeToken<List<LatLng>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toString(list: List<LatLng>): String {
        return gson.toJson(list)
    }
}