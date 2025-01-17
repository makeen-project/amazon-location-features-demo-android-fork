package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ALLOW
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_GEOCODE
import com.aws.amazonlocation.WHILE_USING_THE_APP
import com.aws.amazonlocation.WHILE_USING_THE_APP_ALLOW
import com.aws.amazonlocation.WHILE_USING_THE_APP_CAPS
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentSearchLocationByGeocodeTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showSearchLocationByGeocodeTest() {
        try {
            val btnContinueToApp = uiDevice.findObject(UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/btn_continue_to_app"))
            if (btnContinueToApp.exists()) {
                btnContinueToApp.click()
            }
            Thread.sleep(DELAY_1000)
            uiDevice.findObject(By.text(WHILE_USING_THE_APP))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_CAPS))?.click()
            uiDevice.findObject(By.text(WHILE_USING_THE_APP_ALLOW))?.click()
            uiDevice.findObject(By.text(ALLOW))?.click()
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val edtSearch =
                onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
            edtSearch.perform(click())
            onView(withId(R.id.edt_search_places)).perform(replaceText(TEST_GEOCODE))
            waitForView(allOf(withId(R.id.rv_search_places_suggestion), isDisplayed(), hasMinimumChildCount(1)))
            val rvSearchPlaceSuggestion =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
            if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
                rvSearchPlaceSuggestion.adapter?.itemCount?.let {
                    Assert.assertTrue(TEST_FAILED_COUNT_NOT_GREATER_THAN_ZERO, it > 0)
                }
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }
        } catch (e: Exception) {
            failTest(80, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
