package com.jacks.moviet.app

import android.app.Application
import com.jacks.moviet.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MovietApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovietApplication)
            modules(appModule)
        }
    }
}
