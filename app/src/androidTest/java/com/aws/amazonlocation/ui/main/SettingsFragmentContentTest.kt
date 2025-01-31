package com.aws.amazonlocation.ui.main

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentContentTest : BaseTestMainActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    @Throws(java.lang.Exception::class)
    override fun before() {
        super.before()
        val activity: MainActivity = mActivityRule.activity
        bottomNavigation = activity.findViewById(R.id.bottom_navigation_main)
    }

    @Test
    fun checkContent() {
        try {
            checkLocationPermission()

            val settingsTabText = mActivityRule.activity.getString(R.string.menu_setting)

            onView(
                allOf(
                    withText(settingsTabText),
                    isDescendantOfA(withId(R.id.bottom_navigation_main)),
                    isDisplayed(),
                ),
            ).perform(click())

            if (!bottomNavigation.menu.findItem(R.id.menu_settings).isChecked) {
                Assert.fail(TEST_FAILED_NAVIGATION_TAB_SETTINGS_NOT_SELECTED)
            }

            onView(withId(R.id.cl_map_style)).check(matches(isDisplayed()))
            onView(withId(R.id.cl_route_option)).check(matches(isDisplayed()))
            onView(withId(R.id.cl_aws_cloudformation)).check(matches(isDisplayed()))
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
