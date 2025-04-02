package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NOT_TRACKING_EXIT_DIALOG
import com.aws.amazonlocation.TRACKING_ENTERED
import com.aws.amazonlocation.TRACKING_EXITED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingGeofenceExitTest : BaseTestMainActivity() {
    @Test
    fun showStartTrackingTest() {
        try {
            checkLocationPermission()

            val tracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                        isDisplayed()
                    )
                )
            tracking?.perform(click())

            val labelStartTracking =
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.label_start_tracking)),
                        isDisplayed()
                    )
                )
            labelStartTracking?.perform(click())

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.ok)),
                    isDisplayed()
                )
            )

            var dialogText = getAlertDialogMessage()
            if (dialogText.contains(TRACKING_ENTERED)) {
                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.ok)),
                        isDisplayed()
                    )
                )?.perform(
                    click()
                )

                waitForView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.ok)),
                        isDisplayed()
                    )
                )
                dialogText = getAlertDialogMessage()
                Assert.assertTrue(
                    TEST_FAILED_NOT_TRACKING_EXIT_DIALOG,
                    dialogText.contains(TRACKING_EXITED)
                )
            } else if (dialogText.contains(TRACKING_EXITED)) {
                Assert.assertTrue(
                    TEST_FAILED_NOT_TRACKING_EXIT_DIALOG,
                    dialogText.contains(TRACKING_EXITED)
                )
            } else {
                Assert.fail(TEST_FAILED)
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun getAlertDialogMessage(): String {
        val appCompatTextViewMatcher = withId(android.R.id.message)
        var messageText = ""
        onView(appCompatTextViewMatcher).perform(
            object : ViewAction {
                override fun getDescription(): String = "get AlertDialog message"

                override fun getConstraints(): Matcher<View> = allOf(
                    isDisplayed(),
                    isAssignableFrom(TextView::class.java)
                )

                override fun perform(
                    uiController: UiController?,
                    view: View?
                ) {
                    val textView = view as TextView?
                    messageText = textView?.text.toString()
                }
            }
        )
        return messageText
    }
}
