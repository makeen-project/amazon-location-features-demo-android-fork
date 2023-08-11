package com.aws.amazonlocation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aws.amazonlocation.ui.main.CrashListener
import com.aws.amazonlocation.utils.AmplifyHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.system.exitProcess

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltAndroidApp
class AmazonLocationApp : Application() {

    @Inject
    lateinit var amplifyHelper: AmplifyHelper
    private var crashListener: CrashListener? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (!BuildConfig.DEBUG) {
            amplifyHelper.initAmplify()
        }
        setupUncaughtExceptionHandler()
    }
    private fun setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            notifyAppCrash(throwable.message)
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
    fun setCrashListener(listener: CrashListener) {
        crashListener = listener
    }

    private fun notifyAppCrash(message: String?) {
        crashListener?.notifyAppCrash(message)
    }
}
