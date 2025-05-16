package com.aws.amazonlocation.ui.main

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_INVALID_IDENTITY_POOL_ID
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.scrollForView
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingAwsConnectTest : BaseTestMainActivity() {
    @Test
    fun showAwsConnectTest() {
        try {
            checkLocationPermission()

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                    isDisplayed()
                )
            )?.perform(click())

            waitForView(
                allOf(
                    withText(mActivityRule.activity.getString(R.string.label_enable_tracking)),
                    isDisplayed()
                )
            )

            waitForView(
                allOf(
                    withId(R.id.btn_enable_tracking),
                    isDisplayed()
                )
            )?.perform(click())

            waitForView(allOf(withId(R.id.edt_identity_pool_id), isDisplayed()))
            val appViews = UiScrollable(UiSelector().scrollable(true))

            val edtIdentityPoolId =
                scrollForView(allOf(withId(R.id.edt_identity_pool_id), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtIdentityPoolId?.perform(replaceText(BuildConfig.IDENTITY_POOL_ID))

            val edtUserDomain =
                scrollForView(allOf(withId(R.id.edt_user_domain), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtUserDomain?.perform(replaceText(BuildConfig.USER_DOMAIN))

            val edtUserPoolClientId =
                scrollForView(
                    allOf(
                        withId(R.id.edt_user_pool_client_id),
                        isCompletelyDisplayed()
                    )
                ) {
                    appViews.scrollForward(2)
                }
            edtUserPoolClientId?.perform(replaceText(BuildConfig.USER_POOL_CLIENT_ID))

            val edtUserPoolId =
                scrollForView(allOf(withId(R.id.edt_user_pool_id), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtUserPoolId?.perform(replaceText(BuildConfig.USER_POOL_ID))

            val edtWebSocketUrl =
                scrollForView(allOf(withId(R.id.edt_web_socket_url), isCompletelyDisplayed())) {
                    appViews.scrollForward(2)
                }
            edtWebSocketUrl?.perform(replaceText(BuildConfig.WEB_SOCKET_URL))

            val btnConnect =
                onView(withId(R.id.btn_connect)).check(ViewAssertions.matches(isDisplayed()))
            btnConnect.perform(click())

            waitForView(allOf(withId(R.id.tv_sign_in_required), isDisplayed()))
            val targetContext: Context = getInstrumentation().targetContext.applicationContext
            val pm = PreferenceManager(targetContext)
            val mPoolId = pm.getValue(KEY_POOL_ID, "")
            Assert.assertTrue(
                TEST_FAILED_INVALID_IDENTITY_POOL_ID,
                mPoolId == BuildConfig.IDENTITY_POOL_ID
            )
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
