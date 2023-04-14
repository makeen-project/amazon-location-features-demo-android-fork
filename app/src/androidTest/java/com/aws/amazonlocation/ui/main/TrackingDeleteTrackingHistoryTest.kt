package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TRACKING
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingDeleteTrackingHistoryTest {

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
    fun showDeleteTrackingTest() {
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_1000)

        val tracking = uiDevice.findObject(By.text(TRACKING))
        tracking.click()
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker))),
            DELAY_1000
        )
        val labelContinue =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker)))
        labelContinue?.click()

        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
            DELAY_1000
        )

        val clEnableTracking =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_enable_tracking)
        if (clEnableTracking.visibility == View.VISIBLE) {

            val btnEnableTracking =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_enable_tracking)
            mActivityRule.activity.runOnUiThread {
                btnEnableTracking.performClick()
            }
        }
        Thread.sleep(DELAY_5000)
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking))),
            DELAY_1000
        )
        val labelStartTracking =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
        labelStartTracking?.click()

        Thread.sleep(DELAY_15000)
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_stop_tracking))),
            DELAY_1000
        )
        val labelStopTracking =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_stop_tracking)))
        labelStopTracking?.click()

        val clPersistentBottomSheet =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_tracking)
        if (clPersistentBottomSheet.visibility == View.VISIBLE) {
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clPersistentBottomSheet)
            getInstrumentation().runOnMainSync {
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val tvDeleteTrackingData =
            mActivityRule.activity.findViewById<AppCompatTextView>(R.id.tv_delete_tracking_data)
        if (tvDeleteTrackingData.visibility == View.VISIBLE) {
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_delete_tracking_data))),
                DELAY_1000
            )

            val labelDeleteTrackingData =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_delete_tracking_data)))
            labelDeleteTrackingData?.click()
            Thread.sleep(DELAY_1000)
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.ok))),
                DELAY_1000
            )

            val labelOk =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.ok)))
            labelOk?.click()
            uiDevice.wait(
                Until.hasObject(By.res("com.aws.amazonlocation:id/layout_no_data_found")),
                DELAY_1000
            )
            val rvTracking =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_tracking)
            val itemCount = rvTracking.adapter?.itemCount ?: 0

            Assert.assertTrue(itemCount == 0)
        } else {
            Assert.fail(TEST_FAILED)
        }
    }
}