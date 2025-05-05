package com.aws.amazonlocation.ui.main

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.MAP_1
import com.aws.amazonlocation.MAP_2
import com.aws.amazonlocation.MAP_3
import com.aws.amazonlocation.MAP_4
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
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
class ExploreFragmentChangeStyleTest : BaseTestMainActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        with(preferenceManager) {
            setValue(IS_APP_FIRST_TIME_OPENED, true)
            removeValue(KEY_MAP_NAME)
            removeValue(KEY_MAP_STYLE_NAME)
        }
        super.before()
    }

    @Test
    fun testMapStyleChange() {
        try {
            checkLocationPermission()
            goToMapStyles()

            val mapStyles = listOf(MAP_1, MAP_2, MAP_3, MAP_4)
            mapStyles.forEach { mapContentDescription ->
                waitForView(
                    allOf(withContentDescription(mapContentDescription), isDisplayed())
                )?.perform(click())
            }
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun goToMapStyles() {
        waitForView(allOf(withId(R.id.card_map), isDisplayed()))?.perform(click())
        swipeUp()
    }

    private fun swipeUp(): UiDevice {
        val context = getInstrumentation().targetContext
        val dm = context.resources.displayMetrics
        val uiDevice = UiDevice.getInstance(getInstrumentation())

        val startX = dm.widthPixels / 2
        val startY = dm.heightPixels - 100
        val endY = 100

        uiDevice.swipe(startX, startY, startX, endY, 10)
        return uiDevice
    }
}

