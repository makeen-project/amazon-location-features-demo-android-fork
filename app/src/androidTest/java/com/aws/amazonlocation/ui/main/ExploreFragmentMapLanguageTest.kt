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
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapLanguageTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

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
    fun testMapLanguageChangeTest() {
        try {
            Thread.sleep(DELAY_2000)
            val btnContinueToApp = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
                Thread.sleep(DELAY_2000)
            }
            uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
            uiDevice.findObject(By.text(ALLOW))?.click()
            Thread.sleep(DELAY_2000)
            enableGPS(ApplicationProvider.getApplicationContext())
            Thread.sleep(DELAY_2000)
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            Thread.sleep(DELAY_2000)

            goToMapStyles()

            val clMapLanguage =
                onView(withId(R.id.cl_map_language)).check(matches(isDisplayed()))
            clMapLanguage.perform(click())

            Thread.sleep(DELAY_2000)

            val language =
                waitForView(allOf(withText(TEST_WORD_LANGUAGE_AR), isDisplayed()))
            language?.perform(click())

            Thread.sleep(DELAY_2000)

            val description = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/tv_map_language_description"))
            Assert.assertTrue(TEST_FAILED_LANGUAGE, description.text.contains(TEST_WORD_LANGUAGE_AR))
        } catch (e: Exception) {
            failTest(147, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToMapStyles() {
        val cardMap = waitForView(allOf(withId(R.id.card_map), isDisplayed()))
        cardMap?.perform(click())

        Thread.sleep(DELAY_2000)
        swipeUp()
        Thread.sleep(DELAY_2000)
    }

    private fun swipeUp(): UiDevice? {
        // Get the screen dimensions
        val screenHeight = getInstrumentation().targetContext.resources.displayMetrics.heightPixels

        // Set the starting point for the swipe (bottom-center of the screen)
        val startX = getInstrumentation().targetContext.resources.displayMetrics.widthPixels / 2f
        val startY = screenHeight - 100 // Offset from the bottom of the screen

        // Set the ending point for the swipe (top-center of the screen)
        val endY = 100 // Offset from the top of the screen

        // Perform the swipe action
        val uiDevice = UiDevice.getInstance(getInstrumentation())
        uiDevice.swipe(startX.toInt(), startY, startX.toInt(), endY, 10)
        return uiDevice
    }
}
