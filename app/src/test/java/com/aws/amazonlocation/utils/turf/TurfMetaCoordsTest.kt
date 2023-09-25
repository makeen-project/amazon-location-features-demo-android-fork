package com.aws.amazonlocation.utils.turf

import com.aws.amazonlocation.mock.TEST_DATA_LAT
import com.aws.amazonlocation.mock.TEST_DATA_LNG
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TurfMetaCoordsTest {

    @Test
    fun turfMetaCordTest() {
        val pointList = TurfMeta.coordAll(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG))
        Assert.assertTrue(pointList.isNotEmpty())
//        val pointList5 = TurfMeta.coordAll(
//            Polygon.fromLngLats(
//                mutableListOf(
//                    mutableListOf(
//                        Point.fromLngLat(
//                            TEST_DATA_LAT,
//                            TEST_DATA_LNG
//                        )
//                    )
//                )
//            ), false
//        )
//        Assert.assertTrue(pointList5.isNotEmpty())
//        val pointList6 = TurfMeta.coordAll(
//            MultiLineString.fromLngLats(
//                mutableListOf(
//                    mutableListOf(
//                        Point.fromLngLat(
//                            TEST_DATA_LAT,
//                            TEST_DATA_LNG
//                        )
//                    )
//                )
//            )
//        )
//        Assert.assertTrue(pointList6.isNotEmpty())
//        val pointList7 = TurfMeta.coordAll(
//            MultiPolygon.fromLngLats(
//                mutableListOf(
//                    mutableListOf(
//                        mutableListOf(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG))
//                    )
//                )
//            ), true
//        )
//        Assert.assertTrue(pointList7.isNotEmpty())
//        val pointList8 = TurfMeta.coordAll(
//            MultiPolygon.fromLngLats(
//                mutableListOf(
//                    mutableListOf(
//                        mutableListOf(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG))
//                    )
//                )
//            ), false
//        )
//        Assert.assertTrue(pointList8.isNotEmpty())
//        val feature: Feature = Feature.fromJson(jsonTurf)
//        val feature1: FeatureCollection = FeatureCollection.fromJson(jsonTurf1)
//        val pointList9 = TurfMeta.coordAll(feature, true)
//        Assert.assertTrue(pointList9.isNotEmpty())
//        val pointList10 = TurfMeta.coordAll(feature, false)
//        Assert.assertTrue(pointList10.isNotEmpty())
//        val pointList11 = TurfMeta.coordAll(feature1, false)
//        Assert.assertTrue(pointList11.isNotEmpty())
//        val pointList12 = TurfMeasurement.distance(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG))
//        Assert.assertTrue(!pointList12.isNaN())
//        val pointList13 = TurfMeasurement.destination(Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG), 10.654654, 10.5, TurfConstants.UNIT_MILES)
//        Assert.assertTrue(!pointList13.latitude().isNaN())
//        val pointList14 = TurfMeasurement.bbox(feature1)
//        Assert.assertTrue(!pointList14[0].isNaN())
//        val pointList15 = TurfMeasurement.center(feature1)
//        Assert.assertTrue(pointList15 != null)
//        val pointList16 = TurfMeasurement.center(feature)
//        Assert.assertTrue(pointList16 != null)
//        val pointList17 = TurfMeasurement.center(feature1, null, "test")
//        Assert.assertTrue(pointList17 != null)
    }

    @Test
    fun turfMetaCordAllTest() {
        val pointList = TurfMeta.coordAll(
            MultiPoint.fromLngLats(
                mutableListOf(
                    Point.fromLngLat(
                        TEST_DATA_LAT,
                        TEST_DATA_LNG
                    ), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllLineStringTest() {
        val pointList = TurfMeta.coordAll(
            LineString.fromLngLats(
                mutableListOf(
                    Point.fromLngLat(
                        TEST_DATA_LAT,
                        TEST_DATA_LNG
                    ), Point.fromLngLat(TEST_DATA_LAT, TEST_DATA_LNG)
                )
            )
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }

    @Test
    fun turfMetaCordAllPolygonTest() {
        val pointList = TurfMeta.coordAll(
            Polygon.fromLngLats(
                mutableListOf(
                    mutableListOf(
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        ),
                        Point.fromLngLat(
                            TEST_DATA_LAT,
                            TEST_DATA_LNG
                        )
                    )
                )
            ), true
        )
        Assert.assertTrue(pointList.isNotEmpty())
    }
}
