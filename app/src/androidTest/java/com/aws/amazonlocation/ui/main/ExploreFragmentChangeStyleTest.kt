package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.*
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.ui.main.explore.MapStyleAdapter
import com.aws.amazonlocation.ui.main.explore.MapStyleInnerAdapter
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentChangeStyleTest : BaseTest() {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, true, false)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager
    private val latch = CountDownLatch(1)

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)

        mActivityRule.launchActivity(null)
    }

    private fun getActivity(): AppCompatActivity {
        return mActivityRule.activity
    }

    private fun getCurrentMapName(): String {
        val mapStyleNameDisplay =
            preferenceManager.getValue(
                KEY_MAP_STYLE_NAME,
                getActivity().getString(R.string.map_light)
            )
                ?: getActivity().getString(R.string.map_light)
        val mapName: String

        when (mapStyleNameDisplay) {
            getActivity().getString(R.string.map_light) -> {
                mapName = MapNames.ESRI_LIGHT
            }
            getActivity().getString(R.string.map_streets) -> {
                mapName = MapNames.ESRI_STREET_MAP
            }
            getActivity().getString(R.string.map_navigation) -> {
                mapName = MapNames.ESRI_NAVIGATION
            }
            getActivity().getString(R.string.map_dark_gray) -> {
                mapName = MapNames.ESRI_DARK_GRAY_CANVAS
            }
            getActivity().getString(R.string.map_light_gray) -> {
                mapName = MapNames.ESRI_LIGHT_GRAY_CANVAS
            }
            getActivity().getString(R.string.map_imagery) -> {
                mapName = MapNames.ESRI_IMAGERY
            }
            getActivity().getString(R.string.map_contrast) -> {
                mapName = MapNames.HERE_CONTRAST
            }
            getActivity().getString(R.string.map_explore) -> {
                mapName = MapNames.HERE_EXPLORE
            }
            getActivity().getString(R.string.map_explore_truck) -> {
                mapName = MapNames.HERE_EXPLORE_TRUCK
            }
            getActivity().getString(R.string.map_hybrid) -> {
                mapName = MapNames.HERE_HYBRID
            }
            getActivity().getString(R.string.map_raster) -> {
                mapName = MapNames.HERE_IMAGERY
            }
            else -> {
                mapName = MapNames.ESRI_LIGHT
            }
        }

        return mapName
    }

    @Test
    fun testMapStyleChange() {
        try {
            var mapbox: MapboxMap? = null
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)

            val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
            mapView.getMapAsync {
                mapbox = it
            }
            Thread.sleep(DELAY_2000)

            goToMapStyles()
            loadCountMap()

            while (hasMore()) {
                Thread.sleep(DELAY_2000)
                changeStyle()
                mapbox?.let {
                    checkLoadedTheme(it)
                }
            }
        } catch (e: Exception) {
            failTest(147, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private var countMap: HashMap<Int, Int> = HashMap()

    private var mapNameIndex = 0
    private var selectedPosition = -1

    private fun loadCountMap() {
        val mainRv = mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_map_style)
        val adapter = mainRv.adapter as? MapStyleAdapter

        for (i in 0 until (adapter?.itemCount ?: 0)) {
            val holder = mainRv.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.rv_map_name)
                val innerAdapter = recyclerView.adapter as? MapStyleInnerAdapter
                countMap[i] = innerAdapter?.itemCount ?: 0
            }
        }
    }

    private fun hasMore(): Boolean {
        if (mapNameIndex >= countMap.keys.size - 1) {
            if (selectedPosition >= (countMap[mapNameIndex] ?: 0) - 1) {
                return false
            }
        }
        return true
    }

    private fun changeStyle() {
        val mainRv = mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_map_style)
        if (selectedPosition < (countMap[mapNameIndex] ?: 0) - 1) {
            val holder = mainRv.findViewHolderForAdapterPosition(mapNameIndex)
            val recyclerView = holder?.itemView?.findViewById<RecyclerView>(R.id.rv_map_name)
            selectedPosition++
            changeInnerStyle(recyclerView)
        } else {
            mapNameIndex++
            selectedPosition = -1
            val holder = mainRv.findViewHolderForAdapterPosition(mapNameIndex)
            val recyclerView = holder?.itemView?.findViewById<RecyclerView>(R.id.rv_map_name)
            onView(withId(R.id.rv_map_style)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    mapNameIndex,
                    click()
                )
            )
            Thread.sleep(DELAY_2000)
            changeInnerStyle(recyclerView)
        }
    }

    private fun changeInnerStyle(rv: RecyclerView?) {
        val holder = rv?.findViewHolderForAdapterPosition(selectedPosition)

        getInstrumentation().runOnMainSync {
            holder?.itemView?.findViewById<MaterialCardView>(R.id.card_map_image)?.performClick()
        }
        Thread.sleep(DELAY_2000)
    }

    private fun goToMapStyles() {
        val cardMap = mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_map)
        mActivityRule.activity.runOnUiThread {
            cardMap.performClick()
        }

        val clSearchSheet =
            mActivityRule.activity.findViewById<ConstraintLayout>(R.id.bottom_sheet_map_style)
        if (clSearchSheet.visibility == View.VISIBLE) {
            val mBottomSheetSearchPlaces: BottomSheetBehavior<ConstraintLayout> =
                BottomSheetBehavior.from(clSearchSheet)
            getInstrumentation().runOnMainSync {
                mBottomSheetSearchPlaces.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } else {
            Assert.fail(TEST_FAILED_STYLE_SHEET)
        }
    }

    private fun checkLoadedTheme(mapboxMap: MapboxMap) {
        Thread.sleep(DELAY_3000)

        getInstrumentation().runOnMainSync {
            mapboxMap.getStyle {
                val correctStyleLoaded = it.json.let { json ->

                    val obj = JSONObject(json).getJSONObject(JSON_KEY_SOURCES)

                    val key = if (obj.has(JSON_KEY_ESRI)) {
                        JSON_KEY_ESRI
                    } else if (obj.has(JSON_KEY_HERE)) {
                        JSON_KEY_HERE
                    } else if (obj.has(JSON_KEY_RASTER_TILES)) {
                        JSON_KEY_RASTER_TILES
                    } else {
                        null
                    }

                    if (key != null) {
                        val source = obj.getJSONObject(key)
                        val tiles = source.getJSONArray(JSON_KEY_TILES)
                        if (tiles.length() > 0) {
                            tiles.getString(0)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }?.contains(getCurrentMapName()) == true

                if (!correctStyleLoaded) {
                    Assert.fail(TEST_FAILED_INCORRECT_STYLE)
                }

                latch.countDown()
            }
        }

        getInstrumentation().waitForIdleSync()
        latch.await()
    }
}
