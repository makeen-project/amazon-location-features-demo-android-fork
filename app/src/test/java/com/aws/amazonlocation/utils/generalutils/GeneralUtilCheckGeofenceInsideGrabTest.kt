package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.checkGeofenceInsideGrab
import com.aws.amazonlocation.utils.latSouth
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilCheckGeofenceInsideGrabTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun checkGeofenceInsideGrabTest() {
        val mPreferenceManager = PreferenceManager(context)
        mPreferenceManager.setValue(KEY_USER_REGION, SE_REGION)
        val result = checkGeofenceInsideGrab(LatLng(latSouth, latSouth), mPreferenceManager, context)
        Assert.assertTrue(
            TEST_FAILED_GRAB_MAP_NOT_ENABLE,
            result
        )
    }
}
