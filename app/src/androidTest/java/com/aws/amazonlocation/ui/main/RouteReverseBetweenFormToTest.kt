package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class RouteReverseBetweenFormToTest : BaseTestMainActivity() {

    @Test
    fun showRouteReverseBetweenFormToTest() {
        try {
            checkLocationPermission()

            onView(withId(R.id.card_direction))
                .check(matches(isDisplayed()))
                .perform(click())

            waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
                ?.perform(click())

            waitForView(allOf(withText(R.string.label_my_location), isDisplayed()))
                ?.perform(click())

            waitForView(allOf(withId(R.id.edt_search_dest), isDisplayed()))
                ?.perform(click(), replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))

            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

            waitForView(
                allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(withText(GO)),
                    isDisplayed()
                )
            )

            val originText = getEditTextText(R.id.edt_search_direction)
            val destinationText = getEditTextText(R.id.edt_search_dest)

            waitForView(allOf(withId(R.id.iv_swap_location), isDisplayed()))
                ?.perform(click())

            val swappedOriginText = getEditTextText(R.id.edt_search_direction)
            val swappedDestinationText = getEditTextText(R.id.edt_search_dest)

            Assert.assertTrue(
                TEST_FAILED_INVALID_ORIGIN_OR_DESTINATION_TEXT,
                originText == swappedDestinationText && destinationText == swappedOriginText
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun getEditTextText(id: Int): String {
        val result = StringBuilder()
        onView(withId(id)).check { view, _ ->
            if (view is TextInputEditText) {
                result.append(view.text?.toString()?.trim())
            }
        }
        return result.toString()
    }
}
