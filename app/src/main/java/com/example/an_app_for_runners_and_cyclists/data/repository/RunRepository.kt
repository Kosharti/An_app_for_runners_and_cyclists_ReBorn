package com.example.an_app_for_runners_and_cyclists.data.repository

import com.example.an_app_for_runners_and_cyclists.data.model.Run
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun getRunsByUser(userId: String): Flow<List<Run>>
    fun getRun(runId: String): Flow<Run?>
    suspend fun saveRun(run: Run)
    suspend fun updateRun(run: Run)
    suspend fun deleteRun(runId: String)
    fun getTotalDistance(userId: String): Flow<Float>
    fun getTotalDuration(userId: String): Flow<Long>
    fun getTotalCalories(userId: String): Flow<Int>
    fun getAllRuns(userId: String): Flow<List<Run>> // Добавляем новый метод
}