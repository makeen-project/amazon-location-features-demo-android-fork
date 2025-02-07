package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingStartTrackingHistoryLoggedTest : BaseTestMainActivity() {
    @Test
    fun showStartTrackingHistoryLoggedTest() {
        try {
            checkLocationPermission()

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

            val rvSearchPlaceSuggestion =
                waitForView(allOf(withId(R.id.rv_tracking), isDisplayed(), hasMinimumChildCount(1)))
            rvSearchPlaceSuggestion?.check { view, _ ->
                if (view is RecyclerView) {
                    Assert.assertTrue(
                        TEST_FAILED_NO_TRACKING_HISTORY,
                        (view.adapter?.itemCount ?: 0) > itemCount,
                    )
                } else {
                    Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
                }
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
