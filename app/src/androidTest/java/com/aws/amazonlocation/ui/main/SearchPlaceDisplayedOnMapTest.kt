package com.aws.amazonlocation.ui.main

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_IMAGE_NULL
import com.aws.amazonlocation.TEST_FAILED_NO_SEARCH_RESULT
import com.aws.amazonlocation.TEST_WORD_SHYAMAL
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.waitForView
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class SearchPlaceDisplayedOnMapTest : BaseTestMainActivity() {

    @Test
    fun showSearchPlaceDisplayedOnMapTest() {
        try {
            checkLocationPermission()

            val mapView = mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            var mapbox: MapLibreMap? = null
            val latch = CountDownLatch(1)

            // Wait for map to be ready
            mapView.getMapAsync {
                mapbox = it
                latch.countDown()
            }
            latch.await(5, TimeUnit.SECONDS)

            // Perform search
            onView(withId(R.id.edt_search_places))
                .check(matches(isDisplayed()))
                .perform(click(), replaceText(TEST_WORD_SHYAMAL))

            val rvSearchPlaceSuggestion = waitForView(
                allOf(
                    withId(R.id.rv_search_places_suggestion),
                    isDisplayed(),
                    hasMinimumChildCount(1)
                )
            )

            var itemCount = 0
            rvSearchPlaceSuggestion?.check { view, _ ->
                if (view is RecyclerView) {
                    itemCount = view.adapter?.itemCount ?: 0
                } else {
                    Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
                }
            }

            if (itemCount > 0) {
                var sources = 0
                getInstrumentation().runOnMainSync {
                    mapbox?.getStyle { style ->
                        sources = style.sources.size
                    }
                }
                Assert.assertTrue(TEST_FAILED_IMAGE_NULL, sources > 0)
            } else {
                Assert.fail(TEST_FAILED_NO_SEARCH_RESULT)
            }

        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}

