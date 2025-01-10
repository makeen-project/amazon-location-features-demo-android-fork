package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
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
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_LOGOUT_BUTTON_NOT_VISIBLE
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingSignInTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())


    @Test
    fun showSettingSignInTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

        onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        ).perform(ViewActions.click())

        onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed(),
            ),
        ).perform(ViewActions.click())

        val isTablet = mActivityRule.activity.resources.getBoolean(R.bool.is_tablet)

        if (!isTablet) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.scrollForward()
        }
        val region =
            uiDevice.findObject(By.text("Canada (Central) ca-central-1"))
        region?.click()

        val signIn =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.sign_in)))
        signIn?.click()

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

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.log_out))),
            DELAY_20000,
        )
        val btnLogout =
            mActivityRule.activity.findViewById<AppCompatButton>(R.id.btn_logout)
        Assert.assertTrue(TEST_FAILED_LOGOUT_BUTTON_NOT_VISIBLE, btnLogout.isVisible)
    }
}
