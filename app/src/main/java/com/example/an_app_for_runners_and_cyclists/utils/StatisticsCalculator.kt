package com.example.an_app_for_runners_and_cyclists.utils

import com.example.an_app_for_runners_and_cyclists.data.model.Run

object StatisticsCalculator {

    data class UserStats(
        val totalDistance: Float,
        val totalDuration: Long,
        val totalCalories: Int,
        val totalRuns: Int,
        val averagePace: Float
    )

    fun calculateUserStats(runs: List<Run>): UserStats {
        if (runs.isEmpty()) {
            return UserStats(0f, 0L, 0, 0, 0f)
        }

        val totalDistance = runs.sumOf { it.distance.toDouble() }.toFloat()
        val totalDuration = runs.sumOf { it.duration }
        val totalCalories = runs.sumOf { it.calories }
        val totalRuns = runs.size

        // Рассчитываем средний темп
        val averagePace = if (totalDistance > 0 && totalDuration > 0) {
            RunCalculator.calculatePace(totalDistance, totalDuration)
        } else {
            0f
        }

        return UserStats(
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            totalCalories = totalCalories,
            totalRuns = totalRuns,
            averagePace = averagePace
        )
    }

    fun calculateMonthlyStats(runs: List<Run>): Map<String, UserStats> {
        return runs.groupBy { run ->
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = run.startTime
            }
            "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH) + 1}"
        }.mapValues { (_, monthRuns) ->
            calculateUserStats(monthRuns)
        }
    }
}