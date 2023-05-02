package com.aws.amazonlocation

import android.content.Context
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import java.util.*
import kotlin.random.Random

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@Throws(UiObjectNotFoundException::class)
fun enableGPS(context: Context) {
    if (!isLocationEnabled(context)) {
        val uiDevice: UiDevice =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val allowGpsBtn: UiObject = uiDevice.findObject(
            UiSelector()
                .className("android.widget.Button").packageName("com.google.android.gms")
                .resourceId("android:id/button1")
                .clickable(true).checkable(false),
        )
        uiDevice.pressDelete()
        if (allowGpsBtn.exists() && allowGpsBtn.isEnabled) {
            do {
                allowGpsBtn.click()
            } while (allowGpsBtn.exists())
        }
    }
}

@Suppress("DEPRECATION")
fun isLocationEnabled(context: Context): Boolean {
    val locationMode: Int = try {
        Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
    } catch (e: Settings.SettingNotFoundException) {
        e.printStackTrace()
        return false
    }
    return locationMode != Settings.Secure.LOCATION_MODE_OFF
}

fun getRandomGeofenceName(): String {
    return "Geofence${Calendar.getInstance().timeInMillis}"
}

fun getRandom1To100(): Float {
    return Random.nextDouble(1.0, 100.0).toFloat()
}

fun getRandom0_01To1_0(): Double {
    return Random.nextDouble(0.01, 1.0)
}

data class MockLocation(val latitude: Double, val longitude: Double)

val mockLocations = listOf(
    MockLocation(23.016826, 72.540063),
    MockLocation(23.016717, 72.538942),
    MockLocation(23.016495, 72.537955),
    MockLocation(23.015542, 72.535514),
    MockLocation(23.015463, 72.534677),
    MockLocation(23.015364, 72.533839),
    MockLocation(23.015137, 72.532712),
    MockLocation(23.014985, 72.532109),
    MockLocation(23.014748, 72.531518),
    MockLocation(23.014630, 72.530917),
)

val mockLocationsExit = listOf(
    MockLocation(23.013287, 72.525249),
    MockLocation(23.013247, 72.524497),
    MockLocation(23.013142, 72.523544),
    MockLocation(23.013152, 72.522846),
    MockLocation(23.012348, 72.522382),
    MockLocation(23.011594, 72.522444),
)

fun printError(lineNo: Int, exception: Exception?) {
    System.out.println("Exception caught at line $lineNo: ${exception?.stackTraceToString() ?: "Custom error"}")
}
