package com.example.an_app_for_runners_and_cyclists.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// User.kt
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val password: String = "", // Для OAuth оставляем пустым
    val profileImage: String? = null,
    val address: String? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val runningReason: String? = null,
    val totalDistance: Float = 0f,
    val totalTime: Long = 0L,
    val totalCalories: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    // Новые поля для OAuth
    val authProvider: String = "email", // "email", "google", "vk"
    val providerId: String? = null, // ID из Google
    val accessToken: String? = null, // Токен доступа (опционально)
    val refreshToken: String? = null // Refresh token (опционально)
)