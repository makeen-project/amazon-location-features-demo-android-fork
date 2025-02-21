package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatRadioButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentChangeLanguageTest : BaseTestMainActivity() {
    @Throws(java.lang.Exception::class)
    override fun before() {
        super.before()
    }

    @Test
    fun checkChangeLanguage() {
        try {
            checkLocationPermission()

            goToLanguage()
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }

    private fun goToLanguage() {
        val settingTabText = mActivityRule.activity.getString(R.string.menu_setting)
        onView(
            allOf(
                withText(settingTabText),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        ).perform(click())
        waitForView(CoreMatchers.allOf(withId(R.id.cl_language), isDisplayed()))
        onView(
            allOf(
                withId(R.id.cl_language),
                isDisplayed()
            )
        ).perform(click())
        val rbArabic = waitForView(CoreMatchers.allOf(withId(R.id.rb_arabic), isDisplayed()))
        onView(
            allOf(
                withId(R.id.rb_arabic),
                isDisplayed()
            )
        ).perform(click())
        rbArabic?.check { view, _ ->
            if (view is AppCompatRadioButton) {
                Assert.assertTrue(TEST_FAILED, view.isChecked)
            }
        }
    }
}
