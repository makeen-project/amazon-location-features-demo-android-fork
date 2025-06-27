package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
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
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DUE_TO_WORD_MISMATCHED
import com.aws.amazonlocation.TEST_WORD_ARRIVE
import com.aws.amazonlocation.TEST_WORD_AUBURN_SYDNEY
import com.aws.amazonlocation.TEST_WORD_LEAVE_AT
import com.aws.amazonlocation.TEST_WORD_MANLY_BEACH_SYDNEY
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckDepartOptionsTest : BaseTestMainActivity() {

    @Test
    fun showCheckDepartOptionsTest() {
        try {
            checkLocationPermission()

            onView(withId(R.id.card_direction))
                .check(matches(isDisplayed()))
                .perform(click())

            waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
                ?.perform(replaceText(TEST_WORD_AUBURN_SYDNEY))

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
                ?.perform(click(), replaceText(TEST_WORD_MANLY_BEACH_SYDNEY))

            waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

            waitForView(allOf(withId(R.id.card_drive_go), isDisplayed()))

            waitForView(
                allOf(
                    withId(R.id.card_depart_options),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )?.perform(click())

            waitForView(allOf(withId(R.id.cl_arrive_by), isDisplayed()))
                ?.perform(click())

            waitForView(allOf(withId(R.id.card_drive_go), isDisplayed()))

            waitForView(allOf(withId(R.id.tv_depart_options), isDisplayed()))
                ?.check { view, _ ->
                    val textView = view as? AppCompatTextView
                    Assert.assertTrue(
                        TEST_FAILED_DUE_TO_WORD_MISMATCHED,
                        textView?.text?.contains(TEST_WORD_ARRIVE, true) == true
                    )
                }

            waitForView(allOf(withId(R.id.tv_drive_leave_time), isDisplayed()))
                ?.check { view, _ ->
                    val textView = view as? AppCompatTextView
                    Assert.assertTrue(
                        TEST_FAILED_DUE_TO_WORD_MISMATCHED,
                        textView?.text?.contains(TEST_WORD_LEAVE_AT, true) == true
                    )
                }

        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}

