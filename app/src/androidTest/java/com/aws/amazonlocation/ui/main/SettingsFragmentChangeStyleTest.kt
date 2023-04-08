package com.aws.amazonlocation.ui.main

import androidx.appcompat.app.AppCompatActivity
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
import com.aws.amazonlocation.ui.main.map_style.EsriMapStyleAdapter
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.json.JSONObject
import org.junit.*
import java.util.concurrent.CountDownLatch

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentChangeStyleTest {

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
    private var mapbox: MapboxMap? = null

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
            preferenceManager.getValue(KEY_MAP_STYLE_NAME, getActivity().getString(R.string.map_light))
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
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)

            checkLoadedTheme()

            var hasMore = true

            while (hasMore) {
                hasMore = changeMapStyle()
                Thread.sleep(DELAY_2000)
                checkLoadedTheme()
            }
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }

    private fun changeMapStyle(): Boolean {
        goToMapStyles()

        var hasMoreStyles = false

        val rvEsriStyles =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_esri)
        val adapterEsri = rvEsriStyles.adapter as? EsriMapStyleAdapter

        val rvHereStyles =
            mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_here)
        val adapterHere = rvHereStyles.adapter as? EsriMapStyleAdapter
        adapterHere?.let { here ->
            adapterEsri?.let {
                if (it.itemCount > 0 && here.itemCount > 0) {
                    val mapName = preferenceManager.getValue(KEY_MAP_NAME, getActivity().getString(R.string.map_esri))
                    if (mapName == getActivity().getString(R.string.esri)) {
                        if (it.selectedPosition in -1 until it.itemCount - 1) {
                            onView(withId(R.id.rv_esri)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    it.selectedPosition + 1,
                                    click()
                                )
                            )
                            hasMoreStyles = true
                        } else {
                            onView(withId(R.id.rv_here)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    0,
                                    click()
                                )
                            )
                            hasMoreStyles = true
                        }
                    } else if (mapName == getActivity().getString(R.string.here)) {
                        hasMoreStyles = if (it.selectedPosition in 0 until here.itemCount - 1) {
                            onView(withId(R.id.rv_here)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    it.selectedPosition + 1,
                                    click()
                                )
                            )
                            true
                        } else {
                            false
                        }
                    }
                }
            }
        }
        return hasMoreStyles
    }

    private fun goToMapStyles() {
        onView(
            allOf(
                withText(SETTINGS),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed()
            )
        )
            .perform(click())

        Thread.sleep(DELAY_2000)

        onView(
            allOf(
                withId(R.id.cl_map_style),
                isDisplayed()
            )
        )
            .perform(click())

        Thread.sleep(DELAY_2000)
    }

    private fun checkLoadedTheme() {
        if (getActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_main).selectedItemId != R.id.menu_explore) {
            uiDevice.pressBack()
        }

        Thread.sleep(DELAY_1000)

        if (getActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_main).selectedItemId != R.id.menu_explore) {
            uiDevice.pressBack()
        }

        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        getInstrumentation().runOnMainSync {
            mapbox?.getStyle {
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

        latch.await()
    }

    @After
    fun tearDown() {
        mActivityRule.finishActivity()
    }
}
