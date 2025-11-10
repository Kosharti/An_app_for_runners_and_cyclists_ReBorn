package com.example.an_app_for_runners_and_cyclists.data.repository

import com.example.an_app_for_runners_and_cyclists.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun getUserByEmail(email: String): User?

    suspend fun login(email: String, password: String): User?
    suspend fun getCurrentUser(): User?
    fun logout()
    suspend fun isLoggedIn(): Boolean

    suspend fun updateUserStats(userId: String)
    suspend fun getUsersWithStats(): Flow<List<User>>
    suspend fun recalculateAllUsersStats()

    suspend fun getAllUsers(): List<User>

    suspend fun findUserByProvider(provider: String, providerId: String): User?
    suspend fun createUserFromOAuth(user: User): User
    suspend fun linkGoogleAccount(userId: String, googleUser: User)

    suspend fun setCurrentUser(user: User)
}