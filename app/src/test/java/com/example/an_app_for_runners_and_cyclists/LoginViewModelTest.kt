package com.example.an_app_for_runners_and_cyclists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.ui.LoginViewModel
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
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private lateinit var userRepository: UserRepository

    private val testUser = User(
        id = "test123",
        name = "Test User",
        email = "test@example.com",
        password = "password123"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock(UserRepository::class.java)
        viewModel = LoginViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoginWithValidCredentialsShouldReturnSuccess() = runTest {
        whenever(userRepository.getUserByEmail("test@example.com")).thenReturn(testUser)

        viewModel.login("test@example.com", "password123")

        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Success)
    }

    @Test
    fun testLoginWithWrongPasswordShouldReturnError() = runTest {
        whenever(userRepository.getUserByEmail("test@example.com")).thenReturn(testUser)

        viewModel.login("test@example.com", "wrongpassword")

        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
    }

    @Test
    fun testLoginWithNonExistingEmailShouldReturnError() = runTest {
        whenever(userRepository.getUserByEmail("nonexisting@example.com")).thenReturn(null)

        viewModel.login("nonexisting@example.com", "password")

        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
    }

    @Test
    fun testLoginWithEmptyFieldsShouldReturnError() = runTest {
        viewModel.login("", "")

        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
    }
}