package com.aws.amazonlocation.ui.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingStartTrackingTest : BaseTest() {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showStartTrackingTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker))),
                DELAY_5000,
            )
            val labelContinue =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker)))
            labelContinue?.click()

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
                DELAY_5000,
            )

            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/btn_enable_tracking")),
                DELAY_15000,
            )
            Thread.sleep(DELAY_2000)
            uiDevice.findObject(By.res("${BuildConfig.APPLICATION_ID}:id/btn_enable_tracking"))?.click()

            Thread.sleep(DELAY_5000)
            Thread.sleep(DELAY_3000)

            uiDevice.wait(
                Until.hasObject(By.text(ApplicationProvider.getApplicationContext<Context>().getString(R.string.label_start_tracking))),
                DELAY_5000,
            )
            val labelStartTracking =
                uiDevice.findObject(By.text(ApplicationProvider.getApplicationContext<Context>().getString(R.string.label_start_tracking)))
            labelStartTracking?.click()

            Thread.sleep(DELAY_20000)
            uiDevice.wait(
                Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_tracking")),
                DELAY_15000,
            )
            Thread.sleep(DELAY_2000)
            Espresso.onView(CoreMatchers.allOf(withId(R.id.rv_tracking), isDisplayed())).check(
                matches(
                    hasMinimumChildCount(1),
                ),
            )
        } catch (e: Exception) {
            failTest(93, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
