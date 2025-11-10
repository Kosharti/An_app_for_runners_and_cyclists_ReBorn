package com.example.an_app_for_runners_and_cyclists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.ui.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ProfileViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var runRepository: RunRepository

    private val testUser = User(
        id = "test123",
        name = "Test User",
        email = "test@example.com",
        password = "password",
        height = 180,
        weight = 75,
        runningReason = "Fitness"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock()
        runRepository = mock()
        viewModel = ProfileViewModel(userRepository, runRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartEditingShouldSetIsEditingToTrue() = runTest {
        viewModel.startEditing()

        assertTrue(viewModel.isEditing.value)
    }

    @Test
    fun testCancelEditingShouldResetEditingState() = runTest {
        viewModel.startEditing()

        viewModel.cancelEditing()

        advanceUntilIdle()

        assertFalse(viewModel.isEditing.value)
    }

    @Test
    fun testDeleteAccountShouldCallRepositoryAndSetSuccessState() = runTest {
        whenever(userRepository.getCurrentUser()).thenReturn(testUser)

        viewModel.deleteAccount()

        advanceUntilIdle()

        verify(userRepository).deleteCurrentUser()
        assertTrue(viewModel.deleteState.value is ProfileViewModel.DeleteState.Success)
    }
}