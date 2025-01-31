package com.aws.amazonlocation.ui.main

import android.location.Location
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.mockLocationsExit
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingStartTrackingMapDisplayTest : BaseTestMainActivity() {

    @Test
    fun showStartTrackingTest() {
        try {
            var mapbox: MapLibreMap? = null
            checkLocationPermission()

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                    isDisplayed(),
                ),
            )?.perform(click())

            val itemCount = 0

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.label_start_tracking)),
                    isDisplayed(),
                ),
            )?.perform(click())

            var idCount = 0
            mapbox?.getStyle { style ->
                var isImageFind = false
                while (isImageFind) {
                    val image = style.getImage("tracker$idCount")
                    if (image == null) {
                        isImageFind = true
                    } else {
                        idCount++
                    }
                }
            }

            for (mockLocation in mockLocationsExit) {
                mapbox?.let {
                    mActivityRule.activity.runOnUiThread {
                        val locationComponent: LocationComponent = it.locationComponent
                        locationComponent.isLocationComponentEnabled = true

                        val lastKnownLocation = Location("gps")
                        lastKnownLocation.latitude = mockLocation.latitude
                        lastKnownLocation.longitude = mockLocation.longitude
                        lastKnownLocation.time = System.currentTimeMillis()
                        lastKnownLocation.accuracy = 1.0f
                        it.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude
                                ),
                                14.0
                            )
                        )

                        it.locationComponent.forceLocationUpdate(lastKnownLocation)
                    }
                }
                runBlocking {
                    delay(DELAY_3000) // Sleep for the specified delay time
                }
                val latLng = LatLng(
                    mockLocation.latitude,
                    mockLocation.longitude
                )
                (mActivityRule.activity as MainActivity).mTrackingUtils?.updateLatLngOnMap(
                    latLng
                )
            }
            val rvSearchPlaceSuggestion = waitForView(allOf(withId(R.id.rv_tracking), isDisplayed(), hasMinimumChildCount(1)))
            rvSearchPlaceSuggestion?.check { view, _ ->
                if (view is RecyclerView) {
                    if (itemCount < (view.adapter?.itemCount ?: 0)) {
                        mapbox?.getStyle { style ->
                            mActivityRule.activity.runOnUiThread {
                                idCount++
                                val image = style.getImage("tracker$idCount")
                                Assert.assertTrue(TEST_FAILED_IMAGE_NULL, image != null)
                            }
                        }
                    }
                } else {
                    Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
                }
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
