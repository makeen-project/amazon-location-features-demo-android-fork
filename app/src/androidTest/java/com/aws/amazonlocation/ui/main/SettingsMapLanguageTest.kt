package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsMapLanguageTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        super.before()
    }

    @Test
    fun testSettingsMapPoliticalViewTest() {
        Thread.sleep(DELAY_2000)

        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        goToMapStyles()

        val clPoliticalView =
            onView(withId(R.id.cl_map_language)).check(matches(isDisplayed()))
        clPoliticalView.perform(click())

        Thread.sleep(DELAY_2000)

        val language =
            waitForView(allOf(withText(TEST_WORD_LANGUAGE_BO), isDisplayed()))
        language?.perform(click())

        Thread.sleep(DELAY_2000)

        val description = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/tv_map_language_description"))
        Assert.assertTrue(TEST_FAILED_LANGUAGE, description.text.contains(TEST_WORD_LANGUAGE_BO))
    }

    private fun goToMapStyles() {
        waitForView(
            AllOf.allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)

        waitForView(
            AllOf.allOf(
                withId(R.id.cl_map_style),
                isDisplayed()
            )
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)
    }
}