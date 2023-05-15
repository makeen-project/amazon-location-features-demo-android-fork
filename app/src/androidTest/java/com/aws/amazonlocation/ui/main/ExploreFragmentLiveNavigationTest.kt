package com.aws.amazonlocation.ui.main

import android.content.Context
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
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_LIST
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitUntil
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentLiveNavigationTest : BaseTest() {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showLiveNavigationTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)
            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(typeText(TEST_WORD_4))
//            uiDevice.wait(
//                Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_search_places_suggestion")),
//                DELAY_20000,
//            )

            var rvRecyclerView: UiObject2?
            waitUntil(DELAY_5000, 25) {
                rvRecyclerView = uiDevice.findObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_search_places_suggestion"))
                rvRecyclerView != null
            }

            onView(withId(R.id.rv_search_places_suggestion)).check(matches(isDisplayed()))
            onView(withId(R.id.rv_search_places_suggestion)).check(
                matches(
                    hasMinimumChildCount(1),
                ),
            )

            Thread.sleep(DELAY_3000)
            onView(withId(R.id.rv_search_places_suggestion)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )
            Thread.sleep(DELAY_5000)

            getInstrumentation().waitForIdleSync()
            getInstrumentation().runOnMainSync {
                val directionButton = mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_direction)
                directionButton.performClick()
            }
            Thread.sleep(DELAY_5000)

            onView(withId(R.id.tv_get_location)).check(matches(isDisplayed()))
            onView(withText(ApplicationProvider.getApplicationContext<Context>().getString(R.string.label_my_location))).perform(
                click(),
            )

            uiDevice.wait(
                Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/card_drive_go")),
                DELAY_20000,
            )

            var cardDriveGo: UiObject2?

            waitUntil(DELAY_5000, 25) {
                cardDriveGo = uiDevice.findObject(By.res(BuildConfig.APPLICATION_ID + ":id/card_drive_go"))
                cardDriveGo != null
            }

            onView(
                allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed(),
                ),
            ).perform(click())

            uiDevice.wait(
                Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_navigation_list")),
                DELAY_20000,
            )

            Thread.sleep(DELAY_5000)

            onView(allOf(withId(R.id.rv_navigation_list), isDisplayed())).check(
                matches(
                    hasMinimumChildCount(1),
                ),
            )
        } catch (e: Exception) {
            failTest(136, e)
            Assert.fail(TEST_FAILED_LIST)
        }
    }
}
