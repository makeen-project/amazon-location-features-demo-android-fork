package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.*
import com.aws.amazonlocation.*
import com.aws.amazonlocation.actions.clickXYPercent
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.util.*
import kotlin.properties.Delegates
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceEditTest {

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

    private lateinit var geofenceName: String
    private var updatedRadius by Delegates.notNull<Int>()

    @Test
    fun editGeoFenceTest() {
        try {
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            onView(
                allOf(
                    withId(R.id.card_geofence_map),
                    isDisplayed()
                )
            ).perform(click())

            Thread.sleep(DELAY_1000)

            uiDevice.wait(Until.gone(By.res("com.aws.amazonlocation:id/cl_search_loader_geofence_list")), DELAY_5000)

            createOrGetGeoFence()

            editGeoFence()

            verifyGeoFenceUpdate()
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun createOrGetGeoFence() {
        val rv = mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_geofence)

        val emptyContainer = mActivityRule.activity.findViewById<View>(R.id.cl_empty_geofence_list)

        if (emptyContainer.isVisible) {
            geofenceName = getRandomGeofenceName()
            Thread.sleep(DELAY_1000)
            onView(
                allOf(
                    withId(R.id.btn_add_geofence),
                    isDisplayed(),
                    isEnabled()
                )
            ).perform(click())

            Thread.sleep(DELAY_1000)

            onView(
                withId(R.id.mapView)
            ).perform(
                clickXYPercent(
                    getRandom1To100(),
                    getRandom1To100()
                )
            )

            Thread.sleep(DELAY_1000)

            val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)
            seekbar.progress = ((seekbar.max - seekbar.min) * getRandom0_01To1_0()).toInt()

            Thread.sleep(DELAY_1000)

            onView(withId(R.id.edt_enter_geofence_name)).perform(typeText(geofenceName))
            Thread.sleep(DELAY_1000)
            onView(withId(R.id.btn_add_geofence_save)).perform(click())

            uiDevice.wait(Until.hasObject(By.res("com.aws.amazonlocation:id/rv_geofence")), DELAY_5000)

            onView(withId(R.id.rv_geofence)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(geofenceName))))
        } else {
            val holder = rv.findViewHolderForAdapterPosition(0)
            geofenceName = holder?.itemView?.findViewById<AppCompatTextView>(R.id.tv_geofence_address_type)?.text.toString()
        }
    }

    private fun editGeoFence() {
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName)
                ),
                click()
            )
        )

        Thread.sleep(DELAY_1000)

        val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)
        seekbar.progress = ((seekbar.max - seekbar.min) * getRandom0_01To1_0()).toInt()
        Thread.sleep(DELAY_1000)
        updatedRadius = seekbar.progress

        onView(withId(R.id.btn_add_geofence_save)).perform(click())

        uiDevice.wait(Until.hasObject(By.res("com.aws.amazonlocation:id/rv_geofence")), DELAY_5000)
    }

    private fun verifyGeoFenceUpdate() {
        Thread.sleep(DELAY_1000)
        onView(withId(R.id.rv_geofence)).perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(geofenceName))))
        onView(withId(R.id.rv_geofence)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(
                    withText(geofenceName)
                ),
                click()
            )
        )
        Thread.sleep(DELAY_1000)
        val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)

        if (seekbar.progress != updatedRadius) {
            Assert.fail(TEST_FAILED)
        }
    }
}