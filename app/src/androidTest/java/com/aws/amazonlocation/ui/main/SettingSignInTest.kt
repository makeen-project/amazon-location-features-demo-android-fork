package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
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
class SettingSignInTest : BaseTestMainActivity() {
    @Test
    fun showSettingSignInTest() {
        checkLocationPermission()
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

        onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        ).perform(click())

        onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed()
            )
        ).perform(click())

        val isTablet = mActivityRule.activity.resources.getBoolean(R.bool.is_tablet)

        if (!isTablet) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.scrollForward()
        }
        waitForView(
            allOf(
                withText("Canada (Central) ca-central-1"),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.sign_in)),
                isDisplayed()
            )
        )?.perform(click())

        waitForView(allOf(withId(R.id.sign_in_web_view), isDisplayed()))

        onView(withId(R.id.sign_in_web_view))
            .check(matches(isDisplayed()))

        onWebView(withId(R.id.sign_in_web_view))
            .forceJavascriptEnabled()

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "username"))
            .perform(webKeys(BuildConfig.USER_LOGIN_NAME))

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "password"))
            .perform(webKeys(BuildConfig.USER_LOGIN_PASSWORD))

        onWebView(withId(R.id.sign_in_web_view))
            .withElement(findElement(Locator.NAME, "signInSubmitButton"))
            .perform(webClick())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.log_out)),
                isDisplayed()
            )
        )
    }
}
