package com.sunnyday.notesceneform.ui

import android.app.Application
import timber.log.Timber.*
import timber.log.Timber.Forest.plant


/**
 * Create by SunnyDay /11/16 10:27:28
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        plant(DebugTree())
    }
}