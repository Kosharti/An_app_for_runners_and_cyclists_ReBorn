package com.example.an_app_for_runners_and_cyclists.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "runs")
@TypeConverters(LatLngListConverter::class)
data class Run(
    @PrimaryKey val id: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Float = 0f,
    val duration: Long = 0L,
    val calories: Int = 0,
    val pace: Float = 0f,
    val coordinates: List<LatLng> = emptyList(),
    val weatherCondition: String? = null,
    val temperature: Int? = null,
    val averageHeartRate: Int? = null
)