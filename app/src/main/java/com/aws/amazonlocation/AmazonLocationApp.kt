package com.aws.amazonlocation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aws.amazonlocation.utils.AmplifyHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltAndroidApp
class AmazonLocationApp : Application() {

    @Inject
    lateinit var amplifyHelper: AmplifyHelper

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (!BuildConfig.DEBUG) {
            amplifyHelper.initAmplify()
        }
    }
}
