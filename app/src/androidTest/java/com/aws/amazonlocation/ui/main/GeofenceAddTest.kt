package com.aws.amazonlocation.ui.main

import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.getRandomGeofenceName
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceAddTest : BaseTestMainActivity() {

    private var geofenceName = getRandomGeofenceName()

    @Test
    fun addGeoFenceTest() {
        try {
            checkLocationPermission()

            onView(
                allOf(
                    withId(R.id.card_geofence_map),
                    isDisplayed(),
                ),
            ).perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_search_loader_geofence_list),
                    isDisplayed()
                )
            )
            if (onView(withId(R.id.cl_empty_geofence_list)).check(matches(isDisplayed())).runCatching { true }.getOrDefault(false)) {
                onView(withId(R.id.btn_add_geofence)).perform(click())
            } else {
                onView(withId(R.id.card_add_geofence)).perform(click())
            }
            waitForView(CoreMatchers.allOf(withId(R.id.edt_add_geofence_search), isDisplayed()))
            onView(withId(R.id.edt_add_geofence_search)).perform(clearText(), typeText(TEST_WORD_SHYAMAL_CROSS_ROAD))


            val rvGeofenceSearchPlaces =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.rv_geofence_search_places_suggestion),
                        isDisplayed(),
                        hasMinimumChildCount(1)
                    )
                )

            var itemCount = 0
            rvGeofenceSearchPlaces?.check { view, _ ->
                if (view is RecyclerView) {
                    itemCount = view.adapter?.itemCount ?: 0
                } else {
                    Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                }
            }
            if (itemCount > 0) {
                onView(withId(R.id.rv_geofence_search_places_suggestion)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click(),
                    ),
                )
            }
            val seekbar =
                waitForView(
                    CoreMatchers.allOf(
                        withId(R.id.seekbar_geofence_radius),
                        isDisplayed()
                    )
                )

            seekbar?.check { view, _ ->
                if (view is SeekBar) {
                    view.progress = 400
                }
            }

            onView(withId(R.id.edt_enter_geofence_name)).perform(typeText(geofenceName))

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.btn_add_geofence_save),
                    isDisplayed()
                )
            )?.perform(click())


            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_geofence),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )

            onView(withId(R.id.rv_geofence)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(geofenceName)),
                ),
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
