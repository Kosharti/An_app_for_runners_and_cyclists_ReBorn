package com.example.an_app_for_runners_and_cyclists.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.utils.RunCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunHistoryViewModel(
    private val runRepository: RunRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _runs = MutableStateFlow<List<Run>>(emptyList())
    val runs: StateFlow<List<Run>> = _runs.asStateFlow()

    private val _monthlyRuns = MutableStateFlow<List<MonthlyRunGroup>>(emptyList())
    val monthlyRuns: StateFlow<List<MonthlyRunGroup>> = _monthlyRuns.asStateFlow()

    private val _totalStats = MutableStateFlow(TotalStats())
    val totalStats: StateFlow<TotalStats> = _totalStats.asStateFlow()

    private val _expandedMonths = MutableStateFlow<Set<String>>(emptySet())
    val expandedMonths: StateFlow<Set<String>> = _expandedMonths.asStateFlow()

    init {
        loadRuns()
    }

    private fun loadRuns() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            if (currentUser != null) {
                runRepository.getAllRuns(currentUser.id)
                    .distinctUntilChanged()
                    .collect { runsList ->
                        _runs.value = runsList
                        groupRunsByMonth(runsList)
                        calculateTotalStats(runsList)
                    }
            }
        }
    }

    private fun groupRunsByMonth(runs: List<Run>) {
        val grouped = runs.groupBy { run ->
            val calendar = Calendar.getInstance().apply { timeInMillis = run.startTime }
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
        }.map { (monthKey, monthRuns) ->
            val totalDistance = monthRuns.sumOf { it.distance.toDouble() }.toFloat()
            val totalDuration = monthRuns.sumOf { it.duration }
            val totalCalories = monthRuns.sumOf { it.calories }

            MonthlyRunGroup(
                monthKey = monthKey,
                displayName = formatMonthKey(monthKey),
                runs = monthRuns,
                totalRuns = monthRuns.size,
                totalDistance = totalDistance,
                totalDuration = totalDuration,
                totalCalories = totalCalories
            )
        }.sortedByDescending { it.monthKey }

        _monthlyRuns.value = grouped
    }

    private fun calculateTotalStats(runs: List<Run>) {
        val totalDistance = runs.sumOf { it.distance.toDouble() }.toFloat()
        val totalDuration = runs.sumOf { it.duration }
        val totalCalories = runs.sumOf { it.calories }

        _totalStats.value = TotalStats(
            totalRuns = runs.size,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            totalCalories = totalCalories
        )
    }

    fun toggleMonthExpansion(monthKey: String) {
        _expandedMonths.value = if (_expandedMonths.value.contains(monthKey)) {
            _expandedMonths.value - monthKey
        } else {
            _expandedMonths.value + monthKey
        }
    }

    private fun formatMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size != 2) return monthKey

        val year = parts[0]
        val month = parts[1].toInt()

        val monthName = when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }

        return "$monthName $year"
    }

    data class MonthlyRunGroup(
        val monthKey: String,
        val displayName: String,
        val runs: List<Run>,
        val totalRuns: Int,
        val totalDistance: Float,
        val totalDuration: Long,
        val totalCalories: Int
    )

    data class TotalStats(
        val totalRuns: Int = 0,
        val totalDistance: Float = 0f,
        val totalDuration: Long = 0L,
        val totalCalories: Int = 0
    )
}