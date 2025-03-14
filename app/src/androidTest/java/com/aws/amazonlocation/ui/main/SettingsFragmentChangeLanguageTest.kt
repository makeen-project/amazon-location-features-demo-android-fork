package com.aws.amazonlocation.ui.main

import androidx.appcompat.widget.AppCompatRadioButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf.allOf
import org.junit.*

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
                isDisplayed(),
            ),
        ).perform(click())
        waitForView(CoreMatchers.allOf(withId(R.id.cl_language), isDisplayed()))
        onView(
            allOf(
                withId(R.id.cl_language),
                isDisplayed(),
            ),
        ).perform(click())
        val rbArabic = waitForView(CoreMatchers.allOf(withId(R.id.rb_arabic), isDisplayed()))
        onView(
            allOf(
                withId(R.id.rb_arabic),
                isDisplayed(),
            ),
        ).perform(click())
        rbArabic?.check { view, _ ->
            if (view is AppCompatRadioButton) {
                Assert.assertTrue(TEST_FAILED, view.isChecked)
            }
        }
    }
}
