package com.example.an_app_for_runners_and_cyclists.data.repository

import com.example.an_app_for_runners_and_cyclists.data.local.UserDao
import com.example.an_app_for_runners_and_cyclists.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

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
}