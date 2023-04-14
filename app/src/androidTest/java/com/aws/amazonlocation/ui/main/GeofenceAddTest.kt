package com.aws.amazonlocation.ui.main

import android.view.View
import android.widget.SeekBar
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
import com.aws.amazonlocation.di.AppModule
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.util.*
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class GeofenceAddTest {

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

    private var geofenceName = getRandomGeofenceName()

    @Test
    fun addGeoFenceTest() {
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

            uiDevice.wait(
                Until.gone(By.res("com.aws.amazonlocation:id/cl_search_loader_geofence_list")),
                DELAY_5000
            )
            Thread.sleep(DELAY_2000)
            val emptyContainer =
                mActivityRule.activity.findViewById<View>(R.id.cl_empty_geofence_list)

            if (emptyContainer.isVisible) {
                val btnAddGeofence =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_add_geofence)
                mActivityRule.activity.runOnUiThread {
                    btnAddGeofence.performClick()
                }
            } else {
                val cardAddGeofence =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_add_geofence)
                mActivityRule.activity.runOnUiThread {
                    cardAddGeofence.performClick()
                }
            }
            Thread.sleep(DELAY_2000)
            val edtAddGeofenceSearch1 =
                mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_add_geofence_search)
            mActivityRule.activity.runOnUiThread {
                edtAddGeofenceSearch1.performClick()
            }
            onView(withId(R.id.edt_add_geofence_search)).perform(clearText())
            onView(withId(R.id.edt_add_geofence_search)).perform(typeText(TEST_WORD_4))

            Thread.sleep(DELAY_5000)
            val rvGeofenceSearchPlaces =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_geofence_search_places_suggestion)
            rvGeofenceSearchPlaces.adapter?.itemCount?.let {
                if (it > 0) {
                    onView(withId(R.id.rv_geofence_search_places_suggestion)).perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                            0,
                            click()
                        )
                    )
                }
            }
            Thread.sleep(DELAY_2000)
            val seekbar = mActivityRule.activity.findViewById<SeekBar>(R.id.seekbar_geofence_radius)
            seekbar.progress = 400

            Thread.sleep(DELAY_1000)

            onView(withId(R.id.edt_enter_geofence_name)).perform(typeText(geofenceName))

            val btnAddGeofenceSave =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_add_geofence_save)
            mActivityRule.activity.runOnUiThread {
                btnAddGeofenceSave.performClick()
            }

            uiDevice.wait(Until.hasObject(By.res("com.aws.amazonlocation:id/rv_geofence")), DELAY_5000)

            onView(withId(R.id.rv_geofence)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(geofenceName))
                )
            )
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}