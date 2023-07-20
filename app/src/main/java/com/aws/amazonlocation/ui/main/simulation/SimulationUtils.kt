package com.aws.amazonlocation.ui.main.simulation

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.services.geo.AmazonLocationClient
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.RouteSimulationData
import com.aws.amazonlocation.data.response.RouteSimulationDataItem
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import com.aws.amazonlocation.data.response.SimulationHistoryData
import com.aws.amazonlocation.data.response.SimulationHistoryInnerData
import com.aws.amazonlocation.databinding.BottomSheetTrackSimulationBinding
import com.aws.amazonlocation.domain.`interface`.SimulationInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE
import com.aws.amazonlocation.utils.DELAY_1000
import com.aws.amazonlocation.utils.GeofenceCons
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.simulationCollectionName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class SimulationUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private var routeData: RouteSimulationData? = null
    private var notificationId: Int = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var isBus1TrackerFinish: Boolean = false
    private var isBus2TrackerFinish: Boolean = false
    private var isBus3TrackerFinish: Boolean = false
    private var isBus4TrackerFinish: Boolean = false
    private var isBus5TrackerFinish: Boolean = false
    private var isBus6TrackerFinish: Boolean = false
    private var isBus7TrackerFinish: Boolean = false
    private var isBus8TrackerFinish: Boolean = false
    private var isBus9TrackerFinish: Boolean = false
    private var isBus10TrackerFinish: Boolean = false
    private var isBus1TrackerNotificationEnable: Boolean = false
    private var isBus2TrackerNotificationEnable: Boolean = false
    private var isBus3TrackerNotificationEnable: Boolean = false
    private var isBus4TrackerNotificationEnable: Boolean = false
    private var isBus5TrackerNotificationEnable: Boolean = false
    private var isBus6TrackerNotificationEnable: Boolean = false
    private var isBus7TrackerNotificationEnable: Boolean = false
    private var isBus8TrackerNotificationEnable: Boolean = false
    private var isBus9TrackerNotificationEnable: Boolean = false
    private var isBus10TrackerNotificationEnable: Boolean = false
    private var mqttManager: AWSIotMqttManager? = null
    private var adapter: SimulationTrackingListAdapter? = null
    private var adapterSimulation: SimulationNotificationAdapter? = null
    private var mBottomSheetTrackingBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var mBindingTracking: BottomSheetTrackSimulationBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var simulationInterface: SimulationInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mMapboxMap: MapboxMap? = null
    private var mClient: AmazonLocationClient? = null
    private var mActivity: Activity? = null
    private var mIsLocationUpdateEnable = false
    private var trackingHistoryData = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus1Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus2Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus3Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus4Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus5Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus6Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus7Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus8Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus9Data = arrayListOf<SimulationHistoryData>()
    private var trackingHistoryBus10Data = arrayListOf<SimulationHistoryData>()
    private val notificationData = arrayListOf<NotificationData>()
    private val mCircleUnit: String = TurfConstants.UNIT_METERS
    private var mIsDefaultGeofence = false
    private var mGeofenceList = ArrayList<ListGeofenceResponseEntry>()
    private var simulationUpdate = HashMap<Int, Int>()
    private var selectedTrackerIndex = 0
    private var isNewBusSelected = false
    private var geofenceDataCount = 0

    fun setMapBox(
        activity: Activity,
        mapboxMap: MapboxMap,
        mMapHelper: MapHelper
    ) {
        mClient = AmazonLocationClient(AWSMobileClient.getInstance())
        this.mMapHelper = mMapHelper
        this.mMapboxMap = mapboxMap
        this.mActivity = activity
    }

    fun showSimulationBottomSheet() {
        mBottomSheetTrackingBehavior?.isHideable = false
        mBottomSheetTrackingBehavior?.isFitToContents = false
        mBindingTracking?.clTracking?.context?.let {
            if ((activity as MainActivity).isTablet) {
                mBottomSheetTrackingBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_280)
                mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                mBottomSheetTrackingBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_250)
                mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        simulationInterface?.getGeofenceList()
        createNotificationChannel()
        // Define your bounds coordinates
        val bounds = LatLngBounds.Builder()
            .include(LatLng(49.3097, -123.0229)) // Northeast corner
            .include(LatLng(49.1999, -123.2246)) // Southwest corner
            .build()

        // Set the bounds to restrict the visible area on the map
        mMapboxMap?.limitViewToBounds(bounds)
        mMapboxMap?.setMinZoomPreference(10.0)
        (activity as MainActivity).lifecycleScope.launch {
            delay(CLICK_DEBOUNCE)
            mMapHelper?.simulationZoomCamera(LatLng(49.2827, -123.1207))
        }
    }

    private fun MapboxMap.limitViewToBounds(bounds: LatLngBounds) {
        val newBoundsHeight =
            bounds.latitudeSpan - projection.visibleRegion.latLngBounds.latitudeSpan
        val newBoundsWidth =
            bounds.longitudeSpan - projection.visibleRegion.latLngBounds.longitudeSpan
        val leftTopLatLng = LatLng(
            bounds.latNorth - (bounds.latitudeSpan - newBoundsHeight) / 2,
            bounds.lonEast - (bounds.longitudeSpan - newBoundsWidth) / 2 - newBoundsWidth
        )
        val rightBottomLatLng = LatLng(
            bounds.latNorth - (bounds.latitudeSpan - newBoundsHeight) / 2 - newBoundsHeight,
            bounds.lonEast - (bounds.longitudeSpan - newBoundsWidth) / 2
        )
        val newBounds = LatLngBounds.Builder()
            .include(leftTopLatLng)
            .include(rightBottomLatLng)
            .build()
        setLatLngBoundsForCameraTarget(newBounds)
    }

    private fun initData() {
        mFragmentActivity?.applicationContext?.let {
            val assetManager: AssetManager = it.assets
            val inputStream: InputStream = assetManager.open("route_data.json")
            val inputStreamReader = InputStreamReader(inputStream)

            // Use Gson to convert the JSON data to a Person object
            val gson = Gson()
            routeData =
                gson.fromJson(inputStreamReader, RouteSimulationData::class.java)

            val bus1Coordinates = arrayListOf<Point>()
            val bus2Coordinates = arrayListOf<Point>()
            val bus3Coordinates = arrayListOf<Point>()
            val bus4Coordinates = arrayListOf<Point>()
            val bus5Coordinates = arrayListOf<Point>()
            val bus6Coordinates = arrayListOf<Point>()
            val bus7Coordinates = arrayListOf<Point>()
            val bus8Coordinates = arrayListOf<Point>()
            val bus9Coordinates = arrayListOf<Point>()
            val bus10Coordinates = arrayListOf<Point>()
            routeData?.let { route ->
                mActivity?.let { activity1 ->
                    route.forEachIndexed { index, routeSimulationDataItem ->
                        addMarkerSimulation(activity1, index, routeSimulationDataItem)
                    }
                }
                coroutineScope.launch {
                    while (isActive) {
                        if (mIsLocationUpdateEnable) {
                            val trackingHistorySize = trackingHistoryData.size
                            if (!isBus1TrackerFinish) {
                                simulationUpdate[0]?.let { update ->
                                    route[0].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus1TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[0],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        0
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus1Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus1Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus1Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus1Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus1Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 0,
                                                            "sourceId" + "tracker" + 0
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[0]?.plus(1) ?: 0
                                                simulationUpdate[0] = updatedValue
                                            }
                                        } else {
                                            isBus1TrackerFinish = true
                                            simulationUpdate[0] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus2TrackerFinish) {
                                simulationUpdate[1]?.let { update ->
                                    route[1].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus2TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[1],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        1
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus2Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus2Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus2Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus2Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus2Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 1,
                                                            "sourceId" + "tracker" + 1
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[1]?.plus(1) ?: 0
                                                simulationUpdate[1] = updatedValue
                                            }
                                        } else {
                                            isBus2TrackerFinish = true
                                            simulationUpdate[1] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus3TrackerFinish) {
                                simulationUpdate[2]?.let { update ->
                                    route[2].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus3TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[2],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        2
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus3Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus3Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus3Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus3Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus3Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 2,
                                                            "sourceId" + "tracker" + 2
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[2]?.plus(1) ?: 0
                                                simulationUpdate[2] = updatedValue
                                            }
                                        } else {
                                            isBus3TrackerFinish = true
                                            simulationUpdate[2] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus4TrackerFinish) {
                                simulationUpdate[3]?.let { update ->
                                    route[3].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus4TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[3],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        3
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus4Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus4Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus4Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus4Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus4Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 3,
                                                            "sourceId" + "tracker" + 3
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[3]?.plus(1) ?: 0
                                                simulationUpdate[3] = updatedValue
                                            }
                                        } else {
                                            isBus4TrackerFinish = true
                                            simulationUpdate[3] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus5TrackerFinish) {
                                simulationUpdate[4]?.let { update ->
                                    route[4].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus5TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[4],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        4
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus5Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus5Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus5Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus5Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus5Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 4,
                                                            "sourceId" + "tracker" + 4
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[4]?.plus(1) ?: 0
                                                simulationUpdate[4] = updatedValue
                                            }
                                        } else {
                                            isBus5TrackerFinish = true
                                            simulationUpdate[4] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus6TrackerFinish) {
                                simulationUpdate[5]?.let { update ->
                                    route[5].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus6TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[5],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        5
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus6Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus6Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus6Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus6Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus6Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 5,
                                                            "sourceId" + "tracker" + 5
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[5]?.plus(1) ?: 0
                                                simulationUpdate[5] = updatedValue
                                            }
                                        } else {
                                            isBus6TrackerFinish = true
                                            simulationUpdate[5] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus7TrackerFinish) {
                                simulationUpdate[6]?.let { update ->
                                    route[6].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus7TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[6],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        6
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus7Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus7Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus7Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus7Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus7Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 6,
                                                            "sourceId" + "tracker" + 6
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[6]?.plus(1) ?: 0
                                                simulationUpdate[6] = updatedValue
                                            }
                                        } else {
                                            isBus7TrackerFinish = true
                                            simulationUpdate[6] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus8TrackerFinish) {
                                simulationUpdate[7]?.let { update ->
                                    route[7].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus8TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[7],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        7
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus8Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus8Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus8Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus8Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus8Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 7,
                                                            "sourceId" + "tracker" + 7
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[7]?.plus(1) ?: 0
                                                simulationUpdate[7] = updatedValue
                                            }
                                        } else {
                                            isBus8TrackerFinish = true
                                            simulationUpdate[7] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus9TrackerFinish) {
                                simulationUpdate[8]?.let { update ->
                                    route[8].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus9TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[8],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        8
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus9Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus9Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus9Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus9Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus9Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 8,
                                                            "sourceId" + "tracker" + 8
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[8]?.plus(1) ?: 0
                                                simulationUpdate[8] = updatedValue
                                            }
                                        } else {
                                            isBus9TrackerFinish = true
                                            simulationUpdate[8] = 0
                                        }
                                    }
                                }
                            }
                            if (!isBus10TrackerFinish) {
                                simulationUpdate[9]?.let { update ->
                                    route[9].coordinates?.let { lists ->
                                        if (lists.size > update) {
                                            lists[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point[1],
                                                        point[0]
                                                    )
                                                    if (isBus10TrackerNotificationEnable) {
                                                        val position = arrayListOf<Double>()
                                                        position.add(latLng.longitude)
                                                        position.add(latLng.latitude)
                                                        simulationInterface?.evaluateGeofence(
                                                            simulationCollectionName[9],
                                                            position
                                                        )
                                                    }
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        9
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point[0],
                                                            point[1]
                                                        )
                                                    bus10Coordinates.add(latLngPoint)
                                                    val positionData: String =
                                                        when (bus10Coordinates.size) {
                                                            1 -> {
                                                                it.getString(R.string.label_position_start)
                                                            }
                                                            lists.size -> {
                                                                it.getString(R.string.label_position_end)
                                                            }
                                                            else -> {
                                                                it.getString(R.string.label_position_data)
                                                            }
                                                        }
                                                    trackingHistoryBus10Data.add(
                                                        SimulationHistoryData(
                                                            positionData,
                                                            false,
                                                            0,
                                                            SimulationHistoryInnerData(
                                                                point[1],
                                                                point[0],
                                                                Date()
                                                            )
                                                        )
                                                    )
                                                    if (bus10Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus10Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 9,
                                                            "sourceId" + "tracker" + 9
                                                        )
                                                    }
                                                }
                                                val updatedValue =
                                                    simulationUpdate[9]?.plus(1) ?: 0
                                                simulationUpdate[9] = updatedValue
                                            }
                                        } else {
                                            isBus10TrackerFinish = true
                                            simulationUpdate[9] = 0
                                        }
                                    }
                                }
                            }
                            trackingHistoryData.clear()
                            when (selectedTrackerIndex) {
                                0 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus1Data)
                                }
                                1 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus2Data)
                                }
                                2 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus3Data)
                                }
                                3 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus4Data)
                                }
                                4 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus5Data)
                                }
                                5 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus6Data)
                                }
                                6 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus7Data)
                                }
                                7 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus8Data)
                                }
                                8 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus9Data)
                                }
                                9 -> {
                                    trackingHistoryData.addAll(trackingHistoryBus10Data)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                if (isNewBusSelected) {
                                    isNewBusSelected = false
                                    adapter?.notifyItemRangeChanged(0, trackingHistoryData.size)
                                } else {
                                    adapter?.notifyItemRangeInserted(trackingHistorySize, 1)
                                }
                            }
                        }
                        delay(DELAY_1000)
                    }
                }
            }
        }
    }

    private fun addMarkerSimulation(
        activity1: Activity,
        index: Int,
        routeSimulationDataItem: RouteSimulationDataItem
    ) {
        routeSimulationDataItem.coordinates?.get(0)?.get(1)?.let { latitude ->
            routeSimulationDataItem.coordinates?.get(0)?.get(0)?.let { longitude ->
                mMapHelper?.addMarkerSimulation(
                    "tracker$index",
                    activity1,
                    LatLng(latitude, longitude),
                    index
                )
            }
        }
    }

    fun initSimulationView(
        fragmentActivity: FragmentActivity?,
        bottomSheetTrackSimulationBinding: BottomSheetTrackSimulationBinding,
        simulationInterface1: SimulationInterface
    ) {
        this.simulationInterface = simulationInterface1
        this.mFragmentActivity = fragmentActivity
        this.mBindingTracking = bottomSheetTrackSimulationBinding
        initSimulationBottomSheet()
    }

    private fun initSimulationBottomSheet() {
        mBindingTracking?.apply {
            notificationData.clear()
            notificationData.add(NotificationData("Bus 01 Macdonald", false))
            notificationData.add(NotificationData("Bus 02 Main", false))
            notificationData.add(NotificationData("Bus 03 Robson", false))
            notificationData.add(NotificationData("Bus 04 Davie", false))
            notificationData.add(NotificationData("Bus 05 Fraser", false))
            notificationData.add(NotificationData("Bus 06 Granville", false))
            notificationData.add(NotificationData("Bus 07 Downtown, Oak", false))
            notificationData.add(NotificationData("Bus 08 Victoria", false))
            notificationData.add(NotificationData("Bus 09 Knight", false))
            notificationData.add(NotificationData("Bus 10 UBC", false))

            simulationUpdate.clear()
            simulationUpdate[0] = 0
            simulationUpdate[1] = 0
            simulationUpdate[2] = 0
            simulationUpdate[3] = 0
            simulationUpdate[4] = 0
            simulationUpdate[5] = 0
            simulationUpdate[6] = 0
            simulationUpdate[7] = 0
            simulationUpdate[8] = 0
            simulationUpdate[9] = 0

            mBottomSheetTrackingBehavior = BottomSheetBehavior.from(root)
            mBottomSheetTrackingBehavior?.isHideable = true
            mBottomSheetTrackingBehavior?.isDraggable = true
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetTrackingBehavior?.isFitToContents = false
            mBottomSheetTrackingBehavior?.halfExpandedRatio = 0.6f

            mBottomSheetTrackingBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                imgAmazonLogoTrackingSheet.alpha = 1f
                                ivAmazonInfoTrackingSheet.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoTrackingSheet.alpha = 0f
                                ivAmazonInfoTrackingSheet.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoTrackingSheet.alpha = 1f
                                ivAmazonInfoTrackingSheet.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
            initClick()
            initAdapter()
            setSpinnerData()
            setSelectedNotificationCount()
        }
    }

    private val CHANNEL_ID = "my_channel_simulation"
    private val CHANNEL_NAME = "simulation Notification Channel"
    private val GROUP_KEY_WORK_SIMULATION = BuildConfig.APPLICATION_ID + "SIMULATION"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                        description = CHANNEL_NAME
                    }
                    // Register the channel with the system
                    val notificationManager: NotificationManager =
                        it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }
    }

    // Function to check if a notification group exists
    private fun isNotificationGroupActive(context: Context, groupId: String): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val activeNotifications = notificationManager.activeNotifications

        // Check if any of the active notifications belong to the specified group
        for (notification in activeNotifications) {
            val groupKey = notification.notification.group
            if (groupKey == groupId) {
                return true // Group is still active
            }
        }
        return false // Group is not active or device is below API 23
    }

    private fun showNotification(
        notificationId: Int,
        title: String,
        subTitle: String,
        setGroupSummary: Boolean
    ) {
        mActivity?.let {
            val builder = NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(subTitle)
                .setGroupSummary(setGroupSummary)
                .setGroup(GROUP_KEY_WORK_SIMULATION)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

            NotificationManagerCompat.from(it).apply {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder)
            }
        }
    }

    private fun drawSimulationPolygonCircle(circleCenter: Point, radius: Int, index: String) {
        mMapboxMap?.getStyle { style ->
            // Use Turf to calculate the Polygon's coordinates
            val polygonArea: Polygon = getTurfPolygon(circleCenter, radius.toDouble())
            val pointList = TurfMeta.coordAll(polygonArea, false)

            // Update the source's GeoJSON to draw a new fill circle
            val polygonCircleSource =
                style.getSourceAs<GeoJsonSource>(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index)
            polygonCircleSource?.setGeoJson(
                Polygon.fromOuterInner(
                    LineString.fromLngLats(pointList)
                )
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

    private fun setSpinnerData() {
        mBindingTracking?.apply {
            val adapter = ChangeBusSpinnerAdapter(spinnerChangeBus.context, notificationData)
            spinnerChangeBus.adapter = adapter

            spinnerChangeBus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (selectedTrackerIndex != position) {
                        selectedTrackerIndex = position
                        isNewBusSelected = true
                    }
                    val selectedData = notificationData[selectedTrackerIndex].name
                    tvChangeRoute.text =
                        buildString {
                            append(selectedData.split(" ")[0])
                            append(" ")
                            append(selectedData.split(" ")[1])
                        }
                    tvChangeRouteName.text = selectedData.split(" ")[2]
                    adapter.setSelection(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do something when nothing is selected
                }
            }
        }
    }

    private fun setDefaultIconWithGeofence(index: Int) {
        mMapboxMap?.getStyle { style ->
            if (style.getSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index") == null) {
                style.addSource(GeoJsonSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index"))
            }
            initPolygonCircleFillLayer(index)
        }
        mIsDefaultGeofence = true
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer(index: Int) {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index",
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index"
            )

            mFragmentActivity?.applicationContext?.let {
                fillLayer.setProperties(
                    PropertyFactory.fillColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_bn_selected
                        )
                    ),
                    PropertyFactory.fillOutlineColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_bn_selected
                        )
                    ),
                    PropertyFactory.fillOpacity(0.2f)
                )
            }

            if (style.getLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index") == null) {
                style.addLayerBelow(fillLayer, GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index")
            }
        }
    }

    private fun initClick() {
        mBindingTracking?.apply {
            cardStartTracking.setOnClickListener {
                if (mIsLocationUpdateEnable) {
                    mActivity?.getColor(R.color.color_primary_green)
                        ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
                    tvStopTracking.text = mActivity?.getText(R.string.label_start_tracking)
                    tvTrackingYourActivity.text =
                        mActivity?.getText(R.string.label_not_tracking_your_activity)
                    tvTrackingYourActivity.context?.let {
                        tvTrackingYourActivity.setTextColor(
                            ContextCompat.getColor(
                                it,
                                R.color.color_hint_text
                            )
                        )
                    }
                    stopMqttManager()
                } else {
                    viewLoader.show()
                    tvStopTracking.hide()
                    cardStartTracking.isEnabled = false
                    startMqttManager()
                }
            }
            ivBackArrowRouteNotifications.setOnClickListener {
                if (rvRouteNotifications.isVisible) {
                    hideViews(rvRouteNotifications, viewDividerNotification)
                    ivBackArrowRouteNotifications.rotation = 0f
                } else {
                    showViews(rvRouteNotifications, viewDividerNotification)
                    ivBackArrowRouteNotifications.rotation = 180f
                }
            }

            ivBackArrowChangeRoute.setOnClickListener {
                if (rvTracking.isVisible) {
                    hideViews(rvTracking, viewDividerBus)
                    ivBackArrowChangeRoute.rotation = 0f
                } else {
                    showViews(rvTracking, viewDividerBus)
                    ivBackArrowChangeRoute.rotation = 180f
                }
            }
        }
    }

    private fun stopMqttManager() {
        mIsLocationUpdateEnable = false
        try {
            mqttManager?.unsubscribeTopic("${mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId}/tracker")
        } catch (_: Exception) {
        }

        try {
            mqttManager?.disconnect()
        } catch (_: Exception) {
        }
        mqttManager = null
    }

    private fun startMqttManager() {
        if (mqttManager != null) stopMqttManager()
        val identityId: String =
            BuildConfig.DEFAULT_IDENTITY_POOL_ID

        mqttManager =
            AWSIotMqttManager(
                identityId,
                BuildConfig.SIMULATION_WEB_SOCKET_URL
            )
        mqttManager?.isAutoReconnect =
            false // To be able to display Exceptions and debug the problem.
        mqttManager?.keepAlive = 60
        mqttManager?.setCleanSession(true)

        try {
            val instance = mAWSLocationHelper.getCognitoCachingCredentialsProvider()
            mqttManager?.connect(instance) { status, throwable ->
                runOnUiThread {
                    when (status) {
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting -> {
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                            startTracking()
                            subscribeTopic(identityId)
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> {
                        }
                        AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                            throwable?.printStackTrace()
                        }
                        else -> {
                            mBindingTracking?.apply {
                                viewLoader.hide()
                                tvStopTracking.show()
                                cardStartTracking.isEnabled = true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTracking() {
        mBindingTracking?.apply {
            mIsLocationUpdateEnable = true
            mActivity?.getColor(R.color.color_red)
                ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
            tvStopTracking.text =
                mActivity?.getText(R.string.label_stop_tracking)
            tvTrackingYourActivity.text =
                mActivity?.getText(R.string.label_tracking_your_activity)
            tvTrackingYourActivity.context.let {
                tvTrackingYourActivity.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.color_red
                    )
                )
            }
            if (trackingHistoryData.size == 0) {
                initData()
            }
            viewLoader.hide()
            tvStopTracking.show()
            cardStartTracking.isEnabled = true
        }
    }

    private fun subscribeTopic(identityId: String) {
        try {
            mqttManager?.subscribeToTopic(
                "$identityId/tracker",
                AWSIotMqttQos.QOS0
            ) { _, data ->

                val stringData = String(data)
                if (stringData.isNotEmpty()) {
                    val jsonObject = JsonParser.parseString(stringData).asJsonObject
                    val type = jsonObject.get("trackerEventType").asString
                    val geofenceName = jsonObject.get("geofenceId").asString
                    val geofenceCollection = jsonObject.get("geofenceCollection").asString
                    var subTitle = ""
                    if (type.equals("ENTER", true)) {
                        subTitle = "Entered $geofenceName geofence"
                        var selectedIndex = 0
                        var busStopCount = 0
                        routeData?.forEachIndexed { index, routeSimulationDataItem ->
                            if (routeSimulationDataItem.geofenceCollection == geofenceCollection) {
                                selectedIndex = index
                                busStopCount = routeSimulationDataItem.busStopCount + 1
                                routeSimulationDataItem.busStopCount++
                                return@forEachIndexed
                            }
                        }
                        val trackingHistorySize = trackingHistoryData.size
                        mFragmentActivity?.applicationContext?.let {
                            when (selectedIndex) {
                                0 -> {
                                    trackingHistoryBus1Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                1 -> {
                                    trackingHistoryBus2Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                2 -> {
                                    trackingHistoryBus3Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                3 -> {
                                    trackingHistoryBus4Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                4 -> {
                                    trackingHistoryBus5Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                5 -> {
                                    trackingHistoryBus6Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                6 -> {
                                    trackingHistoryBus7Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                7 -> {
                                    trackingHistoryBus8Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                8 -> {
                                    trackingHistoryBus9Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                9 -> {
                                    trackingHistoryBus10Data.add(
                                        SimulationHistoryData(
                                            it.getString(R.string.label_position_data),
                                            true,
                                            busStopCount,
                                            SimulationHistoryInnerData(
                                                -123.121987,
                                                49.286464,
                                                Date()
                                            )
                                        )
                                    )
                                }
                                else -> {}
                            }
                        }
//                        trackingHistoryData.clear()
//                        when (selectedTrackerIndex) {
//                            0 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus1Data)
//                            }
//                            1 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus2Data)
//                            }
//                            2 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus3Data)
//                            }
//                            3 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus4Data)
//                            }
//                            4 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus5Data)
//                            }
//                            5 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus6Data)
//                            }
//                            6 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus7Data)
//                            }
//                            7 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus8Data)
//                            }
//                            8 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus9Data)
//                            }
//                            9 -> {
//                                trackingHistoryData.addAll(trackingHistoryBus10Data)
//                            }
//                        }
//                        adapter?.notifyItemRangeInserted(trackingHistorySize, 1)
                    } else {
                        subTitle = "Exited $geofenceName geofence"
                    }
                    notificationId++
                    mActivity?.let {
                        if (isNotificationGroupActive(it, GROUP_KEY_WORK_SIMULATION)) {
                            showNotification(notificationId, geofenceName, subTitle, false)
                        } else {
                            showNotification(notificationId, geofenceName, subTitle, true)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAdapter() {
        val notificationLayoutManager = LinearLayoutManager(mActivity?.applicationContext)
        adapterSimulation = SimulationNotificationAdapter(
            notificationData,
            object : SimulationNotificationAdapter.NotificationInterface {
                override fun click(position: Int, isSelected: Boolean) {
                    notificationData[position].isSelected = isSelected
                    when (position) {
                        0 -> {
                            isBus1TrackerNotificationEnable = isSelected
                        }
                        1 -> {
                            isBus2TrackerNotificationEnable = isSelected
                        }
                        2 -> {
                            isBus3TrackerNotificationEnable = isSelected
                        }
                        3 -> {
                            isBus4TrackerNotificationEnable = isSelected
                        }
                        4 -> {
                            isBus5TrackerNotificationEnable = isSelected
                        }
                        5 -> {
                            isBus6TrackerNotificationEnable = isSelected
                        }
                        6 -> {
                            isBus7TrackerNotificationEnable = isSelected
                        }
                        7 -> {
                            isBus8TrackerNotificationEnable = isSelected
                        }
                        8 -> {
                            isBus9TrackerNotificationEnable = isSelected
                        }
                        9 -> {
                            isBus10TrackerNotificationEnable = isSelected
                        }
                    }
                    setSelectedNotificationCount()
                }
            }
        )
        mBindingTracking?.rvRouteNotifications?.adapter = adapterSimulation
        mBindingTracking?.rvRouteNotifications?.layoutManager = notificationLayoutManager

        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
        adapter = SimulationTrackingListAdapter(trackingHistoryData)
        mBindingTracking?.rvTracking?.adapter = adapter
        mBindingTracking?.rvTracking?.layoutManager = layoutManager
    }

    private fun setSelectedNotificationCount() {
        var totalCount = 0
        if (isBus1TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus2TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus3TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus4TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus5TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus6TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus7TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus8TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus9TrackerNotificationEnable) {
            totalCount++
        }
        if (isBus10TrackerNotificationEnable) {
            totalCount++
        }
        mBindingTracking?.apply {
            tvRouteNotificationsName.text = buildString {
                append(totalCount)
                append(" ")
                append(tvRouteNotificationsName.context.getString(R.string.routes_active))
            }
        }
    }

    fun hideSimulationBottomSheet() {
        mBottomSheetTrackingBehavior.let {
            coroutineScope.cancel()
            (activity as MainActivity).lifecycleScope.launch {
                delay(DELAY_1000)
                notificationData.forEachIndexed { index, _ ->
                    mMapHelper?.removeSource("sourceIdtracker$index")
                    mMapHelper?.removeSource("source-idtracker$index")
                    mMapHelper?.removeLayer("layerIdtracker$index")
                    mMapHelper?.removeLayer("layer-idtracker$index")
                }
                mMapHelper?.clearMarker()
                mMapHelper?.removeLine()
                mMapHelper?.removeSimulationData()
                mGeofenceList.clear()
                notificationData.clear()
                geofenceDataCount = 0
                notificationId = 1
                isBus1TrackerFinish = false
                isBus2TrackerFinish = false
                isBus3TrackerFinish = false
                isBus4TrackerFinish = false
                isBus5TrackerFinish = false
                isBus6TrackerFinish = false
                isBus7TrackerFinish = false
                isBus8TrackerFinish = false
                isBus9TrackerFinish = false
                isBus10TrackerFinish = false
                isBus1TrackerNotificationEnable = false
                isBus2TrackerNotificationEnable = false
                isBus3TrackerNotificationEnable = false
                isBus4TrackerNotificationEnable = false
                isBus5TrackerNotificationEnable = false
                isBus6TrackerNotificationEnable = false
                isBus7TrackerNotificationEnable = false
                isBus8TrackerNotificationEnable = false
                isBus9TrackerNotificationEnable = false
                isBus10TrackerNotificationEnable = false
            }
            mGeofenceList.forEachIndexed { index, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index")
            }
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
            stopMqttManager()
        }
    }

    fun manageGeofenceListUI(dataGeofence: SimulationGeofenceData) {
        if (geofenceDataCount == simulationCollectionName.size) {
            mGeofenceList.clear()
        }
        geofenceDataCount++
        mGeofenceList.addAll(dataGeofence.devicePositionData)
        if (geofenceDataCount == simulationCollectionName.size) {
            val mLatLngList = arrayListOf<LatLng>()
            if (mGeofenceList.isNotEmpty()) {
                mGeofenceList.forEachIndexed { index, data ->
                    val latLng =
                        LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
                    setDefaultIconWithGeofence(index)
                    mLatLngList.add(latLng)
                    drawSimulationPolygonCircle(
                        Point.fromLngLat(latLng.longitude, latLng.latitude),
                        data.geometry.circle.radius.toInt(),
                        index.toString()
                    )
                }
            }
        }
    }
}
