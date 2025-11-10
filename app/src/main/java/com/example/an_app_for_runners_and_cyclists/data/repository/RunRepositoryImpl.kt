package com.example.an_app_for_runners_and_cyclists.data.repository

import com.example.an_app_for_runners_and_cyclists.data.local.RunDao
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import kotlinx.coroutines.flow.Flow

class RunRepositoryImpl(
    private val runDao: RunDao
) : RunRepository {

    override fun getRunsByUser(userId: String): Flow<List<Run>> {
        return runDao.getRunsByUser(userId)
    }

    override fun getRun(runId: String): Flow<Run?> {
        return runDao.getRun(runId)
    }

    override suspend fun saveRun(run: Run) {
        runDao.insertRun(run)
    }

    override suspend fun updateRun(run: Run) {
        runDao.updateRun(run)
    }

    override suspend fun deleteRun(runId: String) {
        runDao.deleteRun(runId)
    }

    override fun getTotalDistance(userId: String): Flow<Float> {
        return runDao.getTotalDistance(userId)
    }

    override fun getTotalDuration(userId: String): Flow<Long> {
        return runDao.getTotalDuration(userId)
    }

    override fun getTotalCalories(userId: String): Flow<Int> {
        return runDao.getTotalCalories(userId)
    }

    override fun getAllRuns(userId: String): Flow<List<Run>> {
        return runDao.getAllRuns(userId)
    }

    override suspend fun deleteAllRunsForUser(userId: String) {
        runDao.deleteAllRunsForUser(userId)
    }
}