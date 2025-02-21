package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_POOL_ID_NOT_BLANK
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingAWSDisconnectingTest : BaseTestMainActivity() {
    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun delay() {
        val activity: MainActivity = mActivityRule.activity
        preferenceManager = (activity as BaseActivity).mPreferenceManager
    }

    @Test
    fun showSettingAWSDisconnectingTest() {
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

        onView(withId(R.id.ns_cloud_formation)).perform(swipeUp())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.disconnect_aws)),
                isDisplayed()
            )
        )?.perform(click())

        val disconnectAws =
            onView(withId(android.R.id.button1)).check(ViewAssertions.matches(isDisplayed()))
        disconnectAws.perform(click())

        val poolId = preferenceManager.getValue(KEY_POOL_ID, "")
        Assert.assertTrue(TEST_FAILED_POOL_ID_NOT_BLANK, poolId == "")
    }
}
