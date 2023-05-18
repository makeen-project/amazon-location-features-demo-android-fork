package com.aws.amazonlocation.ui.main

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.utils.* // ktlint-disable no-wildcard-imports
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.core.AllOf.allOf
import org.json.JSONObject
import org.junit.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.CountDownLatch

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SettingsFragmentChangeStyleTest : BaseTestMainActivity() {

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    private lateinit var preferenceManager: PreferenceManager
    private val latch = CountDownLatch(1)
    private var mapbox: MapboxMap? = null

    @Throws(java.lang.Exception::class)
    override fun before() {
        preferenceManager = PreferenceManager(ApplicationProvider.getApplicationContext())
        preferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        preferenceManager.removeValue(KEY_MAP_NAME)
        preferenceManager.removeValue(KEY_MAP_STYLE_NAME)
        super.before()
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

            Thread.sleep(DELAY_2000)

            changeMapStyle(true, 3)
            checkLoadedTheme()

            Thread.sleep(DELAY_2000)

            changeMapStyle(false, 2)
            checkLoadedTheme()
        } catch (e: Exception) {
            failTest(132, e)
            Assert.fail(TEST_FAILED)
        }
    }

    private fun changeMapStyle(isEsri: Boolean, styleIndex: Int) {
        goToMapStyles()

        if (isEsri) {
            waitForView(allOf(withId(R.id.rv_esri), isDisplayed()))?.perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(styleIndex, click()),
            )
        } else {
            waitForView(allOf(withId(R.id.rv_here), isDisplayed()))?.perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(styleIndex, click()),
            )
        }
    }

    private fun goToMapStyles() {
        waitForView(
            allOf(
                withText(mActivityRule.activity.getString(R.string.menu_setting)),
                isDescendantOfA(withId(R.id.bottom_navigation_main)),
                isDisplayed(),
            ),
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)

        waitForView(
            allOf(
                withId(R.id.cl_map_style),
                isDisplayed(),
            ),
        )
            ?.perform(click())

        Thread.sleep(DELAY_3000)
    }

    private fun checkLoadedTheme() {
        Thread.sleep(DELAY_2000)

        if (getActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_main).selectedItemId != R.id.menu_explore) {
            uiDevice.pressBack()
        }

        Thread.sleep(DELAY_2000)

        if (getActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_main).selectedItemId != R.id.menu_explore) {
            uiDevice.pressBack()
        }

        uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
        Thread.sleep(DELAY_2000)

        val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        getInstrumentation().waitForIdleSync()
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
        Thread.sleep(DELAY_2000)
    }

    override fun after() {
        super.after()
        val targetContext = ApplicationProvider.getApplicationContext<Context>()
        val packageName = targetContext.packageName
        // Clear app from recent apps list
        val am = targetContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.let {
            it.appTasks.forEach { task ->
                if (task.taskInfo.baseActivity?.packageName == packageName) {
                    task.finishAndRemoveTask()
                }
            }
        }
    }
}
