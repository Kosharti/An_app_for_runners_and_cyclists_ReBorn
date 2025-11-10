package com.example.an_app_for_runners_and_cyclists.data.initializer

import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class DataInitializer(
    private val userRepository: UserRepository,
    private val runRepository: RunRepository
) {

    fun initializeData(scope: CoroutineScope) {
        scope.launch {
            // Create default user if doesn't exist
            val userId = "user1"

            val user = User(
                id = userId,
                name = "Emily Stone",
                email = "emily1276@gmail.com",
                password = "qwerty123", // ДОБАВЬТЕ ПАРОЛЬ
                address = "4865 Plainfield Avenue\nSyracuse, NY 13202",
                profileImage = null,
                height = 168,
                weight = 68,
                runningReason = "Fitness and health",
                totalDistance = 15f,
                totalTime = 10251000L, // 2:50:51 in milliseconds
                totalCalories = 1540
            )

            userRepository.createUser(user)

            // Create some sample runs
            val sampleRuns = listOf(
                Run(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    startTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                    endTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L + 10251000L,
                    distance = 15f,
                    duration = 10251000L,
                    calories = 1540,
                    pace = 11.4f,
                    coordinates = emptyList(),
                    weatherCondition = "Sunny",
                    temperature = 25,
                    averageHeartRate = 87
                )
            )

            sampleRuns.forEach { run ->
                runRepository.saveRun(run)
            }
        }
    }
}