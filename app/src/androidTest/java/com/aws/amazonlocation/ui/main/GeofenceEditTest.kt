package com.aws.amazonlocation.ui.main

import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.getRandom0_01To1_0
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlin.properties.Delegates
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceEditTest : BaseTestMainActivity() {
    private lateinit var geofenceName: String
    private var updatedRadius by Delegates.notNull<Int>()

    @Test
    fun editGeoFenceTest() {
        try {
            checkLocationPermission()

            onView(
                allOf(
                    withId(R.id.card_geofence_map),
                    isDisplayed()
                )
            ).perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_search_loader_geofence_list),
                    isDisplayed()
                )
            )

            createOrGetGeoFence()

            editGeoFence()

            verifyGeoFenceUpdate()
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun createOrGetGeoFence() {
        val rv =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_geofence),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )

        rv?.check { view, _ ->
            if (view is RecyclerView) {
                val holder = view.findViewHolderForAdapterPosition(0)
                geofenceName =
                    holder
                        ?.itemView
                        ?.findViewById<AppCompatTextView>(R.id.tv_geofence_address_type)
                        ?.text
                        .toString()
            }
        }
    }

    private fun editGeoFence() {
        waitForView(
            CoreMatchers.allOf(
                withId(R.id.rv_geofence),
                isDisplayed(),
                hasMinimumChildCount(1)
            )
        )
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName)
                ),
                click()
            )
        )
        val seekbar =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.seekbar_geofence_radius),
                    isDisplayed()
                )
            )

        seekbar?.check { view, _ ->
            if (view is SeekBar) {
                view.progress = ((view.max - view.min) * getRandom0_01To1_0()).toInt()

                updatedRadius = view.progress
            }
        }

        onView(withId(R.id.btn_add_geofence_save)).perform(click())

        waitForView(
            CoreMatchers.allOf(
                withId(R.id.rv_geofence),
                isDisplayed(),
                hasMinimumChildCount(1)
            )
        )
    }

    private fun verifyGeoFenceUpdate() {
        waitForView(
            CoreMatchers.allOf(
                withId(R.id.rv_geofence),
                isDisplayed(),
                hasMinimumChildCount(1)
            )
        )
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(geofenceName))
            )
        )
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName)
                ),
                click()
            )
        )
        val seekbar =
            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.seekbar_geofence_radius),
                    isDisplayed()
                )
            )
        seekbar?.check { view, _ ->
            if (view is SeekBar) {
                if (view.progress != updatedRadius) {
                    Assert.fail(TEST_FAILED)
                }
            }
        }
    }
}
