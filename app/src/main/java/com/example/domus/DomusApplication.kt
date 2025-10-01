package com.example.domus

import android.app.Application

class DomusApplication : Application() {

    lateinit var networkManager: NetworkManager

    override fun onCreate() {
        super.onCreate()
        networkManager = NetworkManager(this)
        networkManager.checkNetworkStatus()
    }
}