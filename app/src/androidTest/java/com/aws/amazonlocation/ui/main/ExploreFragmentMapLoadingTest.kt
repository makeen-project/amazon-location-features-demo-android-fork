package com.aws.amazonlocation.ui.main

import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTestMainActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.checkLocationPermission
import com.aws.amazonlocation.di.AppModule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Test
import org.maplibre.android.maps.MapView

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExploreFragmentMapLoadingTest : BaseTestMainActivity() {
    @Test
    fun testMapLoaded() {
        try {
            checkLocationPermission()
            val map =
                mActivityRule.activity.findViewById<MapView>(R.id.mapView)
            Assert.assertEquals(AMAZON_MAP_READY, map.contentDescription)
        } catch (e: Exception) {
            Assert.fail("$TEST_FAILED ${e.message}")
        }
    }
}
