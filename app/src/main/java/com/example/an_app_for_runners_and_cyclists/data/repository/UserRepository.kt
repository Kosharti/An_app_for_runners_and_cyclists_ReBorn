package com.example.an_app_for_runners_and_cyclists.data.repository

import com.example.an_app_for_runners_and_cyclists.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun getUserByEmail(email: String): User?

    // Новые методы для управления сессией
    suspend fun login(email: String, password: String): User?
    suspend fun getCurrentUser(): User?
    fun logout()
    suspend fun isLoggedIn(): Boolean
}