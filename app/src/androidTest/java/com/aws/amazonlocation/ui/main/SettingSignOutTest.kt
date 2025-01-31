package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingSignOutTest : BaseTestMainActivity() {

    @Test
    fun showSettingSignInTest() {
        checkLocationPermission()
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

        Espresso.onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        ).perform(click())

        Espresso.onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed(),
            ),
        ).perform(click())
        val isTablet = mActivityRule.activity.resources.getBoolean(R.bool.is_tablet)

        if (!isTablet) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.scrollForward()
        }
        waitForView(
            allOf(
                withText("Canada (Central) ca-central-1"),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.log_out)),
                isDisplayed(),
            ),
        )?.perform(click())

        val btnLogOut =
            Espresso.onView(withId(android.R.id.button1)).check(ViewAssertions.matches(isDisplayed()))
        btnLogOut.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.sign_in)),
                isDisplayed(),
            ),
        )
    }
}
