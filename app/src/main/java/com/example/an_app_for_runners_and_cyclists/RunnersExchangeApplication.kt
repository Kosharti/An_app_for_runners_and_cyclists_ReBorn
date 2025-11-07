package com.example.an_app_for_runners_and_cyclists

import android.app.Application
import com.example.an_app_for_runners_and_cyclists.data.initializer.DataInitializer
import com.example.an_app_for_runners_and_cyclists.data.local.RunDatabase
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepositoryImpl
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RunnersExchangeApplication : Application() {

    val database: RunDatabase by lazy {
        RunDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize data
        val userRepository = UserRepositoryImpl(
            userDao = database.userDao(),
            context = this // ДОБАВЬТЕ ЭТУ СТРОЧКУ
        )
        val runRepository = RunRepositoryImpl(database.runDao())
        val dataInitializer = DataInitializer(userRepository, runRepository)

        CoroutineScope(Dispatchers.IO).launch {
            dataInitializer.initializeData(this)
        }
    }
}