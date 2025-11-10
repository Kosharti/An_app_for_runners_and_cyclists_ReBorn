package com.example.an_app_for_runners_and_cyclists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.an_app_for_runners_and_cyclists.data.model.User
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.ui.SignUpViewModel
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
class SignUpViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SignUpViewModel
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mock()
        viewModel = SignUpViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCreateUserWithValidDataShouldReturnSuccess() = runTest {
        whenever(userRepository.getUserByEmail("new@example.com")).thenReturn(null)
        whenever(userRepository.createUser(any())).thenReturn(Unit)
        whenever(userRepository.login("new@example.com", "password123")).thenReturn(
            User(
                id = "new@example.com",
                name = "New User",
                email = "new@example.com",
                password = "password123"
            )
        )

        viewModel.createUser("New User", "new@example.com", "password123")

        advanceUntilIdle()

        verify(userRepository).createUser(any())
        assertTrue(viewModel.signUpState.value is SignUpViewModel.SignUpState.Success)
    }


    @Test
    fun testCreateUserWithExistingEmailShouldReturnError() = runTest {
        val existingUser = User(id = "1", name = "Existing", email = "existing@example.com", password = "pass")
        whenever(userRepository.getUserByEmail("existing@example.com")).thenReturn(existingUser)

        viewModel.createUser("New User", "existing@example.com", "password123")

        advanceUntilIdle()

        assertTrue(viewModel.signUpState.value is SignUpViewModel.SignUpState.Error)
    }

    @Test
    fun testCreateUserWithShortPasswordShouldReturnError() = runTest {
        viewModel.createUser("New User", "new@example.com", "123")

        advanceUntilIdle()

        assertTrue(viewModel.signUpState.value is SignUpViewModel.SignUpState.Error)
    }

    @Test
    fun testCreateUserWithEmptyFieldsShouldReturnError() = runTest {
        viewModel.createUser("", "", "")

        advanceUntilIdle()

        assertTrue(viewModel.signUpState.value is SignUpViewModel.SignUpState.Error)
    }
}