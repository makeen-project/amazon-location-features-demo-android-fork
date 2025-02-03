package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.GO
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_WORD_SHYAMAL_CROSS_ROAD
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
class CheckGoButtonClickLiveNavigationTest : BaseTestMainActivity() {
    @Test
    fun showGoButtonClickLiveNavigationTest() {
        try {
            checkLocationPermission()

            val cardDirectionTest =
                onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
            cardDirectionTest.perform(click())

            val sourceEdt = waitForView(allOf(withId(R.id.edt_search_direction), isDisplayed()))
            sourceEdt?.perform(click())

            val clMyLocation =
                waitForView(allOf(withText(R.string.label_my_location), isDisplayed()))
            clMyLocation?.perform(click())

            val destinationEdt = waitForView(allOf(withId(R.id.edt_search_dest), isDisplayed()))
            destinationEdt?.perform(click(), replaceText(TEST_WORD_SHYAMAL_CROSS_ROAD))

            val suggestionListRv =
                waitForView(
                    allOf(
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

            val btnCarGo =
                waitForView(
                    allOf(
                        withId(R.id.card_drive_go),
                        hasDescendant(
                            withText(GO),
                        ),
                        isDisplayed(),
                    ),
                )
            btnCarGo?.perform(click())

            Espresso.closeSoftKeyboard()

            waitForView(
                allOf(
                    withId(R.id.rv_navigation_list),
                    isDisplayed(),
                    hasMinimumChildCount(1),
                ),
            )

            // btnExit
            waitForView(allOf(withId(R.id.btn_exit), isDisplayed())) {
                Assert.fail("$TEST_FAILED button exit not visible")
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
