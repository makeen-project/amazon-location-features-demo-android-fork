package com.aws.amazonlocation.ui.main.simulation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.*
import com.aws.amazonlocation.data.response.TrackingHistoryData
import com.aws.amazonlocation.databinding.BottomSheetTrackSimulationBinding
import com.aws.amazonlocation.domain.*
import com.aws.amazonlocation.domain.`interface`.SimulationInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.*
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfConstants
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfMeta
import com.aws.amazonlocation.utils.geofence_helper.turf.TurfTransformation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonParser
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class SimulationUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private var timer: Timer? = null
    private var isBus1TrackerFinish: Boolean = false
    private var isBus2TrackerFinish: Boolean = false
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
    private var trackingHistoryData = arrayListOf<TrackingHistoryData>()
    private val notificationData = arrayListOf<NotificationData>()
    private val mCircleUnit: String = TurfConstants.UNIT_METERS
    private var mIsDefaultGeofence = false
    private var mGeofenceList = ArrayList<ListGeofenceResponseEntry>()
    private var simulationUpdate = HashMap<Int, Int>()

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
        simulationInterface?.getGeofenceList(GeofenceCons.GEOFENCE_COLLECTION)
    }

    private fun initData() {
        mFragmentActivity?.applicationContext?.let {
            val bus1Geometries = GeoJsonReader.readGeoJsonFile(it, "bus1_line.geojson")
            val bus2Geometries = GeoJsonReader.readGeoJsonFile(it, "bus2_line.geojson")
            val mInitialLatLngList = ArrayList<LatLng>()
            val bus1Coordinates = arrayListOf<Point>()
            val bus2Coordinates = arrayListOf<Point>()
            mInitialLatLngList.add(
                LatLng(
                    (bus1Geometries[0] as MultiLineString).coordinates()[0][0].latitude(),
                    (bus1Geometries[0] as MultiLineString).coordinates()[0][0].longitude()
                )
            )
            mInitialLatLngList.add(
                LatLng(
                    (bus2Geometries[0] as MultiLineString).coordinates()[0][0].latitude(),
                    (bus2Geometries[0] as MultiLineString).coordinates()[0][0].longitude()
                )
            )
            mActivity?.let { activity1 ->
                mMapHelper?.addMarkerSimulation(
                    "tracker" + 0,
                    activity1,
                    mInitialLatLngList[0],
                    0
                )
                mMapHelper?.addMarkerSimulation(
                    "tracker" + 1,
                    activity1,
                    mInitialLatLngList[1],
                    1
                )
            }
            mMapHelper?.adjustMapBounds(
                mInitialLatLngList,
                mActivity?.resources?.getDimension(R.dimen.dp_110)
                    ?.toInt()!!
            )
            timer = Timer()
            val task = object : TimerTask() {
                override fun run() {
                    if (!isBus1TrackerFinish) {
                        bus1Geometries[0].let { geometry ->
                            if (geometry is MultiLineString) {
                                geometry.coordinates()[0].let { points ->
                                    simulationUpdate[0]?.let { update ->
                                        if (points.size > update && points[update] != null) {
                                            points[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point.latitude(),
                                                        point.longitude()
                                                    )
//                                                    simulationInterface?.evaluateGeofence(latLng)
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        0
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point.longitude(),
                                                            point.latitude()
                                                        )
                                                    bus1Coordinates.add(latLngPoint)
                                                    if (bus1Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus1Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 0,
                                                            "sourceId" + "tracker" + 0
                                                        )
                                                    }
                                                }
                                                val updatedValue = simulationUpdate[0]?.plus(1) ?: 0
                                                simulationUpdate[0] = updatedValue
                                            }
                                        } else {
                                            isBus1TrackerFinish = true
                                            simulationUpdate[0] = 0
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!isBus2TrackerFinish) {
                        bus2Geometries[0].let { geometry ->
                            if (geometry is MultiLineString) {
                                geometry.coordinates()[0].let { points ->
                                    simulationUpdate[1]?.let { update ->
                                        if (points.size > update && points[update] != null) {
                                            points[update].let { point ->
                                                (activity as MainActivity).lifecycleScope.launch {
                                                    val latLng = LatLng(
                                                        point.latitude(),
                                                        point.longitude()
                                                    )
//                                                    simulationInterface?.evaluateGeofence(latLng)
                                                    mMapHelper?.startAnimation(
                                                        latLng,
                                                        1
                                                    )
                                                    delay(DELAY_1000)
                                                    val latLngPoint =
                                                        Point.fromLngLat(
                                                            point.longitude(),
                                                            point.latitude()
                                                        )
                                                    bus2Coordinates.add(latLngPoint)
                                                    if (bus2Coordinates.size > 1) {
                                                        mMapHelper?.addTrackerLine(
                                                            bus2Coordinates,
                                                            true,
                                                            "layerId" + "tracker" + 1,
                                                            "sourceId" + "tracker" + 1
                                                        )
                                                    }
                                                }
                                                val updatedValue = simulationUpdate[1]?.plus(1) ?: 0
                                                simulationUpdate[1] = updatedValue
                                            }
                                        } else {
                                            isBus2TrackerFinish = true
                                            simulationUpdate[1] = 0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            timer?.scheduleAtFixedRate(task, 0, DELAY_1000)
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
        }
    }

    private fun drawGeofence(mapPoint: Point, radius: Int, index: String) {
        drawPolygonCircle(mapPoint, radius, index)
    }

    /**
     * Update the [FillLayer] based on the GeoJSON retrieved via
     * [.getTurfPolygon].
     *
     * @param circleCenter the center coordinate to be used in the Turf calculation.
     */
    private fun drawPolygonCircle(circleCenter: Point, radius: Int, index: String) {
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
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_TOP_RIGHT_LEFT_PADDING,
                    Durations.CAMERA_BOTTOM_PADDING
                ),
                Durations.CAMERA_DURATION_1500
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
                    mIsLocationUpdateEnable = true
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
        mIsLocationUpdateEnable = true
        if (mqttManager != null) stopMqttManager()
        val identityId: String? =
            mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.identityId

        mqttManager =
            AWSIotMqttManager(identityId, mPreferenceManager?.getValue(WEB_SOCKET_URL, ""))
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
                            identityId.let {
                                if (it != null) {
                                    subscribeTopic(it)
                                }
                            }
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
            initData()
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
                    val subTitle = if (type.equals("ENTER", true)) {
                        "Tracker entered $geofenceName"
                    } else {
                        "Tracker exited $geofenceName"
                    }
                    runOnUiThread {
                        activity?.messageDialog(
                            title = geofenceName,
                            subTitle = subTitle,
                            false,
                            object : MessageInterface {
                                override fun onMessageClick(dialog: DialogInterface) {
                                    dialog.dismiss()
                                }
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter() {
        val notificationLayoutManager = LinearLayoutManager(mActivity?.applicationContext)
        adapterSimulation = SimulationNotificationAdapter(
            notificationData,
            object : SimulationNotificationAdapter.NotificationInterface {
                override fun click(position: Int, isSelected: Boolean) {
                }
            }
        )
        mBindingTracking?.rvRouteNotifications?.adapter = adapterSimulation
        mBindingTracking?.rvRouteNotifications?.layoutManager = notificationLayoutManager

//        val type = object : TypeToken<ArrayList<TrackingHistoryData>>() {}.type
//        trackingHistoryData = Gson().fromJson(SIMULATION_DUMMY_DATA, type)
//        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
//        adapter = SimulationTrackingListAdapter(trackingHistoryData)
//        mBindingTracking?.rvTracking?.adapter = adapter
//        mBindingTracking?.rvTracking?.layoutManager = layoutManager
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideSimulationBottomSheet() {
        mBottomSheetTrackingBehavior.let {
            timer?.cancel()
            (activity as MainActivity).lifecycleScope.launch {
                delay(DELAY_1000)
                notificationData.forEachIndexed { index, notificationData ->
                    mMapHelper?.removeSource("sourceIdtracker$index")
                    mMapHelper?.removeSource("source-idtracker$index")
                    mMapHelper?.removeLayer("layerIdtracker$index")
                    mMapHelper?.removeLayer("layer-idtracker$index")
                }
                mMapHelper?.clearMarker()
                mMapHelper?.removeLine()
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

    fun manageGeofenceListUI(list: ArrayList<ListGeofenceResponseEntry>) {
        mGeofenceList.clear()
        mGeofenceList.addAll(list)
        if (mGeofenceList.isNotEmpty()) {
            val mLatLngList = ArrayList<LatLng>()
            mGeofenceList.forEachIndexed { index, data ->
                val latLng = LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
                if (checkGeofenceInsideGrab(
                        latLng,
                        mPreferenceManager,
                        mActivity?.applicationContext
                    )
                ) {
                    setDefaultIconWithGeofence(index)
                    mLatLngList.add(latLng)
                    drawGeofence(
                        Point.fromLngLat(latLng.longitude, latLng.latitude),
                        data.geometry.circle.radius.toInt(),
                        index.toString()
                    )
                }
            }
            mMapHelper?.adjustMapBounds(
                mLatLngList,
                mActivity?.resources?.getDimension(R.dimen.dp_130)?.toInt()!!
            )
        }
    }
}
