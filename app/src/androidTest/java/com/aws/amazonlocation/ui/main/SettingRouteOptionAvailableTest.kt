package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingRouteOptionAvailableTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSettingRouteOptionAvailableTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val explorer =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_setting)))
            explorer.click()

            Thread.sleep(DELAY_1000)

            val defaultRouteOption =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_default_route_options)))
            defaultRouteOption.click()

            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/tv_avoid_ferries")),
                DELAY_10000
            )
            uiDevice.wait(
                Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/tv_avoid_tools")),
                DELAY_10000
            )

            val tvAvoidFerries =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_avoid_ferries)

            val tvAvoidTools =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_avoid_tools)
            Assert.assertTrue(tvAvoidFerries.visibility == View.VISIBLE && tvAvoidTools.visibility == View.VISIBLE)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
