package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_IMAGE_LABEL
import com.aws.amazonlocation.TEST_WORD_1
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchPlaceDisplayedOnMapPOICircleTest {

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
    private lateinit var preferenceManager: PreferenceManager

    @Before
    fun setUp() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(KEY_MAP_STYLE_NAME, mActivityRule.activity.getString(R.string.map_light))
        preferenceManager.setValue(KEY_MAP_NAME, mActivityRule.activity.getString(R.string.esri))
    }

    @Test
    fun showSearchPlaceDisplayedOnMapPOICircleTest() {
        var mapbox: MapboxMap? = null
        enableGPS(ApplicationProvider.getApplicationContext())
        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_1000)

        val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        val edtSearch =
            onView(withId(R.id.edt_search_places)).check(ViewAssertions.matches(isDisplayed()))
        edtSearch.perform(click())
        onView(withId(R.id.edt_search_places)).perform(typeText(TEST_WORD_1))
        uiDevice.wait(
            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion")),
            DELAY_10000
        )
        Thread.sleep(DELAY_1000)
        val rvSearchPlaceSuggestion =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion)
        if (rvSearchPlaceSuggestion.adapter?.itemCount != null) {
            rvSearchPlaceSuggestion.adapter?.itemCount?.let { it ->
                if (it >= 0) {
                    mapbox?.getStyle { style ->
                        mActivityRule.activity.runOnUiThread {
                            val image = style.getImage(TEST_IMAGE_LABEL)
                            Assert.assertTrue(TEST_FAILED_IMAGE_NULL, image != null)
                        }
                    }
                } else {
                    Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                }
            }
        } else {
            Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
        }
    }
}
