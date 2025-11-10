package com.example.an_app_for_runners_and_cyclists

import android.app.Application
import android.content.Context
import com.example.an_app_for_runners_and_cyclists.data.initializer.DataInitializer
import com.example.an_app_for_runners_and_cyclists.data.local.RunDatabase
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepositoryImpl
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepository
import com.example.an_app_for_runners_and_cyclists.data.repository.UserRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RunnersExchangeApplication : Application() {

    companion object {
        private var instance: RunnersExchangeApplication? = null

        fun getAppContext(): Context? = instance?.applicationContext
    }

    val database: RunDatabase by lazy {
        RunDatabase.getInstance(this)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            userDao = database.userDao(),
            runRepository = runRepository,
            context = this
        )
    }

    private val runRepository: RunRepository by lazy {
        RunRepositoryImpl(database.runDao())
    }


    override fun onCreate() {
        super.onCreate()
        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Application started")

        val runRepository = RunRepositoryImpl(database.runDao())
        val userRepository = UserRepositoryImpl(
            userDao = database.userDao(),
            runRepository = runRepository,
            context = this
        )
        val dataInitializer = DataInitializer(userRepository, runRepository)

        CoroutineScope(Dispatchers.IO).launch {
            dataInitializer.initializeData(this)
        }
    }
}