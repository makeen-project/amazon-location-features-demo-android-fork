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
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY_NULL
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
class TrackingStartTrackingTest : BaseTestMainActivity() {
    @Test
    fun showStartTrackingTest() {
        try {
            checkLocationPermission()

            val tracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                        isDisplayed(),
                    ),
                )
            tracking?.perform(click())

            val labelStartTracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.label_start_tracking)),
                        isDisplayed(),
                    ),
                )
            labelStartTracking?.perform(click())

            val rvTracking =
                waitForView(allOf(withId(R.id.rv_tracking), isDisplayed(), hasMinimumChildCount(1)))
            rvTracking?.check { view, _ ->
                if (view is RecyclerView) {
                    val itemCount = view.adapter?.itemCount ?: 0
                    Assert.assertTrue(TEST_FAILED_NO_TRACKING_HISTORY, itemCount > 0)
                } else {
                    Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY_NULL)
                }
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
