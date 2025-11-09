package com.example.an_app_for_runners_and_cyclists.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.an_app_for_runners_and_cyclists.data.local.UserDao
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.utils.StatisticsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val runRepository: RunRepository, // Добавляем зависимость
    private val context: Context
) : UserRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private var currentUser: User? = null

    override fun getUser(userId: String): Flow<User?> {
        return userDao.getUser(userId)
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Новые методы для управления сессией

    override suspend fun login(email: String, password: String): User? {
        val user = getUserByEmail(email)

        if (user != null && user.password == password) {
            // Сохраняем ID текущего пользователя
            prefs.edit().putString("current_user_id", user.id).apply()
            currentUser = user
            return user
        }

        return null
    }

    override suspend fun getCurrentUser(): User? {
        // Если уже есть в памяти, возвращаем
        if (currentUser != null) return currentUser

        // Иначе пытаемся восстановить из SharedPreferences
        val userId = prefs.getString("current_user_id", null)
        return if (userId != null) {
            // Загружаем пользователя из базы
            userDao.getUser(userId).first().also {
                currentUser = it
            }
        } else {
            null
        }
    }

    override fun logout() {
        currentUser = null
        prefs.edit().remove("current_user_id").apply()
    }

    override suspend fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    override suspend fun updateUserStats(userId: String) {
        val runs = runRepository.getAllRuns(userId).first()
        val stats = StatisticsCalculator.calculateUserStats(runs)

        val user = userDao.getUser(userId).first()
        user?.let {
            val updatedUser = it.copy(
                totalDistance = stats.totalDistance,
                totalTime = stats.totalDuration,
                totalCalories = stats.totalCalories
            )
            userDao.updateUser(updatedUser)
        }
    }

    override suspend fun getUsersWithStats(): Flow<List<User>> {
        return userDao.getAllUsers().map { users ->
            users.map { user ->
                val runs = runRepository.getAllRuns(user.id).first()
                val stats = StatisticsCalculator.calculateUserStats(runs)
                user.copy(
                    totalDistance = stats.totalDistance,
                    totalTime = stats.totalDuration,
                    totalCalories = stats.totalCalories
                )
            }
        }
    }

    override suspend fun recalculateAllUsersStats() {
        val users = userDao.getAllUsers().first()
        users.forEach { user ->
            updateUserStats(user.id)
        }
    }

    // Добавляем метод для получения всех пользователей
    override suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers().first()
    }
}