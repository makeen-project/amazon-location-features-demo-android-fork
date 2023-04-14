package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingSignInTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Before
    fun delay() {
        Thread.sleep(DELAY_10000)
    }

    @Test
    fun showSettingSignInTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

        Thread.sleep(DELAY_1000)
        Espresso.onView(
            AllOf.allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        Thread.sleep(DELAY_1000)
        Espresso.onView(
            AllOf.allOf(
                withId(R.id.cl_aws_cloudformation),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        val appViews = UiScrollable(UiSelector().scrollable(true))
        appViews.scrollForward()
        Thread.sleep(DELAY_1000)
        val signIn =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.sign_in)))
        signIn?.click()
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.log_out))),
            DELAY_20000
        )
        Thread.sleep(DELAY_1000)
        val btnLogout =
            mActivityRule.activity.findViewById<AppCompatButton>(R.id.btn_logout)
        Assert.assertTrue(btnLogout.visibility == View.VISIBLE)
    }
}