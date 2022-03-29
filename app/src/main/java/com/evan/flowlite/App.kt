package com.evan.flowlite

import android.app.Application

class App : Application() {
    companion object{
        private lateinit var mApplication:Application
        fun getApp() = mApplication
    }
    override fun onCreate() {
        super.onCreate()
        mApplication = this
    }
}