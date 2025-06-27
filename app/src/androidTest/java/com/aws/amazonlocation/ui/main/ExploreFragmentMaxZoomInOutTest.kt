package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_MAX_ZOOM_NOT_REACHED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMaxZoomInOutTest : BaseTestMainActivity() {
    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun testMapZoomOut() {
        performZoomUntilMaxReached(isZoomIn = true) {
            val map = uiDevice.findObject(
                UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/mapView")
            )
            if (map.exists()) map.pinchIn(50, 15)
        }
    }

    @Test
    fun testMaxZoomDoubleTap() {
        performZoomUntilMaxReached(isZoomIn = false) {
            onView(withId(R.id.mapView)).perform(ViewActions.doubleClick())
        }
    }

    private fun performZoomUntilMaxReached(isZoomIn: Boolean, zoomAction: () -> Unit) {
        try {
            checkLocationPermission()
            var mapbox: MapLibreMap? = null
            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync { mapbox = it }

            var beforeZoomLevel = mapbox?.cameraPosition?.zoom
            var isMaxZoomReached = false

            while (!isMaxZoomReached) {
                zoomAction()
                val currentZoomLevel = mapbox?.cameraPosition?.zoom

                if (beforeZoomLevel != null && currentZoomLevel != null) {
                    isMaxZoomReached = beforeZoomLevel == currentZoomLevel
                    beforeZoomLevel = currentZoomLevel
                }
            }

            Assert.assertTrue(TEST_FAILED_MAX_ZOOM_NOT_REACHED, isMaxZoomReached)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
