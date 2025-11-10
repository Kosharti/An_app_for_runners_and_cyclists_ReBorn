package com.example.an_app_for_runners_and_cyclists.data.local

import androidx.room.*
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY startTime DESC")
    fun getRunsByUser(userId: String): Flow<List<Run>>

    @Query("SELECT * FROM runs WHERE id = :runId")
    fun getRun(runId: String): Flow<Run?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Update
    suspend fun updateRun(run: Run)

    @Query("DELETE FROM runs WHERE id = :runId")
    suspend fun deleteRun(runId: String)

    @Query("SELECT SUM(distance) FROM runs WHERE userId = :userId")
    fun getTotalDistance(userId: String): Flow<Float>

    @Query("SELECT SUM(duration) FROM runs WHERE userId = :userId")
    fun getTotalDuration(userId: String): Flow<Long>

    @Query("SELECT SUM(calories) FROM runs WHERE userId = :userId")
    fun getTotalCalories(userId: String): Flow<Int>

    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllRuns(userId: String): Flow<List<Run>>

    @Query("DELETE FROM runs WHERE userId = :userId")
    suspend fun deleteAllRunsForUser(userId: String)
}