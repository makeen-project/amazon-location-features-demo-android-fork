package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatTextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_LANGUAGE
import com.aws.amazonlocation.TEST_WORD_LANGUAGE_AR
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
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapLanguageTest : BaseTestMainActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext()).apply {
            setValue(IS_APP_FIRST_TIME_OPENED, true)
            removeValue(KEY_MAP_NAME)
            removeValue(KEY_MAP_STYLE_NAME)
        }
        super.before()
    }

    @Test
    fun testMapLanguageChange() {
        try {
            checkLocationPermission()
            goToMapStyles()

            onView(withId(R.id.cl_map_language)).perform(click())

            waitForView(allOf(withText(TEST_WORD_LANGUAGE_AR), isDisplayed()))
                ?.perform(click())

            waitForView(allOf(withId(R.id.tv_map_language_description), isDisplayed()))
                ?.check { view, _ ->
                    val text = (view as? AppCompatTextView)?.text?.toString().orEmpty()
                    Assert.assertTrue(TEST_FAILED_LANGUAGE, text.contains(TEST_WORD_LANGUAGE_AR))
                }

        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun goToMapStyles() {
        waitForView(allOf(withId(R.id.card_map), isDisplayed()))?.perform(click())
        waitForView(allOf(withId(R.id.cl_map_language), isDisplayed()))
        swipeUp()
    }

    private fun swipeUp(): UiDevice {
        val context = getInstrumentation().targetContext
        val metrics = context.resources.displayMetrics

        val startX = metrics.widthPixels / 2
        val startY = metrics.heightPixels - 100
        val endY = 100

        return UiDevice.getInstance(getInstrumentation()).apply {
            swipe(startX, startY, startX, endY, 10)
        }
    }
}

