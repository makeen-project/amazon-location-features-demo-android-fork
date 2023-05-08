package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
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
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_CARD_DRIVE_GO
import com.aws.amazonlocation.TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO
import com.aws.amazonlocation.TEST_FAILED_LIST
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
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
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showLiveNavigationTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)
        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        onView(withId(R.id.edt_search_places)).perform(typeText(TEST_WORD_4))
        uiDevice.wait(
            Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_search_places_suggestion")),
            DELAY_10000
        )
        val rvSearchPlaceSuggestion =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
        rvSearchPlaceSuggestion.adapter?.itemCount?.let {
            if (it > 0) {
                Thread.sleep(DELAY_2000)
                onView(withId(R.id.rv_search_places_suggestion)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click()
                    )
                )
                Thread.sleep(DELAY_2000)
                uiDevice.wait(
                    Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/tv_direction_time")),
                    DELAY_5000
                )

                val btnDirection =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_direction)
                mActivityRule.activity.runOnUiThread {
                    btnDirection.performClick()
                }
                Thread.sleep(DELAY_2000)
                val tvGetLocation1 =
                    mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_get_location)
                if (tvGetLocation1.visibility == View.VISIBLE) {
                    uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_my_location)))
                        ?.click()
                }
                uiDevice.wait(
                    Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/card_drive_go")),
                    DELAY_5000
                )

                val cardDriveGo =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_drive_go)
                if (cardDriveGo.visibility == View.VISIBLE) {
                    mActivityRule.activity.runOnUiThread {
                        cardDriveGo.performClick()
                    }

                    uiDevice.wait(
                        Until.hasObject(By.res(BuildConfig.APPLICATION_ID + ":id/rv_navigation_list")),
                        DELAY_5000
                    )

                    val rvNavigationList =
                        mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_navigation_list)
                    Thread.sleep(DELAY_2000)
                    if (rvNavigationList.visibility == View.VISIBLE) {
                        mActivityRule.activity.runOnUiThread {
                            val itemCount = rvNavigationList.adapter?.itemCount ?: 0
                            Assert.assertTrue(TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO, itemCount > 0)
                        }
                    } else {
                        Assert.fail(TEST_FAILED_LIST)
                    }
                } else {
                    Assert.fail(TEST_FAILED_CARD_DRIVE_GO)
                }
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        }
    }
}
