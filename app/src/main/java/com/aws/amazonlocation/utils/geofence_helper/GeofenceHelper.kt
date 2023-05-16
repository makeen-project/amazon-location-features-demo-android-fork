package com.aws.amazonlocation.utils.geofence_helper

import android.content.Context
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R
import com.aws.amazonlocation.utils.Durations.CAMERA_BOTTOM_PADDING
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1000
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1500
import com.aws.amazonlocation.utils.Durations.CAMERA_TOP_RIGHT_LEFT_PADDING
import com.aws.amazonlocation.utils.Durations.DEFAULT_RADIUS
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_ICON_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_LAYER_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_SOURCE_ID
import com.aws.amazonlocation.utils.GeofenceCons.RADIUS_SEEKBAR_DIFFERENCE
import com.aws.amazonlocation.utils.GeofenceCons.RADIUS_SEEKBAR_MAX
import com.aws.amazonlocation.utils.GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID
import com.aws.amazonlocation.utils.GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID
import com.aws.amazonlocation.utils.GeofenceCons.TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID
import com.aws.amazonlocation.utils.GeofenceCons.TURF_CALCULATION_LINE_LAYER_ID
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
import com.aws.amazonlocation.utils.MapCameraZoom
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants.UNIT_METRES
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceHelper(
    private var mAppContext: Context,
    private var mTvSeekBar: AppCompatTextView?,
    private var mSeekBar: SeekBar?,
    private var mMapboxMap: MapboxMap?,
    private var mGeofenceMapLatLngInterface: GeofenceMapLatLngInterface?,
    private var mPrefrenceManager: PreferenceManager?,
) {

    var mDefaultLatLng = LatLng(49.281174, -123.116823)
    private var mDefaultLocationPoint =
        fromLngLat(mDefaultLatLng.longitude, mDefaultLatLng.latitude)
    private val mCircleUnit: String = UNIT_METRES
    private var mLastClickPoint: Point = mDefaultLocationPoint
    var mCircleRadius = DEFAULT_RADIUS
    var mIsDefaultGeofence = false

    private fun getLiveLocation(): LatLng? {
        var mLatLng: LatLng? = null
        if (mMapboxMap?.locationComponent?.isLocationComponentActivated == true) {
            mMapboxMap?.locationComponent?.lastKnownLocation?.apply {
                mLatLng = LatLng(
                    latitude,
                    longitude,
                )
            }
        }
        return mLatLng
    }

    fun initMapBoxStyle() {
        getLiveLocation()?.let {
            mDefaultLatLng = it
            mDefaultLocationPoint = fromLngLat(it.longitude, it.latitude)
            mLastClickPoint = mDefaultLocationPoint
        }
    }

    fun setDefaultIconWithGeofence() {
        mDefaultLocationPoint = fromLngLat(mDefaultLatLng.longitude, mDefaultLatLng.latitude)
        mLastClickPoint = mDefaultLocationPoint
        mMapboxMap?.getStyle { style ->
            drawMarkerOnMap(style)

            if (style.getSource(TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID) == null) {
                style.addSource(GeoJsonSource(TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID))
            }

            if (style.getSource(TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID) == null) {
                style.addSource(GeoJsonSource(TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID))
            }

            initPolygonCircleFillLayer()
            mSeekBar?.apply {
                max = RADIUS_SEEKBAR_MAX
                incrementProgressBy(RADIUS_SEEKBAR_DIFFERENCE)
                progress = mCircleRadius
            }
            mSeekBar?.progress?.let { progress ->
                mCircleRadius = progress
            }
            upDateSeekbarText(mCircleRadius)
            drawPolygonCircle(mLastClickPoint)
            drawGeofence(mLastClickPoint)

            mSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    adjustRadius(seekBar?.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    adjustRadius(seekBar?.progress)
                }
            })
        }
        mIsDefaultGeofence = true
    }

    private fun drawMarkerOnMap(style: Style) {
        BitmapUtils.getBitmapFromDrawable(
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.ic_geofence_marker,
            ),
        )?.let {
            style.addImage(
                CIRCLE_CENTER_ICON_ID,
                it,
            )
        }
        if (style.getSource(CIRCLE_CENTER_SOURCE_ID) == null) {
            style.addSource(
                GeoJsonSource(
                    CIRCLE_CENTER_SOURCE_ID,
                    Feature.fromGeometry(mDefaultLocationPoint),
                ),
            )
        }

        if (style.getSource(CIRCLE_CENTER_LAYER_ID) == null && style.getLayer(
                CIRCLE_CENTER_LAYER_ID,
            ) == null
        ) {
            style.addLayer(
                SymbolLayer(CIRCLE_CENTER_LAYER_ID, CIRCLE_CENTER_SOURCE_ID).withProperties(
                    iconImage(CIRCLE_CENTER_ICON_ID),
                    iconIgnorePlacement(false),
                    iconAllowOverlap(true),
                    textAllowOverlap(true),
                    iconAnchor(Property.ICON_ANCHOR_CENTER),
                ),
            )
        }
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer() {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                TURF_CALCULATION_FILL_LAYER_ID,
                TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID,
            )

            val lineLayer = LineLayer(
                TURF_CALCULATION_LINE_LAYER_ID,
                TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID,
            )

            lineLayer.setProperties(
                lineWidth(2f),
                lineColor(
                    ContextCompat.getColor(
                        mAppContext,
                        R.color.color_bn_selected,
                    ),
                ),
            )

            fillLayer.setProperties(
                fillColor(ContextCompat.getColor(mAppContext, R.color.color_bn_selected)),
                fillOutlineColor(ContextCompat.getColor(mAppContext, R.color.color_bn_selected)),
                fillOpacity(0.2f),
            )
            if (style.getLayer(TURF_CALCULATION_FILL_LAYER_ID) == null) {
                style.addLayerBelow(fillLayer, CIRCLE_CENTER_LAYER_ID)
                style.addLayer(lineLayer)
            }
        }
    }

    /**
     * Update the [FillLayer] based on the GeoJSON retrieved via
     * [.getTurfPolygon].
     *
     * @param circleCenter the center coordinate to be used in the Turf calculation.
     */
    private fun drawPolygonCircle(circleCenter: Point) {
        mMapboxMap?.getStyle { style ->
            // Use Turf to calculate the Polygon's coordinates
            val polygonArea: Polygon = getTurfPolygon(circleCenter, mCircleRadius.toDouble())
            val pointList = TurfMeta.coordAll(polygonArea, false)

            // Update the source's GeoJSON to draw a new fill circle
            val polygonCircleSource =
                style.getSourceAs<GeoJsonSource>(TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID)
            polygonCircleSource?.setGeoJson(
                Polygon.fromOuterInner(
                    LineString.fromLngLats(pointList),
                ),
            )

            // Update the source's GeoJSON to draw a new circle
            val markerSource =
                style.getSourceAs<GeoJsonSource>(TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID)
            markerSource?.setGeoJson(Polygon.fromOuterInner(LineString.fromLngLats(pointList)))

            // Adjust camera bounds to include entire circle
            val latLngList: MutableList<LatLng> = ArrayList(pointList.size)
            for (singlePoint in pointList) {
                latLngList.add(LatLng(singlePoint.latitude(), singlePoint.longitude()))
            }
            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder()
                        .includes(latLngList)
                        .build(),
                    CAMERA_TOP_RIGHT_LEFT_PADDING,
                    CAMERA_TOP_RIGHT_LEFT_PADDING,
                    CAMERA_TOP_RIGHT_LEFT_PADDING,
                    CAMERA_BOTTOM_PADDING,
                ),
                CAMERA_DURATION_1500,
            )
        }
    }

    /**
     * Use the Turf library {@link TurfTransformation#circle(Point, double, int, String)} method to
     * retrieve a {@link Polygon} .
     *
     * @param centerPoint a {@link Point} which the circle will center around
     * @param radius      the radius of the circle
     * @return a Polygon which represents the newly created circle
     */
    private fun getTurfPolygon(
        centerPoint: Point,
        radius: Double,
    ): Polygon {
        return TurfTransformation.circle(centerPoint, radius, 360, mCircleUnit)
    }

    fun adjustRadius(progress: Int?) {
        progress?.let { circleProgress ->
            mCircleRadius = if (circleProgress == 0) {
                10
            } else {
                circleProgress
            }
            upDateSeekbarText(mCircleRadius)
            drawPolygonCircle(mLastClickPoint)
        }
    }

    private fun upDateSeekbarText(radius: Int) {
        val isMetric = Units.isMetric(mPrefrenceManager?.getValue(KEY_UNIT_SYSTEM, ""))
        var seekbarText = ""
        if (isMetric) {
            seekbarText = Units.getMetricsNew(radius.toDouble(), true)
        } else {
            seekbarText = Units.getMetricsNew(Units.meterToFeet(radius.toDouble()), false)
        }
        mTvSeekBar?.text = seekbarText
    }

    fun mapClick(mapClickLatLng: LatLng) {
        mDefaultLatLng = mapClickLatLng
        mGeofenceMapLatLngInterface?.getMapLatLng(mapClickLatLng)
        if (!mIsDefaultGeofence) {
            setDefaultIconWithGeofence()
        }
        mMapboxMap?.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng))
        drawGeofence(fromLngLat(mapClickLatLng.longitude, mapClickLatLng.latitude))
    }

    fun drawFillCircle(mapClickLatLng: LatLng) {
        mDefaultLatLng = mapClickLatLng
        if (!mIsDefaultGeofence) {
            setDefaultIconWithGeofence()
        }
        mMapboxMap?.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng))
        drawGeofence(fromLngLat(mapClickLatLng.longitude, mapClickLatLng.latitude))
    }

    fun setGeofence() {
        getLiveLocation()?.let { mapClickLatLng ->
            mDefaultLatLng = mapClickLatLng
            mMapboxMap?.easeCamera(CameraUpdateFactory.newLatLng(mapClickLatLng))
            drawGeofence(fromLngLat(mapClickLatLng.longitude, mapClickLatLng.latitude))
        }
    }

    fun editGeofence() {
        mMapboxMap?.easeCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().zoom(MapCameraZoom.DEFAULT_CAMERA_ZOOM)
                    .target(mDefaultLatLng)
                    .build(),
            ),
            CAMERA_DURATION_1000,
        )
        drawGeofence(fromLngLat(mDefaultLatLng.longitude, mDefaultLatLng.latitude))
    }

    private fun drawGeofence(mapPoint: Point) {
        mLastClickPoint = mapPoint
        moveCircleCenterMarker(mLastClickPoint)
        drawPolygonCircle(mLastClickPoint)
    }

    /**
     * Move the red marker icon to wherever the map was tapped on.
     *
     * @param circleCenter where the red marker icon will be moved to.
     */
    private fun moveCircleCenterMarker(circleCenter: Point) {
        mMapboxMap?.getStyle { style ->
            val markerSource = style.getSourceAs<GeoJsonSource>(CIRCLE_CENTER_SOURCE_ID)
            markerSource?.setGeoJson(circleCenter)
        }
    }

    fun removeMapClickListener() {
        mMapboxMap?.style?.removeSource(CIRCLE_CENTER_SOURCE_ID)
        mMapboxMap?.style?.removeLayer(CIRCLE_CENTER_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_FILL_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_LINE_LAYER_ID)
    }

    fun clearGeofence() {
        mIsDefaultGeofence = false
        mMapboxMap?.style?.removeSource(CIRCLE_CENTER_SOURCE_ID)
        mMapboxMap?.style?.removeLayer(CIRCLE_CENTER_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_FILL_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_LINE_LAYER_ID)
    }

    interface GeofenceMapLatLngInterface {
        fun getMapLatLng(latLng: LatLng)
    }
}
