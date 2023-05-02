package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMaxZoomInOutTest {

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
    fun testMapZoomOut() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            var mapbox: MapboxMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            var beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            var isMaxZoomInReach = false
            while (!isMaxZoomInReach) {
                val map = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView"))
                if (map.exists()) {
                    map.pinchIn(50, 15)
                }
                Thread.sleep(DELAY_2000)
                if (beforeZoomLevel != null) {
                    mapbox?.cameraPosition?.zoom?.let {
                        if (beforeZoomLevel == it) {
                            isMaxZoomInReach = true
                        } else {
                            isMaxZoomInReach = false
                            beforeZoomLevel = mapbox?.cameraPosition?.zoom
                        }
                    }
                }
            }
            Assert.assertTrue(isMaxZoomInReach)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    @Test
    fun testMaxZoomDoubleTap() {
        try {
            var mapbox: MapboxMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            var beforeZoomLevel: Double? = mapbox?.cameraPosition?.zoom
            var isMaxZoomInReach = false
            while (!isMaxZoomInReach) {
                val map = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView"))
                if (map.exists()) {
                    onView(withId(R.id.mapView)).perform(ViewActions.doubleClick())
                }
                Thread.sleep(DELAY_2000)
                if (beforeZoomLevel != null) {
                    mapbox?.cameraPosition?.zoom?.let {
                        if (beforeZoomLevel == it) {
                            isMaxZoomInReach = true
                        } else {
                            isMaxZoomInReach = false
                            beforeZoomLevel = mapbox?.cameraPosition?.zoom
                        }
                    }
                }
            }
            Assert.assertTrue(isMaxZoomInReach)
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
