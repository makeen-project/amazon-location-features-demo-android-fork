package com.aws.amazonlocation.ui.main.simulation

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import com.aws.amazonlocation.data.response.BusRouteCoordinates
import com.aws.amazonlocation.data.response.NotificationSimulationData
import com.aws.amazonlocation.data.response.RouteSimulationDataItem
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import com.aws.amazonlocation.data.response.SimulationHistoryData
import com.aws.amazonlocation.data.response.SimulationHistoryInnerData
import com.aws.amazonlocation.databinding.BottomSheetTrackSimulationBinding
import com.aws.amazonlocation.domain.`interface`.SimulationInterface
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE
import com.aws.amazonlocation.utils.DELAY_1000
import com.aws.amazonlocation.utils.GeofenceCons
import com.aws.amazonlocation.utils.MapCameraZoom.SIMULATION_CAMERA_ZOOM_1
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units.readRouteData
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.notificationData
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.simulationCollectionName
import com.aws.amazonlocation.utils.simulationLatNorth
import com.aws.amazonlocation.utils.simulationLatSouth
import com.aws.amazonlocation.utils.simulationLonEast
import com.aws.amazonlocation.utils.simulationLonWest
import com.aws.amazonlocation.utils.vancouverLat
import com.aws.amazonlocation.utils.vancouverLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
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
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class SimulationUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private val CHANNEL_ID = "my_channel_simulation"
    private val CHANNEL_NAME = "simulation Notification Channel"
    private val GROUP_KEY_WORK_SIMULATION = BuildConfig.APPLICATION_ID + "SIMULATION"
    private var routeData: ArrayList<RouteSimulationDataItem>? = null
    private var notificationId: Int = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var mqttManager: AWSIotMqttManager? = null
    private var simulationTrackingListAdapter: SimulationListAdapter? = null
    private var simulationNotificationAdapter: SimulationNotificationAdapter? = null
    private var mBottomSheetSimulationBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var simulationBinding: BottomSheetTrackSimulationBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var simulationInterface: SimulationInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mMapboxMap: MapboxMap? = null
    private var mClient: AmazonLocationClient? = null
    private var mActivity: Activity? = null
    private var mIsLocationUpdateEnable = false
    private var simulationHistoryData = arrayListOf<SimulationHistoryData>()
    private val mCircleUnit: String = TurfConstants.UNIT_METERS
    private var mIsDefaultGeofence = false
    private var selectedTrackerIndex = 0
    private var timerCounts: MutableList<Int>? = null
    private var busesCoordinates = MutableList(notificationData.size) { mutableListOf<Point>() }
    private var busSimulationNotificationFlags: MutableList<Boolean>? = null
    private var busSimulationHistoryData = MutableList(notificationData.size) { mutableListOf<SimulationHistoryData>() }
    private var mGeofenceList = MutableList(notificationData.size) { mutableListOf<ListGeofenceResponseEntry>() }
    private var mPreDrawTrackerLine = MutableList(notificationData.size) { mutableListOf<Point>() }

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
        mBottomSheetSimulationBehavior?.isHideable = false
        mBottomSheetSimulationBehavior?.isFitToContents = false
        simulationBinding?.clPersistentBottomSheetSimulation?.show()
        simulationBinding?.clTracking?.context?.let {
            if ((activity as MainActivity).isTablet) {
                mBottomSheetSimulationBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_280)
                mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                mBottomSheetSimulationBehavior?.peekHeight =
                    it.resources.getDimensionPixelSize(R.dimen.dp_250)
                mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        simulationInterface?.getGeofenceList()
        createNotificationChannel()
        setBounds()
    }

    private fun setBounds() {
        // Define your bounds coordinates
        val bounds = LatLngBounds.Builder()
            .include(LatLng(simulationLatNorth, simulationLonEast)) // Northeast corner
            .include(LatLng(simulationLatSouth, simulationLonWest)) // Southwest corner
            .build()

        // Set the bounds to restrict the visible area on the map
        mMapboxMap?.limitViewToBounds(bounds)
        mMapboxMap?.setMinZoomPreference(SIMULATION_CAMERA_ZOOM_1)
        (activity as MainActivity).lifecycleScope.launch {
            delay(CLICK_DEBOUNCE)
            mMapHelper?.simulationZoomCamera(LatLng(vancouverLat, vancouverLng))
        }
    }

    private fun initData() {
        mFragmentActivity?.applicationContext?.let { context ->
            routeData = readRouteData(context)?.busRoutesData

            routeData?.let { route ->
                timerCounts = MutableList(route.size) { 0 }
                busSimulationNotificationFlags = MutableList(route.size) { false }
                busSimulationNotificationFlags?.set(0, true)
                notificationData[0].isSelected = true
                setSelectedNotificationCount()
                simulationNotificationAdapter?.notifyItemChanged(0)
                mActivity?.let { activity ->
                    getFirstCoordinates()?.forEachIndexed { index, busRouteCoordinates ->
                        if (isBusTrackerNotificationEnable(index)) {
                            addMarkerSimulation(activity, index, busRouteCoordinates)
                        }
                    }
                }
                drawGeofenceWithPosition(0)
                addPreDrawTrackerLine(route, 0)
                zoomCamera()
                coroutineScope.launch {
                    while (isActive) {
                        if (mIsLocationUpdateEnable) {
                            getNextCoordinates()?.forEachIndexed { index, busRouteCoordinates ->
                                if (busRouteCoordinates.isUpdateNeeded) {
                                    busRouteCoordinates.coordinates?.let { list ->
                                        updateSimulationLocation(
                                            busRouteCoordinates.id,
                                            index,
                                            list,
                                            context,
                                            route.size
                                        )
                                    }
                                }
                            }
                            simulationHistoryData.clear()
                            simulationHistoryData.addAll(
                                getSelectedBusTrackingData(
                                    selectedTrackerIndex
                                )
                            )
                            withContext(Dispatchers.Main) {
                                simulationTrackingListAdapter?.submitList(simulationHistoryData.toMutableList())
                            }
                        }
                        delay(DELAY_1000)
                    }
                }
            }
        }
    }

    private fun drawGeofenceWithPosition(position: Int) {
        val mLatLngList = arrayListOf<LatLng>()
        if (mGeofenceList.isNotEmpty()) {
            mGeofenceList[position].forEachIndexed { index, data ->
                val latLng =
                    LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
                setDefaultIconWithGeofence("$position$index")
                mLatLngList.add(latLng)
                drawSimulationPolygonCircle(
                    Point.fromLngLat(latLng.longitude, latLng.latitude),
                    data.geometry.circle.radius.toInt(),
                    "$position$index"
                )
            }
        }
    }

    private fun removeGeofenceWithPosition(position: Int) {
        if (mGeofenceList.isNotEmpty()) {
            mGeofenceList[position].forEachIndexed { index, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$position$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$position$index")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$position$index")
            }
        }
    }

    private fun addPreDrawTrackerLine(route: ArrayList<RouteSimulationDataItem>, index: Int) {
        val trackerLineToPreDraw = arrayListOf<Point>()
        route[index].coordinates?.forEach {
            trackerLineToPreDraw.add(Point.fromLngLat(it[0], it[1]))
        }
        mPreDrawTrackerLine[index] = trackerLineToPreDraw
        addTrackerLine(trackerLineToPreDraw, route, index)
    }

    private fun zoomCamera() {
        val cameraZoomList = arrayListOf<LatLng>()
        notificationData.forEachIndexed { index, notificationData ->
            if (notificationData.isSelected) {
                cameraZoomList.add(
                    LatLng(
                        mPreDrawTrackerLine[index].first().latitude(),
                        mPreDrawTrackerLine[index].first().longitude()
                    )
                )
                cameraZoomList.add(
                    LatLng(
                        mPreDrawTrackerLine[index].last().latitude(),
                        mPreDrawTrackerLine[index].last().longitude()
                    )
                )
            }
        }
        mActivity?.resources?.getDimension(R.dimen.dp_80)?.toInt()
            ?.let { mMapHelper?.adjustMapBounds(cameraZoomList, it) }
    }

    private fun addTrackerLine(
        trackerLineToPreDraw: ArrayList<Point>,
        route: ArrayList<RouteSimulationDataItem>,
        index: Int
    ) {
        mMapHelper?.addTrackerLine(
            trackerLineToPreDraw,
            true,
            "layer${route[index].id}_pre_draw",
            "source${route[index].id}_pre_draw",
            R.color.color_hint_text
        )
    }

    private fun removePreDrawTrackerLine(route: ArrayList<RouteSimulationDataItem>, index: Int) {
        route[index].let {
            it.id?.let { it1 -> mMapHelper?.removeSource(it1) }
            it.id?.let { it1 -> mMapHelper?.removeLayer(it1) }
            mMapHelper?.removeLayer("layer${it.id}")
            mMapHelper?.removeLayer("source${it.id}")
            mMapHelper?.removeLayer("layer${it.id}_pre_draw")
            mMapHelper?.removeLayer("source${it.id}_pre_draw")
        }
    }

    private fun getNextCoordinates(): List<BusRouteCoordinates>? {
        val maxCoordinatesSize = routeData?.maxOfOrNull { it.coordinates?.size ?: 0 } ?: 0
        val nextCoordinates = routeData?.mapIndexedNotNull { index, routeSimulationDataItem ->
            if (isBusTrackerNotificationEnable(index)) {
                val coordinatesSize = routeSimulationDataItem.coordinates?.size ?: 0
                if (coordinatesSize > 0) {
                    timerCounts?.let {
                        val currentIndex = (it[index]) % coordinatesSize
                        if (currentIndex == 0) {
                            (activity as MainActivity).lifecycleScope.launch {
                                mMapHelper?.removeLayer("layer${routeSimulationDataItem.id}")
                                mMapHelper?.removeLayer("source${routeSimulationDataItem.id}")
                                busesCoordinates[index].clear()
                                routeData?.let { route ->
                                    addPreDrawTrackerLine(route, index)
                                }
                            }
                        }
                        it[index] = (it[index] + 1) % maxCoordinatesSize
                        BusRouteCoordinates(
                            routeSimulationDataItem.id,
                            routeSimulationDataItem.geofenceCollection,
                            routeSimulationDataItem.coordinates?.get(currentIndex),
                            true
                        )
                    }
                } else {
                    null
                }
            } else {
                BusRouteCoordinates(
                    routeSimulationDataItem.id,
                    routeSimulationDataItem.geofenceCollection,
                    null,
                    false
                )
            }
        }
        return nextCoordinates
    }

    private fun getFirstCoordinates(): List<BusRouteCoordinates>? {
        return routeData?.map { route ->
            BusRouteCoordinates(
                route.id,
                route.geofenceCollection,
                route.coordinates?.first()
            )
        }
    }

    private fun getFirstCoordinates(position: Int): BusRouteCoordinates {
        return BusRouteCoordinates(
            routeData?.get(position)?.id,
            routeData?.get(position)?.geofenceCollection,
            routeData?.get(position)?.coordinates?.first()
        )
    }

    private suspend fun updateSimulationLocation(
        busId: String?,
        busIndex: Int,
        point: List<Double>,
        context: Context,
        size: Int
    ) {
        (activity as MainActivity).lifecycleScope.launch {
            val latLng = LatLng(point[1], point[0])
            // TODO - for notification uncomment
//            val position = arrayListOf<Double>()
//            position.add(latLng.longitude)
//            position.add(latLng.latitude)
//            simulationInterface?.evaluateGeofence(simulationCollectionName[busIndex], position)

            mMapHelper?.startAnimation(latLng, busIndex)
            delay(DELAY_1000)
            val latLngPoint = Point.fromLngLat(point[0], point[1])
            busesCoordinates[busIndex].add(latLngPoint)
            val positionData: String = when (busesCoordinates[busIndex].size) {
                1 -> context.getString(R.string.label_position_start)
                size -> context.getString(R.string.label_position_end)
                else -> context.getString(R.string.label_position_data)
            }
            val historyData = SimulationHistoryData(
                positionData,
                false,
                0,
                SimulationHistoryInnerData(point[1], point[0], Date())
            )
            updateTrackingHistoryData(busIndex, historyData)
            if (busesCoordinates[busIndex].size > 1) {
                mMapHelper?.addTrackerLine(
                    busesCoordinates[busIndex],
                    true,
                    "layer$busId",
                    "source$busId",
                    R.color.color_primary_green
                )
                mPreDrawTrackerLine[busIndex].removeAt(0)
                routeData?.let { route ->
                    addTrackerLine(mPreDrawTrackerLine[busIndex] as ArrayList<Point>, route, busIndex)
                }
            }
        }
    }

    private fun isBusTrackerNotificationEnable(busIndex: Int): Boolean {
        busSimulationNotificationFlags?.let {
            return when (busIndex) {
                in it.indices -> {
                    it[busIndex]
                }
                else -> false
            }
        }
        return false
    }

    private fun getSelectedBusTrackingData(busIndex: Int): List<SimulationHistoryData> {
        return when (busIndex) {
            in 0..9 -> busSimulationHistoryData[busIndex]
            else -> emptyList()
        }
    }

    private fun updateTrackingHistoryData(busIndex: Int, data: SimulationHistoryData) {
        if (busIndex in 0..9) {
            busSimulationHistoryData[busIndex].add(data)
        }
    }

    private fun addMarkerSimulation(
        activity1: Activity,
        index: Int,
        routeSimulationDataItem: BusRouteCoordinates
    ) {
        routeSimulationDataItem.coordinates?.get(0)?.let { longitude ->
            routeSimulationDataItem.coordinates?.get(1)
                ?.let { latitude -> LatLng(latitude, longitude) }
                .let {
                    routeSimulationDataItem.id?.let { id ->
                        it?.let { latLng ->
                            mMapHelper?.addMarkerSimulation(
                                id,
                                activity1,
                                latLng,
                                index
                            )
                        }
                    }
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
        this.simulationBinding = bottomSheetTrackSimulationBinding
        initSimulationBottomSheet()
    }

    private fun initSimulationBottomSheet() {
        simulationBinding?.apply {
            mBottomSheetSimulationBehavior = BottomSheetBehavior.from(root)
            mBottomSheetSimulationBehavior?.isHideable = true
            mBottomSheetSimulationBehavior?.isDraggable = true
            mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetSimulationBehavior?.isFitToContents = false
            mBottomSheetSimulationBehavior?.halfExpandedRatio = 0.6f

            mBottomSheetSimulationBehavior?.addBottomSheetCallback(object :
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
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
    private fun isNotificationGroupActive(context: Context): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val activeNotifications = notificationManager.activeNotifications

        // Check if any of the active notifications belong to the specified group
        for (notification in activeNotifications) {
            val groupKey = notification.notification.group
            if (groupKey == GROUP_KEY_WORK_SIMULATION) {
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
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build()

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
        simulationBinding?.apply {
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
                        mIsLocationUpdateEnable = false
                        CoroutineScope(Dispatchers.Default).launch {
                            delay(DELAY_1000)
                            withContext(Dispatchers.Main) {
                                simulationHistoryData.clear()
                                simulationHistoryData.addAll(
                                    getSelectedBusTrackingData(
                                        selectedTrackerIndex
                                    )
                                )
                                simulationTrackingListAdapter?.submitList(simulationHistoryData.toMutableList())
                            }
                            mIsLocationUpdateEnable = true
                        }
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

    private fun setDefaultIconWithGeofence(index: String) {
        mMapboxMap?.getStyle { style ->
            if (style.getSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index) == null) {
                style.addSource(GeoJsonSource(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index))
            }
            initPolygonCircleFillLayer(index)
        }
        mIsDefaultGeofence = true
    }

    /**
     * Add a [FillLayer] to display a [Polygon] in a the shape of a circle.
     */
    private fun initPolygonCircleFillLayer(index: String) {
        mMapboxMap?.getStyle { style ->
            val fillLayer = FillLayer(
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + index,
                GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + index
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

            if (style.getLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + index) == null) {
                style.addLayerBelow(fillLayer, GeofenceCons.CIRCLE_CENTER_LAYER_ID + index)
            }
        }
    }

    private fun initClick() {
        simulationBinding?.apply {
            cardStartTracking.setOnClickListener {
                if (mIsLocationUpdateEnable) {
                    stopTracker()
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
                    closeNotificationCard()
                } else {
                    showViews(rvRouteNotifications, viewDividerNotification)
                    ivBackArrowRouteNotifications.rotation = 180f
                }
            }

            ivBackArrowChangeRoute.setOnClickListener {
                if (rvTracking.isVisible) {
                    closeTrackingCard()
                } else {
                    showViews(rvTracking, viewDividerBus)
                    ivBackArrowChangeRoute.rotation = 180f
                }
            }
        }
    }

    private fun BottomSheetTrackSimulationBinding.closeNotificationCard() {
        hideViews(rvRouteNotifications, viewDividerNotification)
        ivBackArrowRouteNotifications.rotation = 0f
    }

    private fun BottomSheetTrackSimulationBinding.closeTrackingCard() {
        hideViews(rvTracking, viewDividerBus)
        ivBackArrowChangeRoute.rotation = 0f
    }

    private fun BottomSheetTrackSimulationBinding.stopTracker() {
        mActivity?.getColor(R.color.color_primary_green)
            ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
        tvStopTracking.text = mActivity?.getText(R.string.label_start_tracking)
        tvTrackingYourActivity.text =
            mActivity?.getText(R.string.label_tracking_in_active)
        tvTrackingYourActivity.context?.let {
            tvTrackingYourActivity.setTextColor(
                ContextCompat.getColor(
                    it,
                    R.color.color_hint_text
                )
            )
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
        mIsLocationUpdateEnable = true
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
                            if (mIsLocationUpdateEnable) {
                                startTracking()
                            }
                        }
                        else -> {
                            simulationBinding?.apply {
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
        simulationBinding?.apply {
            mActivity?.getColor(R.color.color_red)
                ?.let { it1 -> cardStartTracking.setCardBackgroundColor(it1) }
            tvStopTracking.text =
                mActivity?.getText(R.string.label_stop_tracking)
            tvTrackingYourActivity.text =
                mActivity?.getText(R.string.label_tracking_active)
            tvTrackingYourActivity.context.let {
                tvTrackingYourActivity.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.color_red
                    )
                )
            }
            if (simulationHistoryData.size == 0) {
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
                    val notificationData =
                        Gson().fromJson(stringData, NotificationSimulationData::class.java)
                    var subTitle = ""
                    if (notificationData.trackerEventType.equals("ENTER", true)) {
                        subTitle = "Entered ${notificationData.geofenceId} geofence"
                        var selectedIndex = 0
                        var busStopCount = 0
                        routeData?.forEachIndexed { index, routeSimulationDataItem ->
                            if (routeSimulationDataItem.geofenceCollection == notificationData.geofenceCollection) {
                                selectedIndex = index
                                busStopCount = routeSimulationDataItem.busStopCount + 1
                                routeSimulationDataItem.busStopCount++
                                return@forEachIndexed
                            }
                        }
                        mFragmentActivity?.applicationContext?.let {
                            busSimulationHistoryData[selectedIndex].add(
                                SimulationHistoryData(
                                    it.getString(R.string.label_position_data),
                                    true,
                                    busStopCount,
                                    notificationData.coordinates?.get(0)?.let { longitude ->
                                        notificationData.coordinates?.get(1)?.let { latitude ->
                                            SimulationHistoryInnerData(
                                                latitude,
                                                longitude,
                                                Date()
                                            )
                                        }
                                    }
                                )
                            )
                        }
                    } else {
                        subTitle = "Exited ${notificationData.geofenceId} geofence"
                    }
                    notificationId++
                    mActivity?.let {
                        if (isNotificationGroupActive(it)) {
                            notificationData.geofenceId?.let { it1 ->
                                showNotification(
                                    notificationId,
                                    it1,
                                    subTitle,
                                    false
                                )
                            }
                        } else {
                            notificationData.geofenceId?.let { it1 ->
                                showNotification(
                                    notificationId,
                                    it1,
                                    subTitle,
                                    true
                                )
                            }
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
        simulationNotificationAdapter = SimulationNotificationAdapter(
            notificationData,
            object : SimulationNotificationAdapter.NotificationInterface {
                override fun click(position: Int, isSelected: Boolean) {
                    notificationData[position].isSelected = isSelected
                    busSimulationNotificationFlags?.set(position, isSelected)
                    setSelectedNotificationCount()
                    routeData?.let { route ->
                        if (isSelected) {
                            addPreDrawTrackerLine(route, position)
                            drawGeofenceWithPosition(position)
                            mActivity?.let { activity ->
                                val data = getFirstCoordinates(position)
                                addMarkerSimulation(activity, position, data)
                            }
                            zoomCamera()
                        } else {
                            (activity as MainActivity).lifecycleScope.launch {
                                delay(DELAY_1000)
                                busSimulationHistoryData[position].clear()
                                removePreDrawTrackerLine(route, position)
                                removeGeofenceWithPosition(position)
                                timerCounts?.set(position, 0)
                                mMapHelper?.removeGeoJsonSourceData(position)
                            }
                        }
                    }
                }
            }
        )
        simulationBinding?.rvRouteNotifications?.adapter = simulationNotificationAdapter
        simulationBinding?.rvRouteNotifications?.layoutManager = notificationLayoutManager

        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
        simulationTrackingListAdapter = SimulationListAdapter()
        simulationBinding?.rvTracking?.adapter = simulationTrackingListAdapter
        simulationBinding?.rvTracking?.layoutManager = layoutManager
    }

    private fun setSelectedNotificationCount() {
        var totalCount = 0
        busSimulationNotificationFlags?.forEach {
            if (it) {
                totalCount++
            }
        }
        simulationBinding?.apply {
            tvRouteNotificationsName.text = buildString {
                append(totalCount)
                append(" ")
                if (totalCount >= 1) append(tvRouteNotificationsName.context.getString(R.string.routes_active)) else append(tvRouteNotificationsName.context.getString(R.string.route_active))
            }
        }
    }

    fun isSimulationBottomSheetVisible(): Boolean {
        return mBottomSheetSimulationBehavior?.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun setSimulationDraggable(isDrag: Boolean) {
        if (!isDrag) {
            mBottomSheetSimulationBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        mBottomSheetSimulationBehavior?.isDraggable = isDrag
    }

    fun setSimulationDraggable() {
        setSimulationDraggable(true)
    }

    fun setSimulationData() {
        setBounds()
        mMapHelper?.setGeoJsonSourceEmpty()
        notificationData.forEachIndexed { index, notificationData ->
            if (notificationData.isSelected) {
                val coordinatesSize = routeData?.get(index)?.coordinates?.size ?: 0
                if (coordinatesSize > 0) {
                    timerCounts?.let {
                        val currentIndex = (it[index]) % coordinatesSize
                        val data = BusRouteCoordinates(
                            routeData?.get(index)?.id,
                            routeData?.get(index)?.geofenceCollection,
                            routeData?.get(index)?.coordinates?.get(currentIndex)
                        )
                        mActivity?.let { activity ->
                            addMarkerSimulation(activity, index, data)
                        }
                    }
                }
            }
        }
        (activity as MainActivity).lifecycleScope.launch {
            delay(DELAY_1000)
            zoomCamera()
        }
    }

    fun hideSimulationBottomSheet() {
        mMapboxMap?.removeViewBounds()
        simulationBinding?.apply {
            closeTrackingCard()
            closeNotificationCard()
            clPersistentBottomSheetSimulation.hide()
        }
        simulationBinding?.apply {
            stopTracker()
        }
        coroutineScope.cancel()
        (activity as MainActivity).lifecycleScope.launch {
            delay(DELAY_1000)
            routeData?.forEach { routeSimulationDataItem ->
                routeSimulationDataItem.id?.let { it1 ->
                    mMapHelper?.removeSource(it1)
                    mMapHelper?.removeLayer(it1)
                    mMapHelper?.removeLayer("layer$it1")
                    mMapHelper?.removeLayer("source$it1")
                    mMapHelper?.removeLayer("layer${it1}_pre_draw")
                    mMapHelper?.removeLayer("source${it1}_pre_draw")
                }
            }
            mMapHelper?.clearMarker()
            mMapHelper?.removeLine()
            mMapHelper?.removeSimulationData()
            routeData?.clear()
            notificationData.forEach { data ->
                data.isSelected = false
            }
            notificationId = 1
            busSimulationNotificationFlags = null
            mIsLocationUpdateEnable = false
        }
        mGeofenceList.forEachIndexed { index, data ->
            data.forEachIndexed { indexInner, _ ->
                mMapboxMap?.style?.removeLayer(GeofenceCons.CIRCLE_CENTER_LAYER_ID + "$index$indexInner")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_ID + "$index$indexInner")
                mMapboxMap?.style?.removeLayer(GeofenceCons.TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID + "$index$indexInner")
            }
        }
        mGeofenceList.clear()
        hideSimulationSheet(activity)
    }

    private fun hideSimulationSheet(activity: MainActivity) {
        mBottomSheetSimulationBehavior.let {
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
            stopMqttManager()
            activity.reInitializeSimulation()
        }
    }

    fun manageGeofenceListUI(dataGeofence: SimulationGeofenceData) {
        val position =
            simulationCollectionName.indexOfFirst { it.contains(dataGeofence.collectionName) }
        mGeofenceList[position].clear()
        mGeofenceList[position] = dataGeofence.devicePositionData
        if (isBusTrackerNotificationEnable(position)) {
            drawGeofenceWithPosition(position)
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

    private fun MapboxMap.removeViewBounds() {
        // Set the LatLngBounds for the camera target to null
        setLatLngBoundsForCameraTarget(null)
        style?.let { mMapHelper?.updateZoomRange(it) }
        mMapHelper?.checkLocationComponentEnable((mActivity as BaseActivity), true)
    }
}
