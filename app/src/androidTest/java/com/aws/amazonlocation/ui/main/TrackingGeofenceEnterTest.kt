package com.aws.amazonlocation.ui.main

import android.location.Location
import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_NOT_TRACKING_ENTERED_DIALOG
import com.aws.amazonlocation.TRACKING_ENTERED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.mockLocations
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingGeofenceEnterTest : BaseTestMainActivity() {

    @Test
    fun showTrackingGeofenceEnterTest() {
        checkLocationPermission()

        val tracking =
            waitForView(allOf(withText(mActivityRule.activity.getString(R.string.menu_tracking)), isDisplayed()))
        tracking?.perform(click())

        val labelStartTracking =
            waitForView(allOf(withText(mActivityRule.activity.getString(R.string.label_start_tracking)), isDisplayed()))
        labelStartTracking?.perform(click())
        var mapbox: MapLibreMap? = null
        val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        for (mockLocation in mockLocations) {
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
        waitForView(allOf(withText(mActivityRule.activity.getString(R.string.ok)), isDisplayed()))

        val dialogText = getAlertDialogMessage()
        Assert.assertTrue(TEST_FAILED_NOT_TRACKING_ENTERED_DIALOG, dialogText.contains(TRACKING_ENTERED))
    }

    private fun getAlertDialogMessage(): String {
        val appCompatTextViewMatcher = withId(android.R.id.message)
        var messageText = ""
        onView(appCompatTextViewMatcher).perform(object : ViewAction {
            override fun getDescription(): String {
                return "get AlertDialog message"
            }

            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
            }

            override fun perform(uiController: UiController?, view: View?) {
                val textView = view as TextView?
                messageText = textView?.text.toString()
            }
        })
        return messageText
    }
}
