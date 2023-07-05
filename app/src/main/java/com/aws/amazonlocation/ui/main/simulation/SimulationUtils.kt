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
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.services.geo.AmazonLocationClient
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.*
import com.aws.amazonlocation.data.response.TrackingHistoryData
import com.aws.amazonlocation.databinding.BottomSheetTrackSimulationBinding
import com.aws.amazonlocation.domain.*
import com.aws.amazonlocation.domain.`interface`.TrackingInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.maps.MapboxMap
import java.util.*
import kotlin.collections.ArrayList

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class SimulationUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mAWSLocationHelper: AWSLocationHelper
) {
    private var mqttManager: AWSIotMqttManager? = null
    private var adapter: SimulationTrackingListAdapter? = null
    private var adapterSimulation: SimulationNotificationAdapter? = null
    private var mBottomSheetTrackingBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var mBindingTracking: BottomSheetTrackSimulationBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var mTrackingInterface: TrackingInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mMapboxMap: MapboxMap? = null
    private var mClient: AmazonLocationClient? = null
    private var mActivity: Activity? = null
    private var mIsLocationUpdateEnable = false
    private var trackingHistoryData = arrayListOf<TrackingHistoryData>()
    private val notificationData = arrayListOf<NotificationData>()
    private var headerId = 0
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
    }

    fun initSimulationView(
        fragmentActivity: FragmentActivity?,
        bottomSheetTrackSimulationBinding: BottomSheetTrackSimulationBinding,
        mGeofenceInterface: TrackingInterface
    ) {
        this.mTrackingInterface = mGeofenceInterface
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

            mBottomSheetTrackingBehavior = BottomSheetBehavior.from(root)
            mBottomSheetTrackingBehavior?.isHideable = true
            mBottomSheetTrackingBehavior?.isDraggable = true
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetTrackingBehavior?.isFitToContents = false
            mBottomSheetTrackingBehavior?.halfExpandedRatio = 0.6f
            cardStartTracking.setOnClickListener {
                val mIdentityPoolId = mPreferenceManager?.getValue(
                    KEY_POOL_ID,
                    ""
                )
                val regionData = mPreferenceManager?.getValue(
                    KEY_USER_REGION,
                    ""
                )
                if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
                    mActivity?.getString(R.string.reconnect_with_aws_account)
                        ?.let { it1 -> (activity as MainActivity).showError(it1) }
                    (activity as MainActivity).restartAppWithClearData()
                    return@setOnClickListener
                }
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
                    mTrackingInterface?.removeUpdateBatch()
                    stopMqttManager()
                } else {
                    viewLoader.show()
                    tvStopTracking.hide()
                    cardStartTracking.isEnabled = false
                    startMqttManager()
                }
            }
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

    private fun initClick() {
        mBindingTracking?.apply {
            cardStartTracking.setOnClickListener {
                val mIdentityPoolId = mPreferenceManager?.getValue(
                    KEY_POOL_ID,
                    ""
                )
                val regionData = mPreferenceManager?.getValue(
                    KEY_USER_REGION,
                    ""
                )
                if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
                    mActivity?.getString(R.string.reconnect_with_aws_account)
                        ?.let { it1 -> (activity as MainActivity).showError(it1) }
                    (activity as MainActivity).restartAppWithClearData()
                    return@setOnClickListener
                }
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
                    mTrackingInterface?.removeUpdateBatch()
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
                            if (mIsLocationUpdateEnable) {
                                startTracking()
                            }
                            mBindingTracking?.apply {
                                viewLoader.hide()
                                tvStopTracking.show()
                                cardStartTracking.isEnabled = true
                            }
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

        val type = object : TypeToken<ArrayList<TrackingHistoryData>>() {}.type
        trackingHistoryData = Gson().fromJson(SIMULATION_DUMMY_DATA, type)
        val layoutManager = LinearLayoutManager(mActivity?.applicationContext)
        adapter = SimulationTrackingListAdapter(trackingHistoryData)
        mBindingTracking?.rvTracking?.adapter = adapter
        mBindingTracking?.rvTracking?.layoutManager = layoutManager
    }
}
