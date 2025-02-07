package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.notificationData
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SimulationStopTrackingHistoryLoggedTest : BaseTestMainActivity() {
    @Test
    fun showStopTrackingHistoryLoggedTest() {
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

            waitForView(
                allOf(
                    withId(R.id.cl_enable_tracking),
                    isDisplayed(),
                ),
            )

            waitForView(
                allOf(
                    withId(R.id.cl_enable_tracking),
                    isDisplayed(),
                ),
            )
            val btnTryTracker =
                waitForView(
                    allOf(
                        withId(R.id.btn_try_tracker),
                        isDisplayed(),
                    ),
                )
            btnTryTracker?.perform(click())

            val labelStartSimulation =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.label_start_simulation)),
                        isDisplayed(),
                    ),
                )
            labelStartSimulation?.perform(click())
            swipeUp()

            val ivBackArrowChangeRoute =
                waitForView(
                    allOf(
                        withId(R.id.iv_back_arrow_change_route),
                        isDisplayed(),
                    ),
                )
            ivBackArrowChangeRoute?.perform(click())

            val labelStopTracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.label_stop_tracking)),
                        isDisplayed(),
                    ),
                )
            labelStopTracking?.perform(click())

            ivBackArrowChangeRoute?.perform(click())

            val spinnerChangeBus =
                waitForView(
                    allOf(
                        withId(R.id.spinnerChangeBus),
                        isDisplayed(),
                    ),
                )
            spinnerChangeBus?.perform(click())

            val data =
                waitForView(
                    allOf(
                        withText(notificationData[2].name),
                        isDisplayed(),
                    ),
                )
            data?.perform(click())

            ivBackArrowChangeRoute?.perform(click())

            val rvTrackingSimulation =
                waitForView(
                    allOf(
                        withId(R.id.rv_tracking_simulation),
                        isDisplayed(),
                        hasMinimumChildCount(1),
                    ),
                )
            rvTrackingSimulation?.check { view, _ ->
                if (view is RecyclerView) {
                    Assert.assertTrue(
                        TEST_FAILED_NO_TRACKING_HISTORY,
                        (view.adapter?.itemCount ?: 0) == 0,
                    )
                } else {
                    Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
                }
            }
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun swipeUp(): UiDevice? {
        // Get the screen dimensions
        val screenHeight =
            getInstrumentation()
                .targetContext.resources.displayMetrics.heightPixels

        // Set the starting point for the swipe (bottom-center of the screen)
        val startX =
            getInstrumentation()
                .targetContext.resources.displayMetrics.widthPixels / 2f
        val startY = screenHeight - 100 // Offset from the bottom of the screen

        // Set the ending point for the swipe (top-center of the screen)
        val endY = 100 // Offset from the top of the screen

        // Perform the swipe action
        val uiDevice = UiDevice.getInstance(getInstrumentation())
        uiDevice.swipe(startX.toInt(), startY, startX.toInt(), endY, 10)
        return uiDevice
    }
}
