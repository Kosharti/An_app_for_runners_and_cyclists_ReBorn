package com.example.an_app_for_runners_and_cyclists.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.an_app_for_runners_and_cyclists.RunnersExchangeApplication
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepositoryImpl
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepositoryImpl
import com.example.an_app_for_runners_and_cyclists.ui.community.OtherRunnersViewModel
import com.example.an_app_for_runners_and_cyclists.ui.details.RunDetailsViewModel
import com.example.an_app_for_runners_and_cyclists.ui.history.RunHistoryViewModel
import com.example.an_app_for_runners_and_cyclists.ui.profile.ProfileViewModel
import com.example.an_app_for_runners_and_cyclists.ui.tracking.RunTrackingViewModel

class ViewModelFactory(private val application: RunnersExchangeApplication) : ViewModelProvider.Factory {

    private val runRepository: RunRepository by lazy {
        RunRepositoryImpl(application.database.runDao())
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(application.database.userDao())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RunTrackingViewModel::class.java) -> {
                RunTrackingViewModel(application, runRepository) as T
            }
            modelClass.isAssignableFrom(RunHistoryViewModel::class.java) -> {
                RunHistoryViewModel(runRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(OtherRunnersViewModel::class.java) -> {
                OtherRunnersViewModel() as T
            }
            modelClass.isAssignableFrom(RunDetailsViewModel::class.java) -> {
                RunDetailsViewModel(runRepository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}