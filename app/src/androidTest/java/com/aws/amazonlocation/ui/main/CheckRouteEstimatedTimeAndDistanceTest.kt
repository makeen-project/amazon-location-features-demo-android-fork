package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DISTANCE_OR_TIME_EMPTY
import com.aws.amazonlocation.TEST_WORD_10
import com.aws.amazonlocation.TEST_WORD_9
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteEstimatedTimeAndDistanceTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showRouteEstimatedTimeAndDistanceTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            Thread.sleep(DELAY_2000)

            val sourceEdt = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click(), replaceText(TEST_WORD_9))

            Thread.sleep(DELAY_2000)

            val suggestionListRvSrc = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRvSrc?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            Thread.sleep(DELAY_2000)

            val destinationEdt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed(),
                ),
            )
            destinationEdt?.perform(click(), replaceText(TEST_WORD_10))

            Thread.sleep(DELAY_2000)

            val suggestionListRvDest = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRvDest?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            val btnCarGo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )

            val btnWalkGo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_walk_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )

            val btnTruckGo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_truck_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )

            Thread.sleep(DELAY_10000)

            val tvDriveDistance =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_drive_distance)
            val tvDriveMinute =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_drive_minute)
            val tvWalkDistance =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_walk_distance)
            val tvWalkMinute =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_walk_minute)
            val tvTruckDistance =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_truck_distance)
            val tvTruckMinute =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_truck_minute)

            Assert.assertTrue(
                TEST_FAILED_DISTANCE_OR_TIME_EMPTY,
                tvDriveDistance.text.toString().isNotEmpty() && tvDriveMinute.text.toString().isNotEmpty() &&
                    tvWalkDistance.text.toString().isNotEmpty() && tvWalkMinute.text.toString().isNotEmpty() &&
                    tvTruckDistance.text.toString().isNotEmpty() && tvTruckMinute.text.toString().isNotEmpty(),
            )
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
