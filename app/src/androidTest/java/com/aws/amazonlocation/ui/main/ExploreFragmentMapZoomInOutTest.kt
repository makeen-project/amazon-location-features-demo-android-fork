package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL
import com.aws.amazonlocation.TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED
import com.aws.amazonlocation.actions.doubleTap
import com.aws.amazonlocation.actions.pinchIn
import com.aws.amazonlocation.actions.pinchOut
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.retryRule.Retry
import com.aws.amazonlocation.waitForView
import com.aws.amazonlocation.waitUntil
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapZoomInOutTest : BaseTestMainActivity() {

    private fun getMapInstance(): MapLibreMap? {
        var mapbox: MapLibreMap? = null
        val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
        mapView.getMapAsync { mapbox = it }
        return mapbox
    }

    private fun validateZoomChange(beforeZoom: Double?, afterZoom: Double?, shouldIncrease: Boolean) {
        if (beforeZoom != null && afterZoom != null) {
            val zoomChanged = if (shouldIncrease) afterZoom > beforeZoom else afterZoom < beforeZoom
            Assert.assertTrue(TEST_FAILED_ZOOM_LEVEL_NOT_CHANGED, zoomChanged)
        } else {
            Assert.fail(TEST_FAILED_ZOOM_LEVEL)
        }
    }

    @Test
    @Retry
    fun testMapZoomIn() {
        try {
            checkLocationPermission()
            waitForView(allOf(withId(R.id.mapView), isDisplayed()))
            val mapbox = getMapInstance()
            val beforeZoom = mapbox?.cameraPosition?.zoom

            onView(withId(R.id.mapView)).perform(
                pinchOut(), pinchOut(), pinchOut(), pinchOut(), pinchOut(), pinchOut()
            )

            waitUntil(DELAY_3000, 25) {
                mapbox?.cameraPosition?.zoom?.let { beforeZoom != null && it > beforeZoom }
            }

            val afterZoom = mapbox?.cameraPosition?.zoom
            validateZoomChange(beforeZoom, afterZoom, shouldIncrease = true)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    @Test
    @Retry
    fun testMapZoomOut() {
        try {
            checkLocationPermission()
            waitForView(allOf(withId(R.id.mapView), isDisplayed()))
            val mapbox = getMapInstance()
            val beforeZoom = mapbox?.cameraPosition?.zoom

            onView(withId(R.id.mapView)).perform(
                pinchIn(), pinchIn(), pinchIn(), pinchIn(), pinchIn(), pinchIn()
            )

            waitUntil(DELAY_3000, 25) {
                mapbox?.cameraPosition?.zoom?.let { beforeZoom != null && it < beforeZoom }
            }

            val afterZoom = mapbox?.cameraPosition?.zoom
            validateZoomChange(beforeZoom, afterZoom, shouldIncrease = false)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    @Test
    @Retry
    fun testMapZoomDoubleTap() {
        try {
            checkLocationPermission()
            waitForView(allOf(withId(R.id.mapView), isDisplayed()))
            val mapbox = getMapInstance()
            val beforeZoom = mapbox?.cameraPosition?.zoom

            onView(withId(R.id.mapView)).perform(
                doubleTap(), doubleTap(), doubleTap(), doubleTap()
            )

            waitUntil(DELAY_3000, 25) {
                mapbox?.cameraPosition?.zoom?.let { beforeZoom != null && it > beforeZoom }
            }

            val afterZoom = mapbox?.cameraPosition?.zoom
            validateZoomChange(beforeZoom, afterZoom, shouldIncrease = true)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
