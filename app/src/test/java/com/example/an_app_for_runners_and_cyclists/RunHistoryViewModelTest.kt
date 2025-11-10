package com.example.an_app_for_runners_and_cyclists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.ui.history.RunHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RunHistoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: RunHistoryViewModel
    private lateinit var runRepository: RunRepository
    private lateinit var userRepository: UserRepository

    private val testUser = User(
        id = "user1",
        name = "Test User",
        email = "test@example.com"
    )

    private val testRuns = listOf(
        Run(
            id = "1",
            userId = "user1",
            startTime = System.currentTimeMillis() - 86400000,
            distance = 5.0f,
            duration = 1800000,
            calories = 300
        ),
        Run(
            id = "2",
            userId = "user1",
            startTime = System.currentTimeMillis() - 172800000,
            distance = 3.0f,
            duration = 1200000,
            calories = 200
        )
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        runRepository = mock()
        userRepository = mock()

        whenever(userRepository.getCurrentUser()).thenReturn(testUser)
        whenever(runRepository.getAllRuns("user1")).thenReturn(flowOf(testRuns))

        viewModel = RunHistoryViewModel(runRepository, userRepository)

        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadRunsShouldPopulateMonthlyRuns() = runTest {
        advanceUntilIdle()

        assertTrue(viewModel.monthlyRuns.value.isNotEmpty())
        assertEquals(2, viewModel.totalStats.value.totalRuns)
        assertEquals(8.0f, viewModel.totalStats.value.totalDistance)
    }

    @Test
    fun testToggleMonthExpansionShouldUpdateExpandedMonths() = runTest {
        val monthKey = "2024-1"

        viewModel.toggleMonthExpansion(monthKey)
        advanceUntilIdle()

        assertTrue(viewModel.expandedMonths.value.contains(monthKey))

        viewModel.toggleMonthExpansion(monthKey)
        advanceUntilIdle()

        assertFalse(viewModel.expandedMonths.value.contains(monthKey))
    }
}