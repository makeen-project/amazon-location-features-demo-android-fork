package com.aws.amazonlocation

import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@Throws(UiObjectNotFoundException::class)
fun enableGPS(context: Context) {
    if (!isLocationEnabled(context)) {
        val uiDevice: UiDevice =
            UiDevice.getInstance(getInstrumentation())

        val allowGpsBtn: UiObject = uiDevice.findObject(
            UiSelector()
                .className("android.widget.Button").packageName("com.google.android.gms")
                .resourceId("android:id/button1")
                .clickable(true).checkable(false)
        )
        uiDevice.pressDelete()
        if (allowGpsBtn.exists() && allowGpsBtn.isEnabled) {
            do {
                allowGpsBtn.click()
            } while (allowGpsBtn.exists())
        }
    }
}

fun checkLocationPermission() {
    val uiDevice = UiDevice.getInstance(getInstrumentation())
    val btnContinueToApp =
        uiDevice.findObject(
            UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app")
        )
    if (btnContinueToApp.exists()) {
        btnContinueToApp.click()
    }
    Thread.sleep(DELAY_1000)
    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
    uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
    uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
    uiDevice.findObject(By.text(ALLOW))?.click()
    enableGPS(ApplicationProvider.getApplicationContext())
    waitForView(
        allOf(
            withContentDescription(AMAZON_MAP_READY),
            isDisplayed()
        )
    )
}

fun allowNotificationPermission() {
    val uiDevice = UiDevice.getInstance(getInstrumentation())
    uiDevice.findObject(By.text(ALLOW))?.click()
}

fun performBackPress() {
    val uiDevice = UiDevice.getInstance(getInstrumentation())
    uiDevice.pressBack()
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

fun waitUntil(waitTime: Long, maxCount: Int, condition: () -> Boolean?) {
    var count = 0
    while (condition() != true) {
        Thread.sleep(waitTime)
        if (maxCount < ++count) {
            Assert.fail("$TEST_FAILED - Max count reached")
            break
        }
    }
}

fun waitForView(
    matcher: Matcher<View>,
    retryDelayMs: Long = 100,
    timeoutMs: Long = 10000,
    onNotFound: (() -> Unit)? = null
): ViewInteraction? {
    val startTime = System.currentTimeMillis()
    var lastError: Exception? = null

    do {
        try {
            val interaction = Espresso.onView(matcher)
            interaction.check(matches(isDisplayed()))
            return interaction
        } catch (e: Exception) {
            lastError = e
            Thread.sleep(retryDelayMs)
        }
    } while (System.currentTimeMillis() - startTime < timeoutMs)

    if (onNotFound == null) {
        throw Exception("$TEST_FAILED - Timeout after ${timeoutMs}ms", lastError)
    } else {
        onNotFound()
    }
    return null
}
