@file:Suppress("DEPRECATION")

package com.example.appstory.data.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.security.crypto.MasterKeys
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    lateinit var masterKey: String

    override fun onCreate() {
        super.onCreate()
        masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        context = applicationContext
        Log.d("MyApp", "Application started with Hilt and WorkManager")
    }


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}