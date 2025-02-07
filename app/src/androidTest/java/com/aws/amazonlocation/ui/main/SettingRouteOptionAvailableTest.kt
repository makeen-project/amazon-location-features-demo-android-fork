package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_ROUTE_OPTION_NOT_VISIBLE
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingRouteOptionAvailableTest : BaseTestMainActivity() {
    @Test
    fun showSettingRouteOptionAvailableTest() {
        try {
            checkLocationPermission()

            Espresso
                .onView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.menu_setting)),
                        isDisplayed(),
                    ),
                ).perform(click())

            Espresso
                .onView(
                    allOf(
                        withText(mActivityRule.activity.getString(R.string.label_default_route_options)),
                        isDisplayed(),
                    ),
                ).perform(click())

            waitForView(allOf(withId(R.id.tv_avoid_ferries), isDisplayed()), onNotFound = {
                Assert.fail(TEST_FAILED_ROUTE_OPTION_NOT_VISIBLE)
            })
            waitForView(allOf(withId(R.id.tv_avoid_tools), isDisplayed()), onNotFound = {
                Assert.fail(TEST_FAILED_ROUTE_OPTION_NOT_VISIBLE)
            })
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
