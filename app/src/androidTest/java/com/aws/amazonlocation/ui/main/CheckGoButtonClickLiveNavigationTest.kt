package com.aws.amazonlocation.ui.main

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckGoButtonClickLiveNavigationTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showGoButtonClickLiveNavigationTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            Thread.sleep(DELAY_2000)

            val sourceEdt = onView(withId(R.id.edt_search_direction)).check(matches(isDisplayed()))
            sourceEdt.perform(click())

            Thread.sleep(DELAY_2000)

            val clMyLocation =
                onView(withId(R.id.cl_my_location)).check(matches(isDisplayed()))
            clMyLocation.perform(click())

            Thread.sleep(DELAY_2000)

            val destinationEdt = onView(withId(R.id.edt_search_dest)).check(matches(isDisplayed()))
            destinationEdt.perform(typeText(TEST_WORD_4))

            Thread.sleep(DELAY_2000)

            waitForView(allOf(withId(R.id.rv_search_places_suggestion_direction), isDisplayed())) {
                Assert.fail(TEST_FAILED)
            }

            Thread.sleep(DELAY_3000)

            onView(withId(R.id.rv_search_places_suggestion_direction)).check(
                matches(
                    hasMinimumChildCount(1),
                ),
            )

            Thread.sleep(DELAY_2000)

            onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            waitForView(
                allOf(
                    withId(R.id.card_drive_go),
                    hasDescendant(
                        withText(GO),
                    ),
                    isDisplayed(),
                ),
            ) {
                Assert.fail(TEST_FAILED)
            }

            val cardDriveGoTest =
                onView(
                    allOf(
                        withId(R.id.card_drive_go),
                        hasDescendant(
                            withText(GO),
                        ),
                    ),
                )
            cardDriveGoTest.perform(click())

            waitForView(withId(R.id.rv_navigation_list)) {
                Assert.fail(TEST_FAILED)
            }

            Thread.sleep(DELAY_2000)

            onView(withId(R.id.btn_exit)).check { view, noViewFoundException ->
                if (noViewFoundException != null) {
                    Assert.fail(TEST_FAILED)
                }
                Assert.assertTrue(TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE, view.isVisible)
            }
        } catch (e: Exception) {
            failTest(145, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
