package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.EXPLORE
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_CARD
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_FAILED_SEARCH_SHEET
import com.aws.amazonlocation.TEST_WORD_1
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.ui.main.explore.SearchPlacesAdapter
import com.aws.amazonlocation.ui.main.explore.SearchPlacesSuggestionAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchResultComparisonTest {

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
    fun showSearchResultComparisonTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(typeText(TEST_WORD_1))
            uiDevice.wait(
                Until.hasObject(By.res("com.aws.amazonlocation:id/rv_search_places_suggestion")),
                DELAY_10000
            )
            val rvSearchPlaceSuggestion =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
            if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
                val listDataSearch = arrayListOf<String>()
                rvSearchPlaceSuggestion.adapter?.itemCount?.let {
                    if (it >= 0) {
                        for (i in 0 until it) {
                            onView(withId(R.id.rv_search_places_suggestion)).perform(
                                RecyclerViewActions.scrollToPosition<SearchPlacesSuggestionAdapter.SearchPlaceVH>(
                                    i
                                ),
                                RecyclerViewActions.actionOnItemAtPosition<SearchPlacesSuggestionAdapter.SearchPlaceVH>(
                                    i,
                                    object : ViewAction {
                                        override fun getConstraints(): Matcher<View> {
                                            return isAssignableFrom(TextView::class.java)
                                        }

                                        override fun getDescription(): String {
                                            return "Get data from RecyclerView item"
                                        }

                                        override fun perform(
                                            uiController: UiController?,
                                            view: View
                                        ) {
                                            val data =
                                                view.findViewById<AppCompatTextView>(R.id.tv_place_name)
                                            listDataSearch.add(data.text.toString())
                                        }
                                    }
                                )
                            )
                        }

                        val clSearchSheet =
                            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_search)
                        if (clSearchSheet.visibility == View.VISIBLE) {
                            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                                BottomSheetBehavior.from(clSearchSheet)
                            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_COLLAPSED
                            uiDevice.wait(Until.hasObject(By.text(EXPLORE)), DELAY_5000)
                            val cardDirection =
                                mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_direction)
                            if (cardDirection.visibility == View.VISIBLE) {
                                val cardDirectionTest =
                                    onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
                                cardDirectionTest.perform(click())
                                uiDevice.wait(
                                    Until.hasObject(By.res("com.aws.amazonlocation:id/edt_search_direction")),
                                    DELAY_5000
                                )
                                val edtSearchDirection =
                                    onView(withId(R.id.edt_search_direction)).check(
                                        matches(
                                            isDisplayed()
                                        )
                                    )
                                edtSearchDirection.perform(click())
                                onView(withId(R.id.edt_search_direction)).perform(
                                    typeText(
                                        TEST_WORD_1
                                    )
                                )

                                uiDevice.wait(
                                    Until.hasObject(By.res("com.aws.amazonlocation:id/rv_search_places_suggestion_direction")),
                                    DELAY_5000
                                )
                                val rvSearchPlaceDirection =
                                    mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion_direction)
                                Thread.sleep(DELAY_2000)
                                if (rvSearchPlaceDirection.adapter?.itemCount != null) {
                                    val listInsideDataSearch = arrayListOf<String>()
                                    rvSearchPlaceDirection.adapter?.itemCount?.let { it1 ->
                                        if (it1 >= 0) {
                                            for (i in 0 until it1) {
                                                onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                                    RecyclerViewActions.scrollToPosition<SearchPlacesAdapter.SearchPlaceVH>(
                                                        i
                                                    ),
                                                    RecyclerViewActions.actionOnItemAtPosition<SearchPlacesAdapter.SearchPlaceVH>(
                                                        i,
                                                        object : ViewAction {
                                                            override fun getConstraints(): Matcher<View> {
                                                                return isAssignableFrom(TextView::class.java)
                                                            }

                                                            override fun getDescription(): String {
                                                                return "Get data from RecyclerView item"
                                                            }

                                                            override fun perform(
                                                                uiController: UiController?,
                                                                view: View
                                                            ) {
                                                                val data =
                                                                    view.findViewById<AppCompatTextView>(
                                                                        R.id.tv_place_name
                                                                    )
                                                                listInsideDataSearch.add(data.text.toString())
                                                            }
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Assert.assertTrue(listDataSearch == listInsideDataSearch)
                                }
                            } else {
                                Assert.fail(TEST_FAILED_DIRECTION_CARD)
                            }
                        } else {
                            Assert.fail(TEST_FAILED_SEARCH_SHEET)
                        }
                    } else {
                        Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                    }
                }
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}