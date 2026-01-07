package com.example.doanck

import android.app.Application
import com.example.doanck.di.ServiceLocator

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
