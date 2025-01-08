package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
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
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DUE_TO_WORD_MISMATCHED
import com.aws.amazonlocation.TEST_WORD_ARRIVE
import com.aws.amazonlocation.TEST_WORD_AUBURN_SYDNEY
import com.aws.amazonlocation.TEST_WORD_LEAVE
import com.aws.amazonlocation.TEST_WORD_MANLY_BEACH_SYDNEY
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_ALLOW
import com.aws.amazonlocation.WHILE_USING_THE_APP_CAPS
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ChangeMultipleDepartOptionsTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun changeMultipleDepartOptionsTest() {
        try {
            checkLocationPermissionAndMapLoad()

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            val sourceEdt = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(replaceText(TEST_WORD_AUBURN_SYDNEY))

            val suggestionListRv = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
            suggestionListRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            val destinationEdt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed()
                )
            )
            destinationEdt?.perform(
                click(),
                replaceText(TEST_WORD_MANLY_BEACH_SYDNEY)
            )

            val suggestionListDestRv = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )
            suggestionListDestRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            val cardDepartOptions = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_depart_options),
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )
            cardDepartOptions?.perform(click())

            val clArriveBy = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_arrive_by),
                    isDisplayed()
                )
            )
            clArriveBy?.perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_drive_loader),
                    isDisplayed()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            var tvDepartOptions =
                mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_depart_options)
            Assert.assertTrue(TEST_FAILED_DUE_TO_WORD_MISMATCHED, tvDepartOptions.text.contains(TEST_WORD_ARRIVE, true))

            val clLeaveAt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_leave_at),
                    isDisplayed()
                )
            )
            clLeaveAt?.perform(click())

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.cl_drive_loader),
                    isDisplayed()
                )
            )

            waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed()
                )
            )

            tvDepartOptions =
                mActivityRule.activity.findViewById(R.id.tv_depart_options)
            Assert.assertTrue(TEST_FAILED_DUE_TO_WORD_MISMATCHED, tvDepartOptions.text.contains(TEST_WORD_LEAVE, true))
        } catch (e: Exception) {
            failTest(221, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun checkLocationPermissionAndMapLoad() {
        val btnContinueToApp =
            uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
        if (btnContinueToApp.exists()) {
            btnContinueToApp.click()
        }
        uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
        uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
        uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
        uiDevice.findObject(By.text(ALLOW))?.click()
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
    }
}
