package com.aws.amazonlocation.ui.main

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
import com.aws.amazonlocation.actions.clickOnViewChild
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceDeleteTest : BaseTestMainActivity() {
    private lateinit var geofenceName: String

    @Test
    fun deleteGeoFenceTest() {
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

            createOrGetGeoFence()

            deleteGeoFence()
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
                view.findViewHolderForAdapterPosition(0)?.let {
                    val holder = it
                    geofenceName =
                        holder.itemView.findViewById<AppCompatTextView>(R.id.tv_geofence_address_type).text.toString()
                }
            }
        }
    }

    private fun deleteGeoFence() {
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName),
                ),
                clickOnViewChild(R.id.iv_delete_geofence),
            ),
        )
        onView(withText(mActivityRule.activity.getString(R.string.ok))).perform(click())

        waitForView(
            CoreMatchers.allOf(
                withText(mActivityRule.activity.getString(R.string.add_geofence)),
                isDisplayed()
            )
        )
    }
}
