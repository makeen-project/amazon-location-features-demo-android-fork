package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
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
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingAwsConnectTest {

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

    @Test
    fun showAwsConnectTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_1000)

        val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
        tracking.click()

        Thread.sleep(DELAY_1000)
        uiDevice.wait(
            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/tv_sign_in_required")),
            DELAY_10000
        )
        val appViews = UiScrollable(UiSelector().scrollable(true))
        appViews.scrollForward()
        Thread.sleep(DELAY_1000)
        val edtIdentityPoolId =
            onView(withId(R.id.edt_identity_pool_id)).check(ViewAssertions.matches(isDisplayed()))
        edtIdentityPoolId.perform(click())
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.edt_identity_pool_id))
            ?.perform(click(), replaceText(BuildConfig.IDENTITY_POOL_ID), closeSoftKeyboard())
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.edt_user_domain))
            ?.perform(click(), replaceText(BuildConfig.USER_DOMAIN), closeSoftKeyboard())
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.edt_user_pool_client_id))
            ?.perform(click(), replaceText(BuildConfig.USER_POOL_CLIENT_ID), closeSoftKeyboard())
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.edt_user_pool_id))
            ?.perform(click(), replaceText(BuildConfig.USER_POOL_ID), closeSoftKeyboard())
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.edt_web_socket_url))
            ?.perform(click(), replaceText(BuildConfig.WEB_SOCKET_URL), closeSoftKeyboard())

        Thread.sleep(DELAY_1000)
        val btnConnect =
            onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
        btnConnect.perform(click())
        Thread.sleep(DELAY_5000)
        val targetContext: Context = getInstrumentation().targetContext.applicationContext
        val pm = PreferenceManager(targetContext)
        val mPoolId = pm.getValue(KEY_POOL_ID, "")

        val packageName = targetContext.packageName
        // Clear app from recent apps list
        val am = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.let {
            it.appTasks.forEach { task ->
                if (task.taskInfo.baseActivity?.packageName == packageName) {
                    task.finishAndRemoveTask()
                }
            }
        }
        Assert.assertTrue(mPoolId == BuildConfig.IDENTITY_POOL_ID)
    }
}
