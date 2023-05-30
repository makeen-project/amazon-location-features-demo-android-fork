package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.* // ktlint-disable no-wildcard-imports
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.CountDownLatch

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentGrabChangeStyleTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager
    private val latch = CountDownLatch(1)
    private var mapbox: MapboxMap? = null

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    @Test
    fun testGrabMapStyleChange() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            goToMapStyles()

            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.scrollForward(2)

            waitForView(allOf(withId(R.id.rv_grab), isDisplayed()))?.perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(0, click())
            )
            Thread.sleep(DELAY_2000)
            val labelOk =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.ok)))
            labelOk?.click()
            val mapName =
                preferenceManager.getValue(KEY_MAP_NAME, mActivityRule.activity.getString(R.string.grab))
            Thread.sleep(DELAY_2000)
            Assert.assertTrue(TEST_FAILED_MAP_STYLE_NOT_CHANGED, mapName == MAP_STYLE_GRAB)
        } catch (e: Exception) {
            failTest(202, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun goToMapStyles() {
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)

        waitForView(
            allOf(
                withId(R.id.cl_map_style),
                isDisplayed()
            )
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)
    }

    override fun after() {
        super.after()
        val targetContext = ApplicationProvider.getApplicationContext<Context>()
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
    }
}
