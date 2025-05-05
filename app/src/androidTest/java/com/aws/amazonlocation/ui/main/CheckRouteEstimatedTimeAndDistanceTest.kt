package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_CLOVERDALE_PERTH
import com.aws.amazonlocation.TEST_WORD_KEWDALE_PERTH
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteEstimatedTimeAndDistanceTest : BaseTestMainActivity() {

    @Test
    fun showRouteEstimatedTimeAndDistanceTest() {
        try {
            checkLocationPermission()

            onView(withId(R.id.card_direction))
                .check(matches(isDisplayed()))
                .perform(click())

            waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
                ?.perform(click(), replaceText(TEST_WORD_CLOVERDALE_PERTH))

            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

            waitForView(allOf(withId(R.id.edt_search_dest), isDisplayed()))
                ?.perform(click(), replaceText(TEST_WORD_KEWDALE_PERTH))

            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

            // Wait for all GO cards to be visible
            listOf(
                R.id.card_drive_go,
                R.id.card_walk_go,
                R.id.card_truck_go
            ).forEach { id ->
                waitForView(allOf(withId(id), withEffectiveVisibility(Visibility.VISIBLE)))
            }

            // Validate non-empty values for distance and minute labels
            listOf(
                R.id.tv_drive_distance,
                R.id.tv_drive_minute,
                R.id.tv_walk_distance,
                R.id.tv_walk_minute,
                R.id.tv_truck_distance,
                R.id.tv_truck_minute
            ).forEach { id ->
                onView(withId(id)).check(matches(not(withText(""))))
            }

        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}

