package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NOT_EQUAL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_RIO_TINTO
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
class SearchResultComparisonTest : BaseTestMainActivity() {

    @Test
    fun showSearchResultComparisonTest() {
        try {
            checkLocationPermission()

            val listMainSearch = getSearchResults(
                editTextId = R.id.edt_search_places,
                recyclerViewId = R.id.rv_search_places_suggestion
            )

            pressBack()

            // Go to Navigate tab
            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.menu_navigate)),
                    isDisplayed()
                )
            )

            waitForView(allOf(withId(R.id.card_direction), isDisplayed()))?.perform(click())

            val listDirectionSearch = getSearchResults(
                editTextId = R.id.edt_search_direction,
                recyclerViewId = R.id.rv_search_places_suggestion_direction
            )

            Assert.assertTrue(TEST_FAILED_NOT_EQUAL, listMainSearch == listDirectionSearch)

        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun getSearchResults(
        editTextId: Int,
        recyclerViewId: Int
    ): List<String> {
        val results = mutableListOf<String>()

        // Click and type the test word
        onView(withId(editTextId)).check(matches(isDisplayed())).perform(click())
        onView(withId(editTextId)).perform(replaceText(TEST_WORD_RIO_TINTO))

        val rv = waitForView(
            allOf(
                withId(recyclerViewId),
                isDisplayed(),
                hasMinimumChildCount(1)
            )
        )

        val itemCount = getRecyclerViewItemCount(rv)
        if (itemCount <= 0) Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)

        for (i in 0 until itemCount) {
            rv?.perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i),
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    i,
                    getTextFromItemAction(results)
                )
            )
        }

        return results
    }

    private fun getRecyclerViewItemCount(rv: ViewInteraction?): Int {
        var itemCount = 0
        rv?.check { view, _ ->
            itemCount = (view as? RecyclerView)?.adapter?.itemCount ?: 0
        }
        return itemCount
    }

    private fun getTextFromItemAction(resultList: MutableList<String>): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(View::class.java)

            override fun getDescription(): String = "Extract place name text from RecyclerView item"

            override fun perform(uiController: UiController?, view: View) {
                val textView = view.findViewById<AppCompatTextView>(R.id.tv_place_name)
                resultList.add(textView?.text?.toString() ?: "")
            }
        }
    }
}

