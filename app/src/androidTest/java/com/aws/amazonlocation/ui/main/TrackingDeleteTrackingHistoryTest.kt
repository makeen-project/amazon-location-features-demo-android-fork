package com.aws.amazonlocation.ui.main

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingDeleteTrackingHistoryTest : BaseTestMainActivity() {

    @Test
    fun showDeleteTrackingTest() {
        checkLocationPermission()
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_tracking)),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.label_start_tracking)),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(allOf(withId(R.id.rv_tracking), isDisplayed(), hasMinimumChildCount(1)))

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.label_stop_tracking)),
                isDisplayed(),
            ),
        )?.perform(click())

        val clPersistentBottomSheet = waitForView(
            allOf(
                withId(R.id.bottom_sheet_tracking),
                isDisplayed()
            ),
        )
        clPersistentBottomSheet?.check { view, _ ->
            if (view is ConstraintLayout) {
                val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                    BottomSheetBehavior.from(view)
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        waitForView(
            allOf(
                withId(R.id.tv_delete_tracking_data),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.label_delete_tracking_data)),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.ok)),
                isDisplayed(),
            ),
        )?.perform(click())

        waitForView(
            allOf(
                withId(R.id.layout_no_data_found),
                isDisplayed(),
            ),
        )
    }
}
