package com.aws.amazonlocation.ui.main

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_CONNECT_TO_AWS_FROM_SETTINGS
import com.aws.amazonlocation.actions.nestedScrollTo
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.IS_APP_FIRST_TIME_OPENED
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ConnectToAWSTest : BaseTestMainActivity() {
    @Throws(java.lang.Exception::class)
    override fun before() {
        val targetContext: Context = getInstrumentation().targetContext.applicationContext
        val pm = PreferenceManager(targetContext)
        pm.setDefaultConfig()
        pm.setValue(IS_APP_FIRST_TIME_OPENED, true)
        pm.setValue(KEY_RE_START_APP, false)

        super.before()
    }

    @Test
    fun canConnectToAWSFromSettings() {
        try {
            checkLocationPermission()
            val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)

            onView(
                allOf(
                    withText(settingTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed()
                )
            ).perform(click())

            onView(
                allOf(
                    withId(R.id.cl_aws_cloudformation),
                    isDisplayed()
                )
            ).perform(click())

            onView(withId(R.id.edt_identity_pool_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.IDENTITY_POOL_ID),
                closeSoftKeyboard()
            )
            onView(withId(R.id.edt_user_domain)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_DOMAIN),
                closeSoftKeyboard()
            )
            onView(withId(R.id.edt_user_pool_client_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_POOL_CLIENT_ID),
                closeSoftKeyboard()
            )
            onView(withId(R.id.edt_user_pool_id)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.USER_POOL_ID),
                closeSoftKeyboard()
            )
            onView(withId(R.id.edt_web_socket_url)).perform(
                nestedScrollTo(),
                typeText(BuildConfig.WEB_SOCKET_URL),
                closeSoftKeyboard()
            )
            val btnConnect =
                onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
            btnConnect.perform(click())
            waitForView(
                CoreMatchers.allOf(
                    withText(mActivityRule.activity.getString(R.string.sign_in)),
                    isDisplayed()
                )
            )
        } catch (e: Exception) {
            Assert.fail(TEST_FAILED_CONNECT_TO_AWS_FROM_SETTINGS)
        }
    }
}
