package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_1
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentEnableLocationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testMapEnableLocation() {
        try {
            Thread.sleep(DELAY_2000)
            val btnContinueToApp = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
                Thread.sleep(DELAY_2000)
                try {
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP_1))?.click()
                    uiDevice.findObject(By.text(ALLOW))?.click()

                    enableGPS(ApplicationProvider.getApplicationContext())
                    uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
                    var mapbox: MapboxMap? = null
                    val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
                    mapView.getMapAsync {
                        mapbox = it
                    }
                    Assert.assertTrue(TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED, mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true)
                } catch (e: UiObjectNotFoundException) {
                    failTest(67, e)
                    Assert.fail(TEST_FAILED)
                }
            } else {
                try {
                    uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()

                    uiDevice.findObject(By.text(ALLOW))?.click()

                    enableGPS(ApplicationProvider.getApplicationContext())
                    uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
                    var mapbox: MapboxMap? = null
                    val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
                    mapView.getMapAsync {
                        mapbox = it
                    }
                    Assert.assertTrue(TEST_FAILED_LOCATION_COMPONENT_NOT_ACTIVATED_OR_ENABLED, mapbox?.locationComponent?.isLocationComponentActivated == true && mapbox?.locationComponent?.isLocationComponentEnabled == true)
                } catch (e: UiObjectNotFoundException) {
                    failTest(85, e)
                    Assert.fail(TEST_FAILED)
                }
            }
        } catch (e: Exception) {
            failTest(90, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
