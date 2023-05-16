package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_5
import com.aws.amazonlocation.TEST_WORD_6
import com.aws.amazonlocation.TEST_WORD_7
import com.aws.amazonlocation.TEST_WORD_8
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckRouteOptionsTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showCheckRouteOptionsTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            Thread.sleep(DELAY_2000)

            val sourceEdt = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(replaceText(TEST_WORD_5))

            Thread.sleep(DELAY_2000)

            val suggestionListRv = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )
            Thread.sleep(DELAY_2000)

            val destinationEdt = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                    isDisplayed(),
                ),
            )
            destinationEdt?.perform(
                click(),
                replaceText(TEST_WORD_6),
            )

            Thread.sleep(DELAY_2000)

            val suggestionListDestRv = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListDestRv?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            val btnCarGoFirst = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    isDisplayed(),
                ),
            )

            val cardRoutingOption = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_routing_option),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )
            cardRoutingOption?.perform(click())

            val switchAvoidTolls = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.switch_avoid_tools),
                    isDisplayed(),
                ),
            )
            switchAvoidTolls?.perform(click())

            Thread.sleep(DELAY_2000)

            val btnCarGoSecond = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )

            Thread.sleep(DELAY_2000)

            val sourceEdtTwo = waitForView(CoreMatchers.allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdtTwo?.perform(click(), replaceText(TEST_WORD_7))

            Thread.sleep(DELAY_2000)

            val suggestionListRvTwo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListRvTwo?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            Thread.sleep(DELAY_5000)

            val destinationEdtTwo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.edt_search_dest),
                ),
            )

            getInstrumentation().runOnMainSync {
                val destinationEdtTwoIns = mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_dest)
                destinationEdtTwoIns.setText(TEST_WORD_8)
            }

            Thread.sleep(DELAY_2000)

            val suggestionListDestRvTwo = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.rv_search_places_suggestion_direction),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )
            suggestionListDestRvTwo?.perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click(),
                ),
            )

            val btnCarGoThird = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )

            val switchAvoidFerries = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.switch_avoid_ferries),
                    isDisplayed(),
                ),
            )
            switchAvoidFerries?.perform(click())

            Thread.sleep(DELAY_2000)

            val btnCarGoFourth = waitForView(
                CoreMatchers.allOf(
                    withId(R.id.card_drive_go),
                    withEffectiveVisibility(Visibility.VISIBLE),
                ),
            )
        } catch (e: Exception) {
            failTest(221, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
