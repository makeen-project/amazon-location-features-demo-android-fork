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

    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext()).apply {
            setValue(IS_APP_FIRST_TIME_OPENED, true)
        }
        super.before()
    }

    @Test
    fun testSettingsMapLanguageChange() {
        checkLocationPermission()
        goToMapStyles()

        waitForView(withId(R.id.cl_map_language))?.perform(click())

        waitForView(withText(TEST_WORD_LANGUAGE_BO))?.perform(click())

        waitForView(withId(R.id.tv_map_language_description))?.check { view, _ ->
            val text = (view as? AppCompatTextView)?.text?.toString().orEmpty()
            Assert.assertTrue(TEST_FAILED_LANGUAGE, text.contains(TEST_WORD_LANGUAGE_BO))
        }
    }

    private fun goToMapStyles() {
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(withId(R.id.cl_map_style))?.perform(click())
    }
}

