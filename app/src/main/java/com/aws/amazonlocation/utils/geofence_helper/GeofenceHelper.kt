package com.aws.amazonlocation.utils.geofence_helper // ktlint-disable package-name

import android.content.Context
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R
import com.aws.amazonlocation.utils.Durations.CAMERA_BOTTOM_PADDING
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1000
import com.aws.amazonlocation.utils.Durations.CAMERA_DURATION_1500
import com.aws.amazonlocation.utils.Durations.CAMERA_RIGHT_PADDING
import com.aws.amazonlocation.utils.Durations.CAMERA_TOP_RIGHT_LEFT_PADDING
import com.aws.amazonlocation.utils.Durations.DEFAULT_RADIUS
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_ICON_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_LAYER_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_CENTER_SOURCE_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_DRAGGABLE_BEARING
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_DRAGGABLE_VISIBLE_ICON_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID
import com.aws.amazonlocation.utils.GeofenceCons.CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID
import com.aws.amazonlocation.utils.GeofenceCons.GEOFENCE_MIN_RADIUS
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
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeasurement
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.aws.amazonlocation.utils.isGrabMapSelected
import com.aws.amazonlocation.utils.latNorth
import com.aws.amazonlocation.utils.latSouth
import com.aws.amazonlocation.utils.lonEast
import com.aws.amazonlocation.utils.lonWest
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
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
    private var mPrefrenceManager: PreferenceManager?
) {

    var mDefaultLatLng = LatLng(49.281174, -123.116823)
    val mDefaultLatLngGrab = LatLng(1.2840123, 103.8487542)
    private var mDefaultLocationPoint =
        fromLngLat(mDefaultLatLng.longitude, mDefaultLatLng.latitude)
    private val mCircleUnit: String = UNIT_METRES
    private var mLastClickPoint: Point = mDefaultLocationPoint
    var mCircleRadius = DEFAULT_RADIUS
    var mIsDefaultGeofence = false
    var isTablet = false

    private val invisibleMarkerDragListener = object : OnSymbolDragListener {
        override fun onAnnotationDragStarted(annotation: Symbol?) {
        }

        override fun onAnnotationDrag(annotation: Symbol?) {
            val symbolLatLng = annotation?.latLng
            symbolLatLng?.let {
                if (it.longitude > mLastClickPoint.longitude()) {
                    val distance = TurfMeasurement.distance(mLastClickPoint, fromLngLat(annotation.latLng.longitude, mLastClickPoint.latitude()), mCircleUnit)
                    if (distance >= GEOFENCE_MIN_RADIUS) {
                        mSeekBar?.progress = distance.toInt()
                    } else {
                        mSeekBar?.progress = GEOFENCE_MIN_RADIUS
                    }
                }
            }
        }

        override fun onAnnotationDragFinished(annotation: Symbol?) {
            annotation?.let {
                mSeekBar?.progress?.let { progress ->
                    TurfMeasurement.destination(mLastClickPoint, progress.toDouble(), CIRCLE_DRAGGABLE_BEARING, mCircleUnit).let {
                        if (annotation.latLng.longitude != it.longitude() && annotation.latLng.latitude != it.latitude()) {
                            mGeofenceMapLatLngInterface?.updateInvisibleDraggableMarker(LatLng(it.latitude(), it.longitude()))
                        }
                    }
                }
            }
        }
    }

    private fun getLiveLocation(): LatLng? {
        var mLatLng: LatLng? = null
        if (mMapboxMap?.locationComponent?.isLocationComponentActivated == true) {
            mMapboxMap?.locationComponent?.lastKnownLocation?.apply {
                mLatLng = LatLng(
                    latitude,
                    longitude
                )
            }
        }
        mPrefrenceManager?.let { preferenceManager ->
            if (isGrabMapSelected(preferenceManager, mAppContext)) {
                if (mLatLng != null) {
                    mLatLng?.let {
                        if (!(it.latitude in latSouth..latNorth && it.longitude in lonWest..lonEast)) {
                            return mDefaultLatLngGrab
                        }
                    }
                } else {
                    return mDefaultLatLngGrab
                }
            }
        }
        return if (mLatLng == null) {
            mPrefrenceManager?.let {
                if (isGrabMapSelected(it, mAppContext)) {
                    mDefaultLatLngGrab
                } else {
                    mDefaultLatLng
                }
            }
        } else {
            mLatLng
        }
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
            drawVisibleDraggableMarkerOnMap(style, mLastClickPoint, mCircleRadius)
            addInvisibleDraggablePoint(mLastClickPoint, mCircleRadius)

            mSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    adjustRadius(seekBar?.progress)
                    if (fromUser) {
                        TurfMeasurement.destination(mLastClickPoint, progress.toDouble(), CIRCLE_DRAGGABLE_BEARING, mCircleUnit).let {
                            mGeofenceMapLatLngInterface?.updateInvisibleDraggableMarker(LatLng(it.latitude(), it.longitude()))
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    adjustRadius(seekBar?.progress)
                    seekBar?.progress?.let { progress ->
                        var dragMarkerRadius = progress
                        if (dragMarkerRadius < GEOFENCE_MIN_RADIUS) {
                            dragMarkerRadius = GEOFENCE_MIN_RADIUS
                        }
                        TurfMeasurement.destination(mLastClickPoint, dragMarkerRadius.toDouble(), CIRCLE_DRAGGABLE_BEARING, mCircleUnit).let {
                            mGeofenceMapLatLngInterface?.updateInvisibleDraggableMarker(LatLng(it.latitude(), it.longitude()))
                        }
                    }
                }
            })
        }
        mIsDefaultGeofence = true
    }

    private fun drawMarkerOnMap(style: Style) {
        BitmapUtils.getBitmapFromDrawable(
            ContextCompat.getDrawable(
                mAppContext,
                R.drawable.ic_geofence_marker_1
            )
        )?.let {
            style.addImage(
                CIRCLE_CENTER_ICON_ID,
                it
            )
        }
        if (style.getSource(CIRCLE_CENTER_SOURCE_ID) == null) {
            style.addSource(
                GeoJsonSource(
                    CIRCLE_CENTER_SOURCE_ID,
                    Feature.fromGeometry(mDefaultLocationPoint)
                )
            )
        }

        if (style.getSource(CIRCLE_CENTER_LAYER_ID) == null && style.getLayer(
                CIRCLE_CENTER_LAYER_ID
            ) == null
        ) {
            style.addLayer(
                SymbolLayer(CIRCLE_CENTER_LAYER_ID, CIRCLE_CENTER_SOURCE_ID).withProperties(
                    iconImage(CIRCLE_CENTER_ICON_ID),
                    iconIgnorePlacement(false),
                    iconAllowOverlap(true),
                    textAllowOverlap(true),
                    iconAnchor(Property.ICON_ANCHOR_CENTER)
                )
            )
        }
    }

    private fun drawVisibleDraggableMarkerOnMap(style: Style, point: Point, radius: Int) {
        ContextCompat.getDrawable(
            mAppContext,
            R.drawable.ic_geofence_drag_thumb
        )?.let {
            style.addImage(
                CIRCLE_DRAGGABLE_VISIBLE_ICON_ID,
                it
            )
        }
        if (style.getSource(CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID) == null) {
            style.addSource(
                GeoJsonSource(
                    CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID,
                    Feature.fromGeometry(mDefaultLocationPoint)
                )
            )
        }

        if (style.getSource(CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID) == null && style.getLayer(
                CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID
            ) == null
        ) {
            style.addLayer(
                SymbolLayer(CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID, CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID).withProperties(
                    iconImage(CIRCLE_DRAGGABLE_VISIBLE_ICON_ID),
                    iconIgnorePlacement(false),
                    iconAllowOverlap(true),
                    textAllowOverlap(true),
                    iconAnchor(Property.ICON_ANCHOR_CENTER)
                )
            )
        } else {
            style.getSourceAs<GeoJsonSource>(CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID)?.apply {
                val pt = TurfMeasurement.destination(point, radius.toDouble(), CIRCLE_DRAGGABLE_BEARING, mCircleUnit)
                setGeoJson(pt)
            }
        }
    }

    private fun updateVisibleDraggableMarkerOnMap(radius: Int) {
        mMapboxMap?.getStyle {
            drawVisibleDraggableMarkerOnMap(it, mLastClickPoint, radius)
        }
    }

    private fun addInvisibleDraggablePoint(point: Point, radius: Int) {
        val pt = TurfMeasurement.destination(point, radius.toDouble(), CIRCLE_DRAGGABLE_BEARING, mCircleUnit)
        mGeofenceMapLatLngInterface?.addInvisibleDraggableMarker(LatLng(pt.latitude(), pt.longitude()), invisibleMarkerDragListener)
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer() {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                TURF_CALCULATION_FILL_LAYER_ID,
                TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID
            )

            val lineLayer = LineLayer(
                TURF_CALCULATION_LINE_LAYER_ID,
                TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID
            )

            lineLayer.setProperties(
                lineWidth(2f),
                lineColor(
                    ContextCompat.getColor(
                        mAppContext,
                        R.color.color_bn_selected
                    )
                )
            )

            fillLayer.setProperties(
                fillColor(ContextCompat.getColor(mAppContext, R.color.color_bn_selected)),
                fillOutlineColor(ContextCompat.getColor(mAppContext, R.color.color_bn_selected)),
                fillOpacity(0.2f)
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
                    LineString.fromLngLats(pointList)
                )
            )

            // Update the source's GeoJSON to draw a new circle
            val markerSource =
                style.getSourceAs<GeoJsonSource>(TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID)
            markerSource?.setGeoJson(Polygon.fromOuterInner(LineString.fromLngLats(pointList)))

            updateVisibleDraggableMarkerOnMap(mCircleRadius)

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
                    if (isTablet) CAMERA_BOTTOM_PADDING else CAMERA_TOP_RIGHT_LEFT_PADDING,
                    CAMERA_TOP_RIGHT_LEFT_PADDING,
                    if (isTablet) CAMERA_RIGHT_PADDING else CAMERA_TOP_RIGHT_LEFT_PADDING,
                    CAMERA_BOTTOM_PADDING
                ),
                CAMERA_DURATION_1500
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
        radius: Double
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
        seekbarText = if (isMetric) {
            Units.getMetricsNew(mAppContext, radius.toDouble(), true)
        } else {
            Units.getMetricsNew(mAppContext, Units.meterToFeet(radius.toDouble()), false)
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
        updateInvisibleDraggableMarker(mapClickLatLng)
    }

    private fun updateInvisibleDraggableMarker(center: LatLng) {
        val radius = mSeekBar?.progress?.toDouble()
        radius?.let {
            val point = TurfMeasurement.destination(fromLngLat(center.longitude, center.latitude), it, CIRCLE_DRAGGABLE_BEARING, mCircleUnit)
            mGeofenceMapLatLngInterface?.updateInvisibleDraggableMarker(LatLng(point.latitude(), point.longitude()))
        }
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
                    .build()
            ),
            CAMERA_DURATION_1000
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
        mMapboxMap?.style?.removeSource(CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID)
        mMapboxMap?.style?.removeLayer(CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_FILL_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_LINE_LAYER_ID)
        mGeofenceMapLatLngInterface?.deleteInvisibleDraggableMarker(invisibleMarkerDragListener)
    }

    fun clearGeofence() {
        mIsDefaultGeofence = false
        mMapboxMap?.style?.removeSource(CIRCLE_CENTER_SOURCE_ID)
        mMapboxMap?.style?.removeLayer(CIRCLE_CENTER_LAYER_ID)
        mMapboxMap?.style?.removeSource(CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID)
        mMapboxMap?.style?.removeLayer(CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_FILL_LAYER_ID)
        mMapboxMap?.style?.removeLayer(TURF_CALCULATION_LINE_LAYER_ID)
        mGeofenceMapLatLngInterface?.deleteInvisibleDraggableMarker(invisibleMarkerDragListener)
    }

    interface GeofenceMapLatLngInterface {
        fun getMapLatLng(latLng: LatLng)

        fun addInvisibleDraggableMarker(latLng: LatLng, listener: OnSymbolDragListener)

        fun deleteInvisibleDraggableMarker(listener: OnSymbolDragListener)

        fun updateInvisibleDraggableMarker(latLng: LatLng)
    }
}
