package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_LANGUAGE
import com.aws.amazonlocation.TEST_WORD_LANGUAGE_BO
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
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
class SettingsMapLanguageTest : BaseTestMainActivity() {
    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        super.before()
    }

    @Test
    fun testSettingsMapPoliticalViewTest() {
        checkLocationPermission()

        goToMapStyles()

        waitForView(
            AllOf.allOf(
                withId(R.id.cl_map_language),
                isDisplayed()
            )
        )?.perform(click())

        val language =
            waitForView(allOf(withText(TEST_WORD_LANGUAGE_BO), isDisplayed()))
        language?.perform(click())

        val tvMapLanguageDescription =
            waitForView(
                allOf(
                    withId(R.id.tv_map_language_description),
                    isDisplayed()
                )
            )

        tvMapLanguageDescription?.check { view, _ ->
            if (view is AppCompatTextView) {
                Assert.assertTrue(TEST_FAILED_LANGUAGE, view.text.contains(TEST_WORD_LANGUAGE_BO))
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
