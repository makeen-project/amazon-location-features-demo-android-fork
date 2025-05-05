package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_COUNTRY
import com.aws.amazonlocation.TEST_WORD_RUS
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsMapPoliticalViewTest : BaseTestMainActivity() {
    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    @Test
    fun testSettingsMapPoliticalViewTest() {
        checkLocationPermission()

        goToMapStyles()

        onView(withId(R.id.cl_political_view)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.et_search_country)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.et_search_country)).perform(replaceText(TEST_WORD_RUS))

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.description_rus)),
                isDisplayed()
            )
        )

        waitForView(withId(R.id.rb_country))?.check(matches(isDisplayed()))?.perform(click())

        val tvPoliticalDescription =
            waitForView(
                allOf(
                    withId(R.id.tv_political_description),
                    isDisplayed()
                )
            )

        tvPoliticalDescription?.check { view, _ ->
            if (view is AppCompatTextView) {
                Assert.assertTrue(TEST_FAILED_COUNTRY, view.text.contains(TEST_WORD_RUS))
            }
        }
    }

    private fun goToMapStyles() {
        waitForView(
            AllOf.allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(
            AllOf.allOf(
                withId(R.id.cl_map_style),
                isDisplayed()
            )
        )?.perform(click())
    }
}
