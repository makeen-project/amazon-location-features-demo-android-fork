package com.aws.amazonlocation.ui.main

import android.location.Location
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.mockLocationsExit
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showStartTrackingTest() {
        try {
            var mapbox: MapLibreMap? = null
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()

            Thread.sleep(DELAY_5000)
            val rvTracking =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_tracking)
            val itemCount = rvTracking.adapter?.itemCount ?: 0
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking))),
                DELAY_1000
            )
            val labelStartTracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
            labelStartTracking?.click()
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
            Thread.sleep(DELAY_20000)
            if (rvTracking.adapter?.itemCount != null) {
                rvTracking.adapter?.itemCount?.let {
                    if (itemCount < it) {
                        mapbox?.getStyle { style ->
                            mActivityRule.activity.runOnUiThread {
                                idCount++
                                val image = style.getImage("tracker$idCount")
                                Assert.assertTrue(TEST_FAILED_IMAGE_NULL, image != null)
                            }
                        }
                    }
                }
            } else {
                Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
            }
        } catch (e: Exception) {
            failTest(175, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
