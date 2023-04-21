package com.aws.amazonlocation.utils

import com.aws.amazonlocation.mock.DEFAULT_LOCATION
import com.aws.amazonlocation.utils.Durations.DEFAULT_RADIUS
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.mapbox.geojson.Point
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Abhin.
 */

@RunWith(RobolectricTestRunner::class)
class TurfModuleTest {

    @Test
    fun turfTest() {
        TurfTransformation.circle(
            Point.fromLngLat(DEFAULT_LOCATION.latitude, DEFAULT_LOCATION.longitude),
            DEFAULT_RADIUS.toDouble(),
            360,
            TurfConstants.UNIT_METRES
        )

        TurfTransformation.circle(
            Point.fromLngLat(DEFAULT_LOCATION.latitude, DEFAULT_LOCATION.longitude),
            DEFAULT_RADIUS.toDouble(),
            TurfConstants.UNIT_DEFAULT
        )

        val turnTransformation = TurfTransformation.circle(
            Point.fromLngLat(DEFAULT_LOCATION.latitude, DEFAULT_LOCATION.longitude),
            DEFAULT_RADIUS.toDouble()
        )
        val pointList = TurfMeta.coordAll(turnTransformation, false)
        TurfTransformation.simplify(pointList)
        TurfTransformation.simplify(pointList, 10.0)
        TurfTransformation.simplify(pointList, true)
        val data = TurfTransformation.simplify(pointList, 10.0, true)
        Assert.assertTrue(data.isNotEmpty())
    }
}
