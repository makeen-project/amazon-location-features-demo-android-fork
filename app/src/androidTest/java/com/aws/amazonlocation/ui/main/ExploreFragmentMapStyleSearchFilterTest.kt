package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapStyleSearchFilterTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
    }

    @Test
    fun testMapStyleSearchFilterTest() {
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        goToMapStyles()
        Thread.sleep(DELAY_2000)
        val clMapStyleSheet =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
        if (clMapStyleSheet.visibility == View.VISIBLE) {
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clMapStyleSheet)
            mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED

            Thread.sleep(DELAY_2000)

            val imgFilter =
                onView(withId(R.id.img_filter)).check(ViewAssertions.matches(isDisplayed()))
            imgFilter?.perform(click())
            Thread.sleep(DELAY_2000)
            onView(withId(R.id.rv_provider)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    clickOnViewChild(R.id.cb_sorting)
                )
            )
            Thread.sleep(DELAY_2000)
            val btnApplyFilter =
                onView(withId(R.id.btn_apply_filter)).check(ViewAssertions.matches(isDisplayed()))
            btnApplyFilter?.perform(click())

            Thread.sleep(DELAY_2000)
            val ivSearch =
                onView(withId(R.id.iv_search)).check(ViewAssertions.matches(isDisplayed()))
            ivSearch?.perform(click())
            Thread.sleep(DELAY_2000)
            val etSearchMap =
                onView(withId(R.id.et_search_map)).check(ViewAssertions.matches(isDisplayed()))
            etSearchMap.perform(click())
            onView(withId(R.id.et_search_map)).perform(ViewActions.replaceText(TEST_WORD_14))
            Thread.sleep(DELAY_2000)
            val mapDarkGray =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.map_dark_gray)))
            mapDarkGray.click()
            Thread.sleep(DELAY_2000)
            val mapStyleNameDisplay =
                preferenceManager.getValue(
                    KEY_MAP_STYLE_NAME,
                    getActivity().getString(R.string.map_light)
                )
                    ?: getActivity().getString(R.string.map_light)
            Assert.assertTrue(
                TEST_FAILED_STYLE_SHEET,
                mapStyleNameDisplay == TEST_WORD_15
            )
        }
    }

    private fun clickOnViewChild(viewId: Int) = object : ViewAction {
        override fun getConstraints() = null

        override fun getDescription() = ""

        override fun perform(uiController: UiController, view: View) =
            click().perform(uiController, view.findViewById<View>(viewId))
    }

    private fun goToMapStyles() {
        val cardMap = waitForView(allOf(withId(R.id.card_map), isDisplayed()))
        cardMap?.perform(click())

        Thread.sleep(DELAY_2000)

        getInstrumentation().runOnMainSync {
            val clSearchSheet =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
            if (clSearchSheet.isVisible) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(clSearchSheet)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                Assert.fail(TEST_FAILED_STYLE_SHEET)
            }
        }
    }

    private fun getActivity(): AppCompatActivity {
        return mActivityRule.activity
    }
}
