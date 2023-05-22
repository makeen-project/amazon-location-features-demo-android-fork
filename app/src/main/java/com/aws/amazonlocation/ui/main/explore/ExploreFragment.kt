package com.aws.amazonlocation.ui.main.explore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.res.Configuration
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.* // ktlint-disable no-wildcard-imports
import android.view.inputmethod.EditorInfo
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import aws.sdk.kotlin.services.location.model.TravelMode
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.Leg
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.amplifyframework.auth.options.AuthFetchSessionOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.models.Coordinates
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.databinding.BottomSheetDirectionBinding
import com.aws.amazonlocation.databinding.BottomSheetDirectionSearchBinding
import com.aws.amazonlocation.databinding.FragmentExploreBinding
import com.aws.amazonlocation.domain.`interface`.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import com.aws.amazonlocation.ui.main.map_style.MapStyleBottomSheetFragment
import com.aws.amazonlocation.ui.main.map_style.MapStyleChangeListener
import com.aws.amazonlocation.ui.main.signin.CloudFormationBottomSheetFragment
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.ui.main.tracking.TrackingViewModel
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Distance.DISTANCE_IN_METER_10
import com.aws.amazonlocation.utils.MapNames.ESRI_LIGHT
import com.aws.amazonlocation.utils.MapStyles.VECTOR_ESRI_TOPOGRAPHIC
import com.aws.amazonlocation.utils.Units.convertToLowerUnit
import com.aws.amazonlocation.utils.Units.getDeviceId
import com.aws.amazonlocation.utils.Units.getMetricsNew
import com.aws.amazonlocation.utils.Units.getTime
import com.aws.amazonlocation.utils.Units.isGPSEnabled
import com.aws.amazonlocation.utils.Units.isMetric
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.* // ktlint-disable no-wildcard-imports
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import java.util.*
import kotlin.math.roundToInt

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class ExploreFragment :
    BaseFragment(),
    OnMapReadyCallback,
    SignOutInterface,
    MapboxMap.OnMapClickListener,
    MapHelper.IsMapLoadedInterface,
    MapboxMap.OnScaleListener,
    MapStyleChangeListener {

    private lateinit var mapStyleBottomSheetFragment: MapStyleBottomSheetFragment
    private var isCalculateDriveApiError: Boolean = false
    private var isCalculateWalkApiError: Boolean = false
    private var isCalculateTruckApiError: Boolean = false
    private var isLocationUpdatedNeeded: Boolean = false
    private var isZooming: Boolean = false
    private var mLastClickTime: Long = 0
    private var mIsDirectionDataSet: Boolean = false
    private var mIsDirectionSheetHalfExpanded: Boolean = false
    private var mIsLocationAlreadyEnabled: Boolean = false
    private var mIsCurrentLocationClicked: Boolean = false
    private var mIsTrackingLocationClicked: Boolean = false
    private lateinit var mBinding: FragmentExploreBinding
    private var mMapboxMap: MapboxMap? = null
    private var mAdapter: SearchPlacesAdapter? = null
    private var mMapStyleAdapter: MapStyleAdapter? = null
    private var mAdapterDirection: SearchPlacesAdapter? = null
    private var mPlaceList = ArrayList<SearchSuggestionData>()
    private var mNavigationList = ArrayList<NavigationData>()
    private var mSearchPlacesDirectionSuggestionAdapter: SearchPlacesSuggestionAdapter? = null
    private var mSearchPlacesSuggestionAdapter: SearchPlacesSuggestionAdapter? = null
    private var mNavigationAdapter: NavigationAdapter? = null
    val mViewModel: ExploreViewModel by viewModels()
    private val mSignInViewModel: SignInViewModel by viewModels()
    private val mGeofenceViewModel: GeofenceViewModel by viewModels()
    private val mTrackingViewModel: TrackingViewModel by viewModels()
    private var mIsAvoidTolls: Boolean = false
    private var mIsAvoidFerries: Boolean = false
    private var mIsRouteOptionsOpened = false
    private var mTravelMode: String = TravelMode.Car.value
    private var mRouteFinish: Boolean = false
    private var mRedirectionType: String? = null
    private val mServiceName = "geo"

    private var gpsActivityResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            checkAndEnableLocation()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.bottomSheetSearch.clSearchSheet.layoutParams.width = width
        mBinding.bottomSheetSearch.clSearchSheet.requestLayout()
        mBinding.bottomSheetDirection.clPersistentBottomSheetDirection.layoutParams.width = width
        mBinding.bottomSheetDirection.clPersistentBottomSheetDirection.requestLayout()
        mBinding.bottomSheetDirectionSearch.clDirectionSearchSheet.layoutParams.width = width
        mBinding.bottomSheetDirectionSearch.clDirectionSearchSheet.requestLayout()
        mBinding.bottomSheetNavigation.clNavigationParent.layoutParams.width = width
        mBinding.bottomSheetNavigation.clNavigationParent.requestLayout()
        mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.layoutParams.width = width
        mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete.requestLayout()
        mBinding.bottomSheetTracking.clPersistentBottomSheet.layoutParams.width = width
        mBinding.bottomSheetTracking.clPersistentBottomSheet.requestLayout()
        mBinding.bottomSheetGeofenceList.clGeofenceListMain.layoutParams.width = width
        mBinding.bottomSheetGeofenceList.clGeofenceListMain.requestLayout()
        mBinding.bottomSheetAddGeofence.clPersistentBottomSheetAddGeofence.layoutParams.width = width
        mBinding.bottomSheetAddGeofence.clPersistentBottomSheetAddGeofence.requestLayout()
        mBinding.bottomSheetAttribution.clMain.layoutParams.width = width
        mBinding.bottomSheetAttribution.clMain.requestLayout()
        val widthTimeDialog = resources.getDimensionPixelSize(R.dimen.navigation_top_dialog_size)
        mBinding.cardNavigationTimeDialog.layoutParams.width = widthTimeDialog
        mBinding.cardNavigationTimeDialog.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // initialize MapLibre
        Mapbox.getInstance(requireContext())
        mBinding = FragmentExploreBinding.inflate(inflater, container, false)
        KeyBoardUtils.attachKeyboardListeners(
            mBinding.root,
            object : KeyBoardUtils.KeyBoardInterface {
                override fun showKeyBoard() {
                    mBaseActivity?.mGeofenceUtils?.let {
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
                        } else if (it.geofenceBottomSheetVisibility()) {
                            mBaseActivity?.mGeofenceUtils?.expandAddGeofenceBottomSheet()
                        } else {
                            if (mBottomSheetHelper.isSearchSheetOpen && !mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
                                mBottomSheetHelper.expandSearchBottomSheet()
                            } else if (mBinding.bottomSheetSearch.edtSearchPlaces.hasFocus() && mBottomSheetHelper.isSearchBottomSheetHalfExpand()) {
                                mBottomSheetHelper.expandSearchBottomSheet()
                            } else {
                            }
                        }
                    }
                }

                override fun hideKeyBoard() {
                    mBaseActivity?.mGeofenceUtils?.let {
                        if (it.isAddGeofenceBottomSheetVisible()) {
                            mBaseActivity?.mGeofenceUtils?.collapseAddGeofenceBottomSheet()
                        } else if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            return
                        } else if (mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                            mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
                            if (mPlaceList.isNotEmpty()) {
                                mBottomSheetHelper.halfExpandBottomSheet()
                            } else {
                                mBottomSheetHelper.collapseSearchBottomSheet()
                            }
                        } else {
                            if (mBottomSheetHelper.isSearchSheetOpen) {
                                mBottomSheetHelper.isSearchSheetOpen = false
                                mBottomSheetHelper.halfExpandBottomSheet()
                            } else if (mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
                                mBottomSheetHelper.hideSearchBottomSheet(false)
                            } else {
                            }
                        }
                    }
                }
            },
        )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMap(savedInstanceState)
        mBottomSheetHelper.setSearchBottomSheet(
            activity,
            mBinding.bottomSheetSearch,
            mBaseActivity,
            this@ExploreFragment,
        )
        mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        mBinding.bottomSheetDirectionSearch.switchAvoidTools.isChecked = mIsAvoidTolls
        mBinding.bottomSheetDirectionSearch.switchAvoidFerries.isChecked = mIsAvoidFerries

        mBottomSheetHelper.setNavigationBottomSheet(mBinding.bottomSheetNavigation)
        mBottomSheetHelper.setNavigationCompleteBottomSheet(mBinding.bottomSheetNavigationComplete.clPersistentBottomSheetNavigationComplete)
        mBottomSheetHelper.setDirectionBottomSheet(mBinding.bottomSheetDirection.clPersistentBottomSheetDirection)
        mBaseActivity?.isTablet?.let {
            if (!it) {
                mBottomSheetHelper.setMapStyleBottomSheet(mBinding.bottomSheetMapStyle)
            } else {
                mBinding.bottomSheetMapStyle.clMapStyleBottomSheet.hide()
            }
        }
        mBottomSheetHelper.setAttributeBottomSheet(mBinding.bottomSheetAttribution)
        mBottomSheetHelper.setDirectionSearchBottomSheet(
            mBinding.bottomSheetDirectionSearch,
            this,
            mBaseActivity,
        )
        mBaseActivity?.mGeofenceUtils?.initGeofenceView(
            activity,
            mBinding.bottomSheetGeofenceList,
            mBinding.bottomSheetAddGeofence,
            mGeofenceInterface,
        )

        mBaseActivity?.mTrackingUtils?.initTrackingView(
            activity,
            mBinding.bottomSheetTracking,
            mTrackingInterface,
        )
        setUserProfile()
        if ((activity as MainActivity).isAppNotFirstOpened()) {
            checkPermission()
        }
        setNavigationAdapter()
        setMapStyleAdapter()
        setSearchPlaceDirectionAdapter()
        setSearchPlaceDirectionSuggestionAdapter()
        setSearchPlaceAdapter()
        setSearchPlaceSuggestionAdapter()
        initObserver()
        initGeofenceObserver()
        clickListener()

        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (mMapHelper.mSymbolManager == null) {
                    activity?.runOnUiThread {
                        mBinding.groupMapLoad.show()
                        mBinding.mapView.getMapAsync(this@ExploreFragment)
                    }
                }
            }

            override fun onLost(network: Network) {
            }
        })
        (activity as MainActivity).showBottomBar()
        if (!checkSessionValid(mPreferenceManager)) {
            AWSMobileClient.getInstance().signOut()
            activity?.userSignOutDialog()
        }
    }

    private fun initGeofenceObserver() {
        lifecycleScope.launchWhenStarted {
            mBinding.apply {
                mGeofenceViewModel.mGetGeofenceList.collect { handleResult ->
                    bottomSheetGeofenceList.apply {
                        handleResult.onLoading {
                            rvGeofence.hide()
                            clSearchLoaderGeofenceList.root.show()
                        }.onSuccess {
                            clSearchLoaderGeofenceList.root.hide()
                            rvGeofence.show()
                            lifecycleScope.launch(Dispatchers.Main) {
                                mBaseActivity?.mGeofenceUtils?.manageGeofenceListUI(it)
                            }
                        }.onError {
                            clSearchLoaderGeofenceList.root.hide()
                            rvGeofence.hide()
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mTrackingViewModel.mGetGeofenceList.collect { handleResult ->
                handleResult.onLoading {
                    mBinding.bottomSheetGeofenceList.clSearchLoaderGeofenceList.root.show()
                }.onSuccess {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBaseActivity?.mTrackingUtils?.manageGeofenceListUI(it)
                    }
                }.onError {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mBinding.apply {
                mTrackingViewModel.mGetLocationHistoryList.collect { handleResult ->
                    handleResult.onLoading {
                        bottomSheetTracking.clSearchLoaderSheetTracking.root.show()
                    }.onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mTrackingUtils?.locationHistoryListUI(it)
                        }
                    }.onError {
                        bottomSheetTracking.clSearchLoaderSheetTracking.root.hide()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mTrackingViewModel.mGetLocationHistoryTodayList.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBaseActivity?.mTrackingUtils?.locationHistoryTodayListUI(it)
                    }
                }.onError {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mTrackingViewModel.mGetUpdateDevicePosition.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBaseActivity?.mTrackingUtils?.getTodayData()
                    }
                }.onError {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mBinding.apply {
                mTrackingViewModel.mDeleteLocationHistoryList.collect { handleResult ->
                    handleResult.onLoading {
                    }.onSuccess {
                        lifecycleScope.launch(Dispatchers.Main) {
                            mBaseActivity?.mTrackingUtils?.deleteTrackingData()
                        }
                    }.onError {
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mGeofenceViewModel.mAddGeofence.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mBaseActivity?.mGeofenceUtils?.mangeAddGeofenceUI(requireActivity())
                        mBaseActivity?.bottomNavigationVisibility(true)
                        showViews(mBinding.cardGeofenceMap, mBinding.cardMap)
                        activity?.hideKeyboard()
                    }
                }.onError {
                    if (it.messageResource.toString()
                            .contains(resources.getString(R.string.unable_to_execute_request))
                    ) {
                        showError(resources.getString(R.string.check_your_internet_connection_and_try_again))
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mGeofenceViewModel.mDeleteGeofence.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    lifecycleScope.launch(Dispatchers.Main) {
                        mGeofenceInterface.hideShowBottomNavigationBar(
                            false,
                            GeofenceBottomSheetEnum.NONE,
                        )
                        it.position?.let { position ->
                            mBaseActivity?.mGeofenceUtils?.notifyGeofenceList(
                                position,
                                requireActivity(),
                            )
                        }
                    }
                }.onError {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mGeofenceViewModel.mGeofenceSearchForSuggestionsResultList.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    val mText = mBinding.bottomSheetAddGeofence.edtAddGeofenceSearch.text.toString()
                        .replace(", ", ",")
                    if (!it.text.isNullOrEmpty() && it.text == mText) {
                        mBaseActivity?.mGeofenceUtils?.updateGeofenceSearchSuggestionList(it.data)
                    }
                    activity?.hideKeyboard()
                }.onError {
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mGeofenceViewModel.mGeofenceSearchLocationList.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    val mText = mBinding.bottomSheetAddGeofence.edtAddGeofenceSearch.text.toString()
                    if (!it.text.isNullOrEmpty() && it.text == mText) {
                        mBaseActivity?.mGeofenceUtils?.updateGeofenceSearchPlaceList(it.data)
                    }
                    activity?.hideKeyboard()
                }.onError {
                }
            }
        }
    }

    private val mGeofenceInterface = object : GeofenceInterface {

        override fun addGeofence(
            geofenceId: String,
            collectionName: String,
            radius: Double?,
            latLng: LatLng?,
        ) {
            activity?.hideKeyboard()
            mGeofenceViewModel.addGeofence(geofenceId, collectionName, radius, latLng)
        }

        override fun getGeofenceList(collectionName: String) {
            mGeofenceViewModel.getGeofenceList(collectionName)
        }

        override fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry) {
            mGeofenceViewModel.deleteGeofence(position, data)
        }

        override fun geofenceSearchPlaceIndexForText(searchText: String) {
            mGeofenceViewModel.geofenceSearchPlaceIndexForText(searchText, mViewModel.mLatLng)
        }

        override fun hideShowBottomNavigationBar(isHide: Boolean, type: GeofenceBottomSheetEnum) {
            lifecycleScope.launch {
                mBinding.apply {
                    if (!isHide) {
                        showViews(cardGeofenceMap, cardMap)
                    } else {
                        hideViews(cardGeofenceMap, cardMap)
                    }
                }
                mBaseActivity?.bottomNavigationVisibility(!isHide)
                delay(100)
                when (type) {
                    GeofenceBottomSheetEnum.EMPTY_GEOFENCE_BOTTOM_SHEET -> {
                        mBaseActivity?.mGeofenceUtils?.emptyGeofenceBottomSheetAddBtn()
                    }
                    GeofenceBottomSheetEnum.ADD_GEOFENCE_BOTTOM_SHEET -> {
                        mBaseActivity?.mGeofenceUtils?.addGeofenceCloseBtn(requireActivity())
                    }
                    else -> {}
                }
            }
        }

        override fun openAddGeofenceBottomSheet(point: LatLng) {
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
        }
    }
    private val mTrackingInterface = object : TrackingInterface {

        override fun updateBatch(latLng: LatLng) {
            latLng.let {
                val positionData = arrayListOf<Double>()
                positionData.add(it.longitude)
                positionData.add(it.latitude)
                mTrackingViewModel.batchUpdateDevicePosition(
                    TrackerCons.TRACKER_COLLECTION,
                    positionData,
                    getDeviceId(requireContext()),
                    Date(),
                )
            }
        }

        override fun updateBatch() {
            activity?.hideKeyboard()
            mMapHelper.setTrackingUpdateRoute(mTrackingUpDate)
        }

        override fun removeUpdateBatch() {
            mMapHelper.removeTrackingLocationListener()
        }

        override fun getLocationHistory(startDate: Date, endDate: Date) {
            mTrackingViewModel.getLocationHistory(
                TrackerCons.TRACKER_COLLECTION,
                getDeviceId(requireContext()),
                startDate,
                endDate,
            )
        }

        override fun getTodayLocationHistory(startDate: Date, endDate: Date) {
            mTrackingViewModel.getLocationHistoryToday(
                TrackerCons.TRACKER_COLLECTION,
                getDeviceId(requireContext()),
                startDate,
                endDate,
            )
        }

        override fun getGeofenceList(collectionName: String) {
            mBinding.bottomSheetTracking.layoutNoDataFound.root.hide()
            mTrackingViewModel.getGeofenceList(collectionName)
        }

        override fun getCheckPermission() {
            mIsTrackingLocationClicked = true
            checkLocationPermission()
        }

        override fun getDeleteTrackingData() {
            mTrackingViewModel.deleteLocationHistory(
                TrackerCons.TRACKER_COLLECTION,
                getDeviceId(requireContext()),
            )
        }
    }

    private fun setNavigationAdapter() {
        mBinding.bottomSheetNavigation.apply {
            mNavigationAdapter = NavigationAdapter(mNavigationList, mPreferenceManager)
            rvNavigationList.layoutManager = LinearLayoutManager(requireContext())
            rvNavigationList.adapter = mNavigationAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setMapStyleAdapter() {
        mBinding.bottomSheetMapStyle.rvMapStyle.apply {
            mViewModel.setMapListData(context)
            val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                ?: getString(R.string.map_esri)
            val mapStyleName =
                mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                    ?: getString(R.string.map_light)
            mViewModel.mStyleList.forEach {
                if (it.styleNameDisplay.equals(mapName)) {
                    it.isSelected = true
                    it.mapInnerData?.forEach { mapStyleInnerData ->
                        if (mapStyleInnerData.mapName.equals(mapStyleName)) {
                            mapStyleInnerData.isSelected = true
                        }
                    }
                } else {
                    it.isSelected = false
                }
            }
            this.layoutManager = LinearLayoutManager(requireContext())
            mMapStyleAdapter =
                MapStyleAdapter(
                    mViewModel.mStyleList,
                    object : MapStyleAdapter.MapInterface {
                        override fun mapClick(position: Int) {
                            if (position != -1) {
                                if (!mViewModel.mStyleList[position].isSelected) {
                                    repeat(mViewModel.mStyleList.size) {
                                        mViewModel.mStyleList[it].isSelected = false
                                    }
                                } else {
                                    return
                                }
                                mapStyleChange(position, 0)
                                mViewModel.mStyleList[position].isSelected =
                                    !mViewModel.mStyleList[position].isSelected
                                mMapStyleAdapter?.notifyDataSetChanged()
                            }
                        }

                        override fun mapStyleClick(position: Int, innerPosition: Int) {
                            if (checkInternetConnection()) {
                                if (position != -1 && innerPosition != -1) {
                                    mViewModel.mStyleList[position].mapInnerData?.let {
                                        if (it[innerPosition].isSelected) {
                                            return
                                        }
                                    }
                                    mapStyleChange(position, innerPosition)
                                }
                            }
                        }
                    },
                )
            this.adapter = mMapStyleAdapter
        }
    }

    fun setUserProfile() {
        mBinding.bottomSheetSearch.apply {
            val userName = getUserName(mBaseActivity?.getUserInfo())
            if (userName.isNullOrEmpty()) {
                tvUserProfile.hide()
                ivUserProfile.show()
                cardUserProfile.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white,
                    ),
                )
            } else {
                ivUserProfile.hide()
                tvUserProfile.show()
                tvUserProfile.text = userName
                cardUserProfile.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow,
                    ),
                )
            }
        }
    }

    private val mRouteUpDate = object : UpdateRouteInterface {
        override fun updateRoute(latLng: Location, bearing: Float?) {
            if (mBottomSheetHelper.isNavigationSheetVisible() && isLocationUpdatedNeeded) {
                if (mViewModel.mNavigationResponse != null) {
                    mViewModel.mDestinationLatLng?.latitude?.let { latitude ->
                        mViewModel.mDestinationLatLng?.longitude?.let { longitude ->
                            val destinationLocation = Location("destination")
                            destinationLocation.latitude = latitude
                            destinationLocation.longitude = longitude
                            val distance = destinationLocation.distanceTo(latLng)
                            if (distance < DISTANCE_IN_METER_10) {
                                mBottomSheetHelper.hideNavigationSheet()
                                mBottomSheetHelper.expandNavigationCompleteSheet()
                                mBinding.bottomSheetNavigationComplete.tvNavigationCompleteAddress.text =
                                    mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.label?.split(
                                        ",",
                                    )?.toTypedArray()?.get(0)
                                        ?: mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.label

                                mBinding.bottomSheetNavigationComplete.sheetNavigationCompleteTvDirectionStreet.text =
                                    getRegion(
                                        mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.region,
                                        mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.subRegion,
                                        mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.country,
                                    )

                                mBinding.cardNavigationTimeDialog.hide()
                                mMapHelper.removeLine()
                                mMapHelper.removeLocationListener()
                                mMapboxMap?.removeOnScaleListener(this@ExploreFragment)
                            } else {
                                bearing?.let { mMapHelper.bearingCamera(it, latLng) }
                                CoroutineScope(Dispatchers.IO).launch {
                                    mViewModel.updateCalculateDistanceFromMode(
                                        latLng.latitude,
                                        latLng.longitude,
                                        mViewModel.mDestinationLatLng?.latitude,
                                        mViewModel.mDestinationLatLng?.longitude,
                                        mIsAvoidFerries,
                                        mIsAvoidTolls,
                                        mTravelMode,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val mTrackingUpDate = object : UpdateTrackingInterface {
        override fun updateRoute(latLng: Location, bearing: Float?) {
            latLng.let {
                val positionData = arrayListOf<Double>()
                positionData.add(it.longitude)
                positionData.add(it.latitude)
                mTrackingViewModel.batchUpdateDevicePosition(
                    TrackerCons.TRACKER_COLLECTION,
                    positionData,
                    getDeviceId(requireContext()),
                    Date(),
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        mBinding.bottomSheetNavigation.apply {
            lifecycleScope.launchWhenStarted {
                mViewModel.mNavigationData.collect { handleResult ->
                    handleResult.onLoading {
                        clSearchLoaderNavigation.root.show()
                        rvNavigationList.hide()
                    }.onSuccess {
                        clSearchLoaderNavigation.root.hide()
                        rvNavigationList.show()
                        mBinding.bottomSheetNavigation.apply {
                            tvNavigationDistance.text = it.distance?.let { it1 ->
                                convertToLowerUnit(
                                    it1,
                                    isMetric(
                                        mPreferenceManager.getValue(
                                            KEY_UNIT_SYSTEM,
                                            "",
                                        ),
                                    ),
                                )
                            }?.let { it2 ->
                                getMetricsNew(
                                    it2,
                                    isMetric(
                                        mPreferenceManager.getValue(
                                            KEY_UNIT_SYSTEM,
                                            "",
                                        ),
                                    ),
                                )
                            }
                            tvNavigationTime.text = it.duration
                        }
                        if (it.navigationList.isNotEmpty()) {
                            it.navigationList[0].distance?.let { distance ->
                                setNavigationTimeDialog(
                                    distance,
                                    it.navigationList[0].getRegions(),
                                )
                            }
                        }
                        mNavigationList.clear()
                        mNavigationList.addAll(it.navigationList)
                        mNavigationAdapter?.notifyDataSetChanged()
                        if (mTravelMode == TravelMode.Walking.value) {
                            mNavigationAdapter?.setIsRounded(true)
                        } else {
                            mNavigationAdapter?.setIsRounded(false)
                        }
                        if (isLocationUpdatedNeeded) {
                            mMapHelper.setUpdateRoute(mRouteUpDate)
                            mMapboxMap?.addOnScaleListener(this@ExploreFragment)
                        }
                    }.onError { it ->
                        isLocationUpdatedNeeded = false
                        clSearchLoaderNavigation.root.hide()
                        rvNavigationList.show()
                        it.messageResource?.let {
                            showError(it.toString())
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mSignInViewModel.mSignInResponse.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    mBaseActivity?.mIsUserLoggedIn = true
                    showError(it)
                    setUserProfile()
                    mPreferenceManager.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.SIGNED_IN.name,
                    )
                    (activity as MainActivity).getTokenAndAttachPolicy(it)
                }.onError { it ->
                    setUserProfile()
                    (activity as MainActivity).hideProgress()
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mSignInViewModel.mSignOutResponse.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    mBaseActivity?.clearUserInFo()
                    showError(it.message.toString())
                    mBaseActivity?.mPreferenceManager?.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.AWS_CONNECTED.name,
                    )
                    mPreferenceManager.removeValue(KEY_ID_TOKEN)
                    mPreferenceManager.removeValue(KEY_PROVIDER)
                    setUserProfile()
                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                    activity?.restartApplication()
                }.onError { it ->
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mBinding.apply {
                mViewModel.mSearchLocationList.collect { handleResult ->
                    handleResult.onLoading {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            bottomSheetSearch.apply {
                                clSearchLoaderSearchSheet.root.show()
                                rvSearchPlacesSuggestion.hide()
                            }
                        } else {
                            bottomSheetDirectionSearch.apply {
                                clSearchLoaderDirectionSearch.root.show()
                                rvSearchPlacesSuggestionDirection.hide()
                                rvSearchPlacesDirection.hide()
                            }
                        }
                    }.onSuccess {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            addPlaceDataInList(it, SearchApiEnum.SEARCH_PLACE_INDEX_TEXT)
                            mAdapter?.notifyDataSetChanged()
                        } else {
                            addPlaceDirectionDataInList(it, SearchApiEnum.SEARCH_PLACE_INDEX_TEXT)
                            mAdapterDirection?.notifyDataSetChanged()
                        }
                    }.onError {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            mBinding.bottomSheetSearch.apply {
                                clSearchLoaderSearchSheet.root.hide()
                                rvSearchPlacesSuggestion.hide()
                            }
                        } else {
                            bottomSheetDirectionSearch.apply {
                                clDriveLoader.hide()
                                clWalkLoader.hide()
                                clTruckLoader.hide()
                                clMapOptionRoute.show()
                            }
                        }
                        if (it.messageResource.toString() == resources.getString(R.string.check_your_internet_connection_and_try_again)) {
                            mBinding.apply {
                                if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                    hideViews(
                                        bottomSheetDirectionSearch.layoutNoDataFound.root,
                                        bottomSheetDirectionSearch.layoutCardError.root,
                                        bottomSheetDirectionSearch.rvSearchPlacesDirection,
                                        bottomSheetDirectionSearch.rvSearchPlacesSuggestionDirection,
                                    )
                                    bottomSheetDirectionSearch.clNoInternetConnectionDirectionSearch.show()
                                } else if (mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                    hideViews(
                                        bottomSheetSearch.layoutNoDataFound.root,
                                        bottomSheetSearch.nsSearchPlaces,
                                    )
                                    bottomSheetSearch.clNoInternetConnectionSearchSheet.show()
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mBinding.apply {
                mViewModel.searchForSuggestionsResultList.collect { handleResult ->
                    handleResult.onLoading {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            bottomSheetSearch.apply {
                                clSearchLoaderSearchSheet.root.show()
                                rvSearchPlaces.hide()
                                rvSearchPlacesSuggestion.hide()
                            }
                        } else {
                            bottomSheetDirectionSearch.apply {
                                clSearchLoaderDirectionSearch.root.show()
                                rvSearchPlacesSuggestionDirection.hide()
                                rvSearchPlacesDirection.hide()
                            }
                        }
                    }.onSuccess {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            addPlaceDataInList(it, SearchApiEnum.SEARCH_PLACE_SUGGESTION)
                            mSearchPlacesSuggestionAdapter?.notifyDataSetChanged()
                        } else {
                            addPlaceDirectionDataInList(it, SearchApiEnum.SEARCH_PLACE_SUGGESTION)
                            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
                        }
                    }.onError {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            bottomSheetSearch.apply {
                                clSearchLoaderSearchSheet.root.hide()
                                rvSearchPlaces.hide()
                                rvSearchPlacesSuggestion.hide()
                            }
                        } else {
                            bottomSheetDirectionSearch.apply {
                                clSearchLoaderDirectionSearch.root.hide()
                            }
                        }
                        if (it.messageResource.toString() == resources.getString(R.string.check_your_internet_connection_and_try_again)) {
                            mBinding.apply {
                                if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                    hideViews(
                                        bottomSheetDirectionSearch.layoutNoDataFound.root,
                                        bottomSheetDirectionSearch.layoutCardError.root,
                                        bottomSheetDirectionSearch.rvSearchPlacesDirection,
                                        bottomSheetDirectionSearch.rvSearchPlacesSuggestionDirection,
                                    )
                                    bottomSheetDirectionSearch.clNoInternetConnectionDirectionSearch.show()
                                } else if (!mBottomSheetHelper.isSearchPlaceSheetVisible()) {
                                    hideViews(
                                        bottomSheetSearch.layoutNoDataFound.root,
                                        bottomSheetSearch.nsSearchPlaces,
                                    )
                                    bottomSheetSearch.clNoInternetConnectionSearchSheet.show()
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.mCalculateDistance.collect { handleResult ->
                handleResult.onLoading {
                    if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                        mBinding.apply {
                            bottomSheetDirection.apply {
                                groupDistanceLoad.show()
                                groupDistance.invisible()
                            }
                        }
                    } else {
                        mBinding.bottomSheetDirectionSearch.clSearchLoaderDirectionSearch.root.hide()
                    }
                }.onSuccess {
                    mBinding.apply {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            bottomSheetDirection.apply {
                                groupDistanceLoad.hide()
                                groupDistance.show()
                                tvDirectionError.hide()
                                tvDirectionError2.hide()
                                ivInfo.hide()
                                btnDirection.setCardBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_primary_green,
                                    ),
                                )
                                if (activity?.checkLocationPermission() == true) {
                                    if (!isGPSEnabled(requireContext())) {
                                        mBinding.bottomSheetDirection.tvDirectionError2.text =
                                            getString(R.string.label_location_permission_denied)
                                        locationErrorDirection()
                                    }
                                } else {
                                    mBinding.bottomSheetDirection.tvDirectionError2.text =
                                        getString(R.string.label_location_permission_denied)
                                    locationErrorDirection()
                                }
                            }
                        } else {
                            when (it.name.toString()) {
                                TravelMode.Walking.value -> {
                                    mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                                    mBinding.bottomSheetDirectionSearch.clWalk.show()
                                }
                                TravelMode.Car.value -> {
                                    mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                                    mBinding.bottomSheetDirectionSearch.clDrive.show()
                                }
                                TravelMode.Truck.value -> {
                                    mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                                    mBinding.bottomSheetDirectionSearch.clTruck.show()
                                }
                            }
                            mBinding.bottomSheetDirectionSearch.clMapOptionRoute.show()
                        }
                    }
                    when (it.name) {
                        TravelMode.Car.value -> {
                            mViewModel.mCarData = it.calculateRouteResult
                            mBinding.bottomSheetDirection.apply {
                                mViewModel.mCarData?.legs?.let { legs ->
                                    if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                        legs.firstOrNull()?.let { firstLeg ->
                                            firstLeg.distance?.let { distance ->
                                                tvDirectionDistance.text = mPreferenceManager.getValue(
                                                    KEY_UNIT_SYSTEM,
                                                    "",
                                                ).let { unitSystem ->
                                                    val isMetric = isMetric(unitSystem)
                                                    getMetricsNew(convertToLowerUnit(distance, isMetric), isMetric)
                                                }
                                            }
                                            groupDistance.show()
                                            tvDirectionDot.show()
                                            tvDirectionTime.show()
                                            tvDirectionTime.text = getTime(firstLeg.durationSeconds)
                                        }
                                    }
                                    mBinding.bottomSheetDirectionSearch.apply {
                                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                            mIsDirectionDataSet = true
                                            edtSearchDest.setText(tvDirectionAddress.text)
                                            lifecycleScope.launch {
                                                delay(600)
                                                mIsDirectionDataSet = false
                                            }
                                        } else {
                                            if (mTravelMode == TravelMode.Car.value) {
                                                tvDriveSelected.show()
                                                hideViews(tvTruckSelected, tvWalkSelected)
                                                drawPolyLineOnMap(
                                                    legs,
                                                    isLineUpdate = false,
                                                    isWalk = false,
                                                    isLocationIcon = false,
                                                    sourceLatLng = it.sourceLatLng,
                                                    destinationLatLng = it.destinationLatLng,
                                                )
                                            }
                                        }

                                        setGOButtonState(
                                            edtSearchDirection.text.toString(),
                                            cardDriveGo,
                                            clDrive,
                                        )
                                        legs.firstOrNull()?.let { firstLeg ->
                                            tvDriveDistance.text = mPreferenceManager.getValue(
                                                KEY_UNIT_SYSTEM,
                                                "",
                                            ).let { unitSystem ->
                                                val isMetric = isMetric(unitSystem)
                                                getMetricsNew(convertToLowerUnit(firstLeg.distance, isMetric), isMetric)
                                            }
                                            tvDriveMinute.text = getTime(firstLeg.durationSeconds)
                                        }
                                    }
                                }
                            }
                        }
                        TravelMode.Walking.value -> {
                            mViewModel.mWalkingData = it.calculateRouteResult
                            mBinding.bottomSheetDirectionSearch.apply {
                                mViewModel.mWalkingData?.legs?.let { walkingData ->
                                    setGOButtonState(
                                        edtSearchDirection.text.toString(),
                                        cardWalkGo,
                                        clWalk,
                                    )
                                    tvWalkDistance.text = mPreferenceManager.getValue(
                                        KEY_UNIT_SYSTEM,
                                        "",
                                    ).let { unitSystem ->
                                        val isMetric = isMetric(unitSystem)
                                        getMetricsNew(convertToLowerUnit(walkingData[0].distance, isMetric), isMetric)
                                    }
                                    tvWalkMinute.text = getTime(walkingData[0].durationSeconds)
                                    if (mTravelMode == TravelMode.Walking.value) {
                                        tvWalkSelected.show()
                                        hideViews(tvTruckSelected, tvDriveSelected)
                                        drawPolyLineOnMap(
                                            walkingData,
                                            isLineUpdate = false,
                                            isWalk = true,
                                            isLocationIcon = false,
                                            sourceLatLng = it.sourceLatLng,
                                            destinationLatLng = it.destinationLatLng,
                                        )
                                    }
                                }
                            }
                        }
                        TravelMode.Truck.value -> {
                            mViewModel.mTruckData = it.calculateRouteResult
                            mBinding.bottomSheetDirectionSearch.apply {
                                mViewModel.mTruckData?.legs?.let { truckData ->
                                    setGOButtonState(
                                        edtSearchDirection.text.toString(),
                                        cardTruckGo,
                                        clTruck,
                                    )
                                    setGOButtonState(
                                        edtSearchDirection.text.toString(),
                                        cardDriveGo,
                                        clDrive,
                                    )

                                    tvTruckDistance.text = mPreferenceManager.getValue(
                                        KEY_UNIT_SYSTEM,
                                        "",
                                    ).let { unitSystem ->
                                        val isMetric = isMetric(unitSystem)
                                        getMetricsNew(convertToLowerUnit(truckData[0].distance, isMetric), isMetric)
                                    }
                                    tvTruckMinute.text = getTime(truckData[0].durationSeconds)
                                    if (mTravelMode == TravelMode.Truck.value) {
                                        tvTruckSelected.show()
                                        hideViews(tvWalkSelected, tvDriveSelected)
                                        drawPolyLineOnMap(
                                            truckData,
                                            isLineUpdate = false,
                                            isWalk = false,
                                            isLocationIcon = false,
                                            sourceLatLng = it.sourceLatLng,
                                            destinationLatLng = it.destinationLatLng,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }.onError {
                    mBinding.apply {
                        if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            bottomSheetDirection.apply {
                                groupDistanceLoad.hide()
                                groupDistance.show()
                                tvDirectionTime.invisible()
                                tvDirectionDot.invisible()
                                tvDirectionTime.text = ""
                                tvDirectionDistance.text = ""
                                mViewModel.mCarData = null
                            }
                            checkErrorDirectionDistance()
                            isCalculateDriveApiError = true
                        } else {
                            bottomSheetDirectionSearch.apply {
                                when (it.messageResource.toString()) {
                                    TravelMode.Walking.value -> {
                                        cardWalkGo.setCardBackgroundColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.btn_go_disable,
                                            ),
                                        )
                                        mViewModel.mWalkingData = null
                                        isCalculateWalkApiError = true
                                        mBinding.bottomSheetDirectionSearch.clWalkLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clWalk.show()
                                    }
                                    TravelMode.Car.value -> {
                                        cardDriveGo.setCardBackgroundColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.btn_go_disable,
                                            ),
                                        )
                                        isCalculateDriveApiError = true
                                        mViewModel.mCarData = null
                                        mBinding.bottomSheetDirectionSearch.clDriveLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clDrive.show()
                                    }
                                    TravelMode.Truck.value -> {
                                        cardTruckGo.setCardBackgroundColor(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.btn_go_disable,
                                            ),
                                        )
                                        mViewModel.mTruckData = null
                                        isCalculateTruckApiError = true
                                        mBinding.bottomSheetDirectionSearch.clTruckLoader.hide()
                                        mBinding.bottomSheetDirectionSearch.clTruck.show()
                                    }
                                }
                            }
                            mBinding.bottomSheetDirectionSearch.clMapOptionRoute.show()
                            checkAllApiCallFailed()
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.mUpdateCalculateDistance.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    var isWalk = false
                    when (it.name) {
                        TravelMode.Car.value -> {
                            isWalk = false
                            mViewModel.mCarData = it.calculateRouteResult
                        }
                        TravelMode.Walking.value -> {
                            isWalk = true
                            mViewModel.mWalkingData = it.calculateRouteResult
                        }
                        TravelMode.Truck.value -> {
                            isWalk = false
                            mViewModel.mTruckData = it.calculateRouteResult
                        }
                    }

                    activity?.runOnUiThread {
                        it.calculateRouteResult?.legs?.let { it1 ->
                            drawPolyLineOnMap(
                                it1,
                                true,
                                isWalk,
                                isLocationIcon = mBottomSheetHelper.isNavigationSheetVisible(),
                            )
                        }

                        mBinding.bottomSheetNavigation.apply {
                            tvNavigationTime.text =
                                it.calculateRouteResult?.legs?.get(0)?.durationSeconds?.let { it1 ->
                                    getTime(
                                        it1,
                                    )
                                }
                            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                            tvNavigationDistance.text =
                                it.calculateRouteResult?.legs?.get(0)?.distance?.let { it1 ->
                                    convertToLowerUnit(it1, isMetric)
                                }?.let { it2 -> getMetricsNew(it2, isMetric) }
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        it.calculateRouteResult?.legs?.get(0)?.steps?.get(0)?.let { it1 ->
                            mViewModel.getAddressFromLatLng(
                                it.calculateRouteResult?.legs?.get(0)?.startPosition?.get(0),
                                it.calculateRouteResult?.legs?.get(0)?.startPosition?.get(1),
                                it1,
                                true,
                            )
                        }
                    }
                }.onError {
                    showError(it.messageResource.toString())
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.mUpdateRoute.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    when (it.name) {
                        TravelMode.Car.value -> {
                            mViewModel.mCarData = it.calculateRouteResult
                            mBinding.bottomSheetDirectionSearch.apply {
                                mViewModel.mCarData?.legs?.let { legs ->
                                    tvDriveSelected.show()
                                    hideViews(tvTruckSelected, tvWalkSelected)
                                    drawPolyLineOnMap(
                                        legs,
                                        isLineUpdate = false,
                                        isWalk = false,
                                        isLocationIcon = false,
                                    )
                                }
                            }
                        }
                        TravelMode.Walking.value -> {
                            mViewModel.mWalkingData = it.calculateRouteResult
                            mBinding.bottomSheetDirectionSearch.apply {
                                mViewModel.mWalkingData?.legs?.let { walkingData ->
                                    tvWalkSelected.show()
                                    hideViews(tvTruckSelected, tvDriveSelected)
                                    drawPolyLineOnMap(
                                        walkingData,
                                        isLineUpdate = false,
                                        isWalk = true,
                                        isLocationIcon = false,
                                    )
                                }
                            }
                        }
                        TravelMode.Truck.value -> {
                            mViewModel.mTruckData = it.calculateRouteResult
                            mBinding.bottomSheetDirectionSearch.apply {
                                mViewModel.mTruckData?.legs?.let { truckData ->
                                    tvTruckSelected.show()
                                    hideViews(tvWalkSelected, tvDriveSelected)
                                    drawPolyLineOnMap(
                                        truckData,
                                        isLineUpdate = false,
                                        isWalk = false,
                                        isLocationIcon = false,
                                    )
                                }
                            }
                        }
                    }
                    mMapHelper.getLiveLocation()?.let { mLatLng ->
                        mMapHelper.navigationZoomCamera(mLatLng, isZooming)
                    }
                    it.calculateRouteResult?.let { it1 ->
                        mMapHelper.clearOriginMarker()
                        fetchAddressFromLatLng(it1)
                    }
                }.onError {
                    mBinding.bottomSheetDirection.apply {
                        groupDistance.invisible()
                        tvDirectionTime.invisible()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mViewModel.mNavigationTimeDialogData.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    it.distance?.let { distance ->
                        setNavigationTimeDialog(
                            distance,
                            it.getRegions(),
                        )
                    }
                }.onError {
                    showError(it.messageResource.toString())
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            mViewModel.addressLineData.collect { handleResult ->
                handleResult.onLoading {}.onSuccess {
                    if (!mBottomSheetHelper.isSearchPlaceSheetVisible() || mBottomSheetHelper.isDirectionSheetVisible()) {
                        val searchSuggestionData = SearchSuggestionData()
                        if (!it.searchPlaceIndexForPositionResult?.results.isNullOrEmpty()) {
                            it.searchPlaceIndexForPositionResult?.let { searchPlaceIndexForPositionResult ->
                                searchSuggestionData.text =
                                    searchPlaceIndexForPositionResult.results[0].place.label
                                searchSuggestionData.searchText =
                                    searchPlaceIndexForPositionResult.results[0].place.label
                                searchSuggestionData.distance =
                                    searchPlaceIndexForPositionResult.results[0].distance
                                searchSuggestionData.isDestination = true
                                searchSuggestionData.placeId =
                                    searchPlaceIndexForPositionResult.results[0].placeId
                                searchSuggestionData.isPlaceIndexForPosition = false
                                val coordinates = Coordinates(
                                    searchPlaceIndexForPositionResult.results[0].place.geometry.point[1],
                                    searchPlaceIndexForPositionResult.results[0].place.geometry.point[0],
                                )
                                val amazonLocationPlace = AmazonLocationPlace(
                                    coordinates = coordinates,
                                    label = searchPlaceIndexForPositionResult.results[0].place.label,
                                    addressNumber = searchPlaceIndexForPositionResult.results[0].place.addressNumber,
                                    street = searchPlaceIndexForPositionResult.results[0].place.street,
                                    country = searchPlaceIndexForPositionResult.results[0].place.country,
                                    region = searchPlaceIndexForPositionResult.results[0].place.region,
                                    subRegion = searchPlaceIndexForPositionResult.results[0].place.subRegion,
                                    municipality = searchPlaceIndexForPositionResult.results[0].place.municipality,
                                    neighborhood = searchPlaceIndexForPositionResult.results[0].place.neighborhood,
                                    postalCode = searchPlaceIndexForPositionResult.results[0].place.postalCode,
                                )
                                searchSuggestionData.amazonLocationPlace = amazonLocationPlace
                            }
                        } else {
                            searchSuggestionData.text =
                                String.format(STRING_FORMAT, it.latitude, it.longitude)
                            searchSuggestionData.searchText =
                                String.format(STRING_FORMAT, it.latitude, it.longitude)
                            searchSuggestionData.distance = null
                            searchSuggestionData.isDestination = true
                            searchSuggestionData.placeId = "11"
                            searchSuggestionData.isPlaceIndexForPosition = false
                            val coordinates = it.longitude?.let { it1 ->
                                it.latitude?.let { it2 ->
                                    Coordinates(
                                        it2,
                                        it1,
                                    )
                                }
                            }
                            val amazonLocationPlace = coordinates?.let { it1 ->
                                AmazonLocationPlace(
                                    coordinates = it1,
                                    label = String.format(STRING_FORMAT, it.latitude, it.longitude),
                                    addressNumber = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    street = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    country = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    region = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    subRegion = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    municipality = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    neighborhood = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                    postalCode = String.format(
                                        STRING_FORMAT,
                                        it.latitude,
                                        it.longitude,
                                    ),
                                )
                            }
                            searchSuggestionData.amazonLocationPlace = amazonLocationPlace
                        }
                        setDirectionData(searchSuggestionData, true)
                        return@onSuccess
                    }
                    it.searchPlaceIndexForPositionResult?.let { searchPlaceIndexForPositionResult ->
                        val label = searchPlaceIndexForPositionResult.results[0].place.label
                        if (label != null) {
                            if (label.contains(",")) {
                                val index = label.indexOf(",")
                                val result: String = index.let { it1 -> label.substring(0, it1) }
                                mBaseActivity?.mGeofenceUtils?.setSearchText(result)
                            } else {
                                mBaseActivity?.mGeofenceUtils?.setSearchText(label)
                            }
                        }
                    }
                }.onError {
                    showError(it.messageResource.toString())
                }
            }
        }
    }

    private fun checkAllApiCallFailed() {
        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (isCalculateDriveApiError && isCalculateWalkApiError && isCalculateTruckApiError) {
                    cardMapOption.hide()
                    layoutCardError.groupCardErrorNoSearchFound.show()
                    layoutCardError.root.show()
                    val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                    if (mapName == getString(R.string.map_esri)) {
                        layoutCardError.tvCardError1.text = getString(R.string.distance_is_greater_than_400_km)
                        layoutCardError.tvCardError2.text = getString(R.string.can_t_calculate_via_esri_kindly_switch_to_here_provider)
                        layoutCardError.tvCardError2.show()
                    } else {
                        layoutCardError.tvCardError1.text = getString(R.string.no_route_found)
                        layoutCardError.tvCardError2.hide()
                    }
                } else {
                    cardMapOption.show()
                    layoutCardError.groupCardErrorNoSearchFound.hide()
                    layoutCardError.root.hide()
                }
            }
        }
    }

    private fun BottomSheetDirectionBinding.locationErrorDirection() {
        tvDirectionError.hide()
        showViews(tvDirectionError2, ivInfo)
    }

    private fun setGOButtonState(
        source: String,
        cardView: MaterialCardView,
        cl: ConstraintLayout,
    ) {
        if (source == resources.getString(R.string.label_my_location)
        ) {
            mBinding.bottomSheetDirectionSearch.apply {
                when (cardView) {
                    cardWalkGo -> {
                        tvWalkGo.text = getString(R.string.btn_go)
                    }
                    cardDriveGo -> {
                        tvDriveGo.text = getString(R.string.btn_go)
                    }
                    cardTruckGo -> {
                        tvTruckGo.text = getString(R.string.btn_go)
                    }
                }
            }
        } else {
            mBinding.bottomSheetDirectionSearch.apply {
                when (cardView) {
                    cardWalkGo -> {
                        tvWalkGo.text = getString(R.string.label_preview)
                    }
                    cardDriveGo -> {
                        tvDriveGo.text = getString(R.string.label_preview)
                    }
                    cardTruckGo -> {
                        tvTruckGo.text = getString(R.string.label_preview)
                    }
                }
            }
        }
        cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_dark_yellow,
            ),
        )
        cardView.isClickable = true
        cl.isClickable = true
    }

    private fun setNavigationTimeDialog(distance: Double, region: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            mBinding.apply {
                if (!mRouteFinish) cardNavigationTimeDialog.show() else cardNavigationTimeDialog.hide()
                tvDistance.text = mPreferenceManager.getValue(
                    KEY_UNIT_SYSTEM,
                    "",
                ).let { unitSystem ->
                    val isMetric = isMetric(unitSystem)
                    getMetricsNew(convertToLowerUnit(distance, isMetric), isMetric)
                }
                tvNavigationName.text = region
                hideViews(cardDirection, cardMap, cardGeofenceMap)
            }
        }
    }

    private fun addPlaceDataInList(
        it: SearchSuggestionResponse,
        searchPlaceIndexText: SearchApiEnum,
    ) {
        val mText = mBinding.bottomSheetSearch.edtSearchPlaces.text.toString()
        if (!it.text.isNullOrEmpty() && it.text == mText) {
            mPlaceList.clear()
            mPlaceList.addAll(it.data)
            if (mPlaceList.isNotEmpty()) {
                activity?.hideKeyboard()
                mBottomSheetHelper.halfExpandBottomSheet()
            }
            mBaseActivity?.bottomNavigationVisibility(false)
            mMapHelper.addMultipleMarker(
                requireActivity(),
                MarkerEnum.NONE,
                it.data,
                mMarkerClickInterface,
            )
            showNoPlaceFoundUI(searchPlaceIndexText)
        }
        mBinding.apply {
            if (!mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                bottomSheetSearch.apply {
                    clSearchLoaderSearchSheet.root.hide()
                }
            } else {
                bottomSheetDirectionSearch.apply {
                    clSearchLoaderDirectionSearch.root.hide()
                    rvSearchPlacesDirection.show()
                }
            }
        }
    }

    private fun addPlaceDirectionDataInList(
        it: SearchSuggestionResponse,
        searchPlaceIndexText: SearchApiEnum,
    ) {
        val mText: String = if (mBinding.bottomSheetDirectionSearch.edtSearchDirection.hasFocus()) {
            mBinding.bottomSheetDirectionSearch.edtSearchDirection.text.toString().trim()
        } else {
            mBinding.bottomSheetDirectionSearch.edtSearchDest.text.toString().trim()
        }
        if (!it.text.isNullOrEmpty() && it.text.toString().trim().equals(mText, true)) {
            mBinding.apply {
                bottomSheetDirectionSearch.apply {
                    clSearchLoaderDirectionSearch.root.hide()
                    clDriveLoader.hide()
                    clWalkLoader.hide()
                    clTruckLoader.hide()
                    clMapOptionRoute.show()
                    when (searchPlaceIndexText) {
                        SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                            rvSearchPlacesDirection.show()
                            rvSearchPlacesSuggestionDirection.hide()
                        }
                        SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                            rvSearchPlacesSuggestionDirection.show()
                            rvSearchPlacesDirection.hide()
                        }
                    }
                }
            }
            mPlaceList.clear()
            mPlaceList.addAll(it.data)
            if (mPlaceList.isNotEmpty()) {
                activity?.hideKeyboard()
            }
            showNoPlaceFoundDirectionUI(searchPlaceIndexText)
        }
    }

    // get marker click
    private val mMarkerClickInterface = object : MarkerClickInterface {
        override fun markerClick(placeData: String) {
            mPlaceList.forEach {
                if (placeData == it.amazonLocationPlace?.label) {
                    setDirectionData(it, false)
                    return
                }
            }
        }
    }

    // Based on list user able to see UI on screen
    private fun showNoPlaceFoundDirectionUI(searchPlaceIndexText: SearchApiEnum) {
        if (!mIsDirectionSheetHalfExpanded) {
            mBinding.bottomSheetDirectionSearch.apply {
                if (mPlaceList.isNotEmpty()) {
                    clNoInternetConnectionDirectionSearch.hide()
                    nsDirectionSearchPlaces.show()
                    layoutNoDataFound.groupNoSearchFound.hide()
                    layoutCardError.groupCardErrorNoSearchFound.hide()
                    when (searchPlaceIndexText) {
                        SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                            rvSearchPlacesDirection.show()
                            rvSearchPlacesSuggestionDirection.hide()
                        }
                        SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                            rvSearchPlacesSuggestionDirection.show()
                            rvSearchPlacesDirection.hide()
                        }
                    }
                } else {
                    hideViews(rvSearchPlacesDirection, nsDirectionSearchPlaces)
                    layoutNoDataFound.groupNoSearchFound.show()
                }
            }
        }
    }

    // Based on list user able to see UI on screen
    private fun showNoPlaceFoundUI(searchPlaceIndexText: SearchApiEnum) {
        mBinding.bottomSheetSearch.apply {
            clNoInternetConnectionSearchSheet.hide()
            if (mPlaceList.isNotEmpty()) {
                nsSearchPlaces.show()
                layoutNoDataFound.groupNoSearchFound.hide()
                when (searchPlaceIndexText) {
                    SearchApiEnum.SEARCH_PLACE_INDEX_TEXT -> {
                        rvSearchPlaces.show()
                        rvSearchPlacesSuggestion.hide()
                    }
                    SearchApiEnum.SEARCH_PLACE_SUGGESTION -> {
                        rvSearchPlacesSuggestion.show()
                        rvSearchPlaces.hide()
                    }
                }
            } else {
                hideViews(rvSearchPlaces, nsSearchPlaces)
                layoutNoDataFound.groupNoSearchFound.show()
            }
        }
    }

    // search places using text
    private fun searchPlaces(searchText: String) {
        clearSearchList()
        mViewModel.searchPlaceSuggestion(
            searchText,
        )
    }

    // check permission granted or not
    fun checkPermissionFromWelcome() {
        checkPermission()
    }

    // check permission granted or not
    private fun checkPermission() {
        if (activity?.checkLocationPermission() == true) {
            checkGpsLocationProvider(true, isCurrentLocationClicked = false)
        } else {
            permissionReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val permissionReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when {
                it -> {
                    checkGpsLocationProvider(false, isCurrentLocationClicked = false)
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) -> {
                    mBaseActivity?.updateLocationPermission(false)
                }
                else -> {
                    mBaseActivity?.updateLocationPermission(true)
                }
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun clickListener() {
        mBinding.apply {
            cardGeofenceMap.setOnClickListener {
                (activity as MainActivity).geofenceClick()
            }

            cardNavigation.setOnClickListener {
                checkLocationPermission()
            }

            cardMap.setOnClickListener {
                mBaseActivity?.isTablet?.let {
                    if (it) {
                        mapStyleBottomSheetFragment =
                            MapStyleBottomSheetFragment(
                                mViewModel,
                                object : MapStyleAdapter.MapInterface {
                                    override fun mapClick(position: Int) {
                                        if (!mViewModel.mStyleList[position].isSelected) {
                                            repeat(mViewModel.mStyleList.size) {
                                                mViewModel.mStyleList[it].isSelected = false
                                            }
                                        } else {
                                            return
                                        }
                                        mapStyleChange(position, 0)
                                        mViewModel.mStyleList[position].isSelected =
                                            !mViewModel.mStyleList[position].isSelected
                                        mapStyleBottomSheetFragment.notifyAdapter()
                                    }

                                    override fun mapStyleClick(position: Int, innerPosition: Int) {
                                        if (checkInternetConnection()) {
                                            mViewModel.mStyleList[position].mapInnerData?.let {
                                                if (it[innerPosition].isSelected) {
                                                    return
                                                }
                                            }
                                            mapStyleChange(position, innerPosition)
                                        }
                                    }
                                }
                            )
                        activity?.supportFragmentManager.let {
                            if (it != null) {
                                mapStyleBottomSheetFragment.show(
                                    it,
                                    CloudFormationBottomSheetFragment::class.java.name
                                )
                            }
                        }
                    } else {
                        mBottomSheetHelper.expandMapStyleSheet()
                    }
                    when {
                        mBaseActivity?.mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true -> {
                            mBaseActivity?.mTrackingUtils?.collapseTracking()
                        }
                        mBaseActivity?.mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true -> {
                            mBaseActivity?.mGeofenceUtils?.collapseGeofenceList()
                        }
                    }
                }
            }

            bottomSheetNavigationComplete.cardNavigationCompleteClose.setOnClickListener {
                if (checkInternetConnection()) {
                    hideArrivedBottomSheet()
                }
            }

            bottomSheetNavigationComplete.btnNavigationComplete.setOnClickListener {
                if (checkInternetConnection()) {
                    hideArrivedBottomSheet()
                    openDirectionBottomSheet()
                }
            }

            bottomSheetNavigation.cardNavigationLocation.setOnClickListener {
                if (checkInternetConnection()) {
                    mMapHelper.getLiveLocation()?.let { mLatLng ->
                        mMapHelper.navigationZoomCamera(mLatLng, isZooming)
                    }
                }
            }

            cardDirection.setOnClickListener {
                (activity as MainActivity).mTrackingUtils?.hideTrackingBottomSheet()
                clearAllMapData()
                if (checkInternetConnection()) {
                    openDirectionBottomSheet()
                }
            }

            bottomSheetSearch.apply {
                tvSearchCancel?.setOnClickListener {
                    mBottomSheetHelper.collapseSearchBottomSheet()
                }
            }
            bottomSheetSearch.edtSearchPlaces.textChanges().debounce(CLICK_DEBOUNCE).onEach { text ->
                updateSearchUI(text.isNullOrEmpty())
                if (mViewModel.mIsPlaceSuggestion) {
                    if (!text.isNullOrEmpty()) {
                        searchPlaces(text.toString())
                    }
                }
            }.launchIn(lifecycleScope)

            bottomSheetNavigation.apply {
                btnExit.setOnClickListener {
                    navigationExit()
                }
            }

            bottomSheetDirectionSearch.apply {
                if (checkInternetConnection()) {
                    clDrive.setOnClickListener {
                        mTravelMode = TravelMode.Car.value
                        mViewModel.mCarData?.let {
                            tvDriveSelected.show()
                            hideViews(tvTruckSelected, tvWalkSelected)
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                clWalk.setOnClickListener {
                    if (checkInternetConnection()) {
                        mTravelMode = TravelMode.Walking.value
                        mViewModel.mWalkingData?.let {
                            tvWalkSelected.show()
                            hideViews(tvDriveSelected, tvTruckSelected)
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = true,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                clTruck.setOnClickListener {
                    if (checkInternetConnection()) {
                        mTravelMode = TravelMode.Truck.value
                        mViewModel.mTruckData?.let {
                            tvTruckSelected.show()
                            hideViews(tvDriveSelected, tvWalkSelected)
                            adjustMapBound()
                            drawPolyLineOnMapCardClick(
                                it.legs,
                                isLineUpdate = false,
                                isWalk = false,
                                isLocationIcon = false,
                            )
                        }
                    }
                }

                cardDriveGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mCarData?.legs != null) {
                            mTravelMode = TravelMode.Car.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission()
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                cardWalkGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mWalkingData?.legs != null) {
                            mTravelMode = TravelMode.Walking.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission()
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                cardTruckGo.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (mViewModel.mTruckData?.legs != null) {
                            mTravelMode = TravelMode.Truck.value
                            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                                mRedirectionType = RedirectionType.ROUTE_OPTION.name
                                checkLocationPermission()
                            } else {
                                checkRouteData()
                            }
                        } else {
                            checkRouteValidation()
                        }
                    }
                }

                ivSwapLocation.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return@setOnClickListener
                        }
                        mLastClickTime = SystemClock.elapsedRealtime()
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            mIsDirectionDataSet = true
                            edtSearchDirection.setText(edtSearchDest.text.toString().trim())
                            edtSearchDest.setText(resources.getString(R.string.label_my_location))
                            clMyLocation.root.hide()
                            enableDirectionSearch()
                            mViewModel.mSearchDirectionOriginData =
                                mViewModel.mSearchDirectionDestinationData
                            mViewModel.mSearchDirectionOriginData?.isDestination = false
                            mViewModel.mSearchDirectionDestinationData = null
                            mViewModel.mSearchDirectionOriginData?.let { it1 ->
                                showCurrentLocationOriginRoute(
                                    it1,
                                )
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            mIsDirectionDataSet = true
                            edtSearchDest.setText(edtSearchDirection.text.toString().trim())
                            edtSearchDirection.setText(resources.getString(R.string.label_my_location))
                            clMyLocation.root.hide()
                            enableDirectionSearch()
                            mViewModel.mSearchDirectionDestinationData =
                                mViewModel.mSearchDirectionOriginData
                            mViewModel.mSearchDirectionDestinationData?.isDestination = true
                            mViewModel.mSearchDirectionOriginData = null
                            mViewModel.mSearchDirectionDestinationData?.let { it1 ->
                                showCurrentLocationDestinationRoute(
                                    it1,
                                )
                            }
                        } else {
                            mMapHelper.removeMarkerAndLine()
                            clearDirectionData()
                            mIsDirectionDataSet = true
                            val searchDest = edtSearchDest.text.toString().trim()
                            edtSearchDest.setText(edtSearchDirection.text.toString().trim())
                            edtSearchDirection.setText(searchDest)
                            enableDirectionSearch()
                            if (mViewModel.mSearchDirectionOriginData != null && mViewModel.mSearchDirectionDestinationData != null) {
                                val originData = mViewModel.mSearchDirectionOriginData
                                mViewModel.mSearchDirectionOriginData =
                                    mViewModel.mSearchDirectionDestinationData
                                mViewModel.mSearchDirectionOriginData?.isDestination = false
                                mViewModel.mSearchDirectionDestinationData = originData
                                mViewModel.mSearchDirectionDestinationData?.isDestination = true
                                showOriginToDestinationRoute()
                            }
                        }

                        if (edtSearchDirection.hasFocus()) {
                            edtSearchDest.requestFocus()
                            edtSearchDest.setSelection(edtSearchDest.text.toString().length)
                        } else if (edtSearchDest.hasFocus()) {
                            edtSearchDirection.requestFocus()
                            edtSearchDirection.setSelection(edtSearchDirection.text.toString().length)
                        }
                        activity?.hideKeyboard()
                    }
                }
                switchAvoidTools.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mIsAvoidTolls = isChecked
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) showOriginToDestinationRoute()
                    }
                }

                switchAvoidFerries.setOnCheckedChangeListener { _, isChecked ->
                    if (checkInternetConnection()) {
                        mMapHelper.removeMarkerAndLine()
                        clearDirectionData()
                        mIsAvoidFerries = isChecked
                        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionDestinationData?.let {
                                showCurrentLocationDestinationRoute(it)
                            }
                        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                            mViewModel.mSearchDirectionOriginData?.let {
                                showCurrentLocationOriginRoute(it)
                            }
                        } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) showOriginToDestinationRoute()
                    }
                }

                edtSearchDest.textChanges().debounce(CLICK_DEBOUNCE).onEach { text ->
                    updateDirectionSearchUI(text.isNullOrEmpty())
                    if (!mIsDirectionDataSet && mViewModel.mIsPlaceSuggestion && !text.isNullOrEmpty()) {
                        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionDestinationData = null
                            searchPlaces(text.toString())
                        }
                    }
                    checkMyLocationUI(text, edtSearchDirection)
                }.launchIn(lifecycleScope)

                edtSearchDirection.textChanges().debounce(CLICK_DEBOUNCE).onEach { text ->
                    updateDirectionSearchUI(text.isNullOrEmpty())
                    if (!mIsDirectionDataSet && mViewModel.mIsPlaceSuggestion) {
                        if (!text.isNullOrEmpty() && text != getString(R.string.label_my_location) && mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                            cardRouteOptionHide()
                            clearMapLineMarker()
                            mViewModel.mSearchDirectionOriginData = null
                            searchPlaces(text.toString())
                        }
                    }
                    checkMyLocationUI(text, edtSearchDest)
                    lifecycleScope.launch {
                        delay(600)
                        mIsDirectionDataSet = false
                    }
                }.launchIn(lifecycleScope)

                clMyLocation.root.setOnClickListener {
                    if (checkInternetConnection()) {
                        mRedirectionType = RedirectionType.MY_LOCATION.name
                        checkLocationPermission()
                    }
                }
                cardRoutingOption.setOnClickListener {
                    if (checkInternetConnection()) {
                        mIsRouteOptionsOpened = !mIsRouteOptionsOpened
                        changeRouteListUI()
                    }
                }
                bottomSheetSearch.ivAmazonInfoSearchSheet.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetDirectionSearch.ivAmazonInfoDirectionSearchSheet.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetDirection.ivAmazonInfoDirection?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetMapStyle.ivAmazonInfoMapStyle?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetNavigation.ivAmazonInfoNavigation.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetNavigationComplete.ivAmazonInfoNavigationComplete.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetGeofenceList.ivAmazonInfoGeofenceList?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetAddGeofence.ivAmazonInfoAddGeofence?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                bottomSheetTracking.ivAmazonInfoTrackingSheet?.setOnClickListener {
                    setAttributionDataAndExpandSheet()
                }
                mBinding.bottomSheetAttribution.apply {
                    btnLearnMoreSa.setOnClickListener {
                        startActivity(
                            Intent(
                                context,
                                WebViewActivity::class.java,
                            ).putExtra(
                                KEY_URL,
                                BuildConfig.BASE_DOMAIN + BuildConfig.AWS_SOFTWARE_ATTRIBUTION_URL,
                            ),
                        )
                    }
                    btnLearnMore.setOnClickListener {
                        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                        if (mapName == getString(R.string.map_esri)) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_ESRI_URL),
                                ),
                            )
                        } else {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(BuildConfig.ATTRIBUTION_LEARN_MORE_HERE_URL),
                                ),
                            )
                        }
                    }
                    ivBack.setOnClickListener {
                        mBottomSheetHelper.hideAttributeSheet()
                    }
                }
            }

            bottomSheetSearch.cardUserProfile.setOnClickListener {
                if (mBaseActivity?.mIsUserLoggedIn == true) {
                    requireContext().signOutDialog(this@ExploreFragment)
                } else {
                    val poolId = mPreferenceManager.getValue(KEY_POOL_ID, "")
                    if (poolId.isNullOrEmpty()) {
                        mBaseActivity?.mGeofenceBottomSheetHelper?.cloudFormationBottomSheet(
                            TabEnum.TAB_EXPLORE,
                            object : CloudFormationInterface {
                                override fun dialogDismiss(dialog: Dialog?) {
                                    super.dialogDismiss(dialog)
                                    dialog?.dismiss()
                                }
                            },
                        )
                    } else {
                        mSignInViewModel.signInWithAmazon(requireActivity())
                    }
                }
            }

            bottomSheetSearch.ivClose.setOnClickListener {
                bottomSheetSearch.edtSearchPlaces.setText("")
                mMapHelper.clearMarker()
                clearSearchList()
            }

            bottomSheetSearch.edtSearchPlaces.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mBottomSheetHelper.expandSearchBottomSheet()
                }
            }

            bottomSheetSearch.edtSearchPlaces.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    notifyAdapters()
                    mViewModel.searchPlaceIndexForText(bottomSheetSearch.edtSearchPlaces.text.toString())
                    true
                } else {
                    false
                }
            }

            bottomSheetDirectionSearch.edtSearchDest.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    notifyAdapters()
                    mViewModel.searchPlaceIndexForText(bottomSheetDirectionSearch.edtSearchDest.text.toString())
                    true
                } else {
                    false
                }
            }

            bottomSheetDirection.cardDirectionCloseDirection.setOnClickListener {
                clearDirectionBottomSheet()
            }

            bottomSheetMapStyle.cardMapStyleClose.setOnClickListener {
                mBottomSheetHelper.hideMapStyleSheet()
            }

            bottomSheetDirectionSearch.cardDirectionCloseDirectionSearch.setOnClickListener {
                mBinding.bottomSheetSearch.clSearchLoaderSearchSheet.root.hide()
                mMapHelper.addLiveLocationMarker(false)
                mBottomSheetHelper.hideDirectionSearchBottomSheet(this@ExploreFragment)
                hideDirectionBottomSheet()
            }

            mBinding.bottomSheetDirection.apply {
                btnDirection.setOnClickListener {
                    if (checkInternetConnection()) {
                        if (activity?.checkLocationPermission() == true) {
                            if (isGPSEnabled(requireContext())) {
                                if (mViewModel.mCarData?.legs != null) {
                                    routeOption()
                                } else {
                                    openDirectionWithError()
                                }
                            } else {
                                mViewModel.mCarData = null
                                openDirectionWithError()
                            }
                        } else {
                            mViewModel.mCarData = null
                            openDirectionWithError()
                        }
                    }
                }
                ivInfo.setOnClickListener {
                    if (tvDirectionError2.isVisible) {
                        if (tvDirectionError2.text.equals(getString(R.string.label_location_permission_denied))) {
                            activity?.locationPermissionDialog()
                        }
                    }
                }
            }

            bottomSheetAddGeofence.edtAddGeofenceSearch.textChanges().debounce(CLICK_DEBOUNCE)
                .onEach { text ->
                    if (mBaseActivity?.mGeofenceUtils?.isAddGeofenceBottomSheetVisible() == true) {
                        if (text.toString()
                                .isEmpty()
                        ) {
                            mMapboxMap?.removeOnMapClickListener(this@ExploreFragment)
                            mBaseActivity?.mGeofenceUtils?.showAddGeofenceDefaultUI()
                        }
                        if (bottomSheetAddGeofence.edtAddGeofenceSearch.hasFocus()) {
                            val dataToSearch = text.toString().replace(", ", ",")
                            if (dataToSearch.isNotEmpty()) {
                                mGeofenceViewModel.geofenceSearchPlaceSuggestion(
                                    dataToSearch,
                                    mViewModel.mLatLng,
                                )
                            }
                        }
                    }
                }.launchIn(lifecycleScope)

            bottomSheetAddGeofence.edtAddGeofenceSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    notifyAdapters()
                    mGeofenceViewModel.geofenceSearchPlaceIndexForText(
                        bottomSheetAddGeofence.edtAddGeofenceSearch.text.toString(),
                        mViewModel.mLatLng,
                    )
                    true
                } else {
                    false
                }
            }
        }
    }

    fun setAttributionDataAndExpandSheet() {
        setAttributionData()
        mBottomSheetHelper.expandAttributeSheet()
    }

    private fun setAttributionData() {
        mBinding.apply {
            val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
            bottomSheetAttribution.apply {
                if (mapName == getString(R.string.map_esri)) {
                    tvAttribution.text =
                        getString(R.string.label_esri_here)
                } else {
                    tvAttribution.text = getString(R.string.label_here_attribution_text)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun openDirectionWithError() {
        mBinding.bottomSheetDirectionSearch.apply {
            clearDirectionData()
            switchAvoidTools.isChecked = false
            switchAvoidFerries.isChecked = false
            tvDriveGo.text = getString(R.string.btn_go)
            edtSearchDest.setText(mBinding.bottomSheetDirection.tvDirectionAddress.text.trim())
            edtSearchDirection.requestFocus()
            mIsDirectionDataSet = true
            lifecycleScope.launch {
                delay(600)
                mIsDirectionDataSet = false
            }
            hideViews(
                rvSearchPlacesSuggestionDirection,
                rvSearchPlacesDirection,
                clMyLocation.root,
                clSearchLoaderDirectionSearch.root,
                clDriveLoader,
                cardRoutingOption,
                cardMapOption,
                clTruckLoader,
                clWalkLoader,
                clDrive,
            )
            clTruck.invisible()
            clWalk.invisible()
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
        openDirectionSearch()
    }

    private fun BottomSheetDirectionSearchBinding.checkRouteValidation() {
        var originLatLng: LatLng? = null
        var destinationLatLng: LatLng? = null
        if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
            originLatLng = mMapHelper.getLiveLocation()
            mViewModel.mSearchDirectionDestinationData?.let {
                destinationLatLng =
                    it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                        it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
            destinationLatLng = mMapHelper.getLiveLocation()
            mViewModel.mSearchDirectionOriginData?.let {
                originLatLng =
                    it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                        it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
            mViewModel.mSearchDirectionOriginData?.let {
                originLatLng =
                    it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                        it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
            mViewModel.mSearchDirectionDestinationData?.let {
                destinationLatLng =
                    it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                        it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }
            }
        }
        checkErrorDistance(originLatLng, destinationLatLng)
    }

    private fun checkErrorDirectionDistance() {
        if (activity?.checkLocationPermission() == true) {
            if (isGPSEnabled(requireContext())) {
                var destinationLatLng: LatLng? = null
                val originLatLng: LatLng? = mMapHelper.getLiveLocation()
                mViewModel.mSearchSuggestionData?.let {
                    destinationLatLng =
                        it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                            it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                                LatLng(
                                    it1,
                                    it2,
                                )
                            }
                        }
                }
                originLatLng?.let {
                    val distance = destinationLatLng?.latitude?.let { it1 ->
                        destinationLatLng?.longitude?.let { it2 ->
                            mAWSLocationHelper.getDistance(
                                it,
                                it1,
                                it2,
                            )
                        }
                    }
                    if (distance != null) {
                        val mapName = mPreferenceManager.getValue(
                            KEY_MAP_NAME,
                            getString(R.string.map_esri),
                        )
                        mBinding.bottomSheetDirection.tvDirectionError.show()
                        hideViews(
                            mBinding.bottomSheetDirection.tvDirectionError2,
                            mBinding.bottomSheetDirection.ivInfo,
                        )
                        if (mapName == getString(R.string.map_esri)) {
                            val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                            val canEsriShowRoute = if (isMetric) {
                                (distance / 1000) < 400
                            } else {
                                (distance / 5280) < 248.5
                            }
                            if (canEsriShowRoute) {
                                mBinding.bottomSheetDirection.tvDirectionError.text =
                                    getString(R.string.error_route)
                            } else {
                                mBinding.bottomSheetDirection.tvDirectionDistance.text =
                                    getMetricsNew(distance, isMetric)
                                mBinding.bottomSheetDirection.tvDirectionError.text =
                                    getString(R.string.error_switch_to_here)
                            }
                        } else {
                            mBinding.bottomSheetDirection.tvDirectionError.text =
                                getString(R.string.error_route)
                        }
                    } else {
                        mBinding.bottomSheetDirection.tvDirectionError.text =
                            getString(R.string.error_route)
                    }
                }
            } else {
                showLocationError()
                mBinding.bottomSheetDirection.tvDirectionError2.text =
                    getString(R.string.label_location_permission_denied)
            }
        } else {
            showLocationError()
            mBinding.bottomSheetDirection.tvDirectionError2.text =
                getString(R.string.label_location_permission_denied)
        }
    }

    private fun showLocationError() {
        mBinding.bottomSheetDirection.tvDirectionError.hide()
        showViews(
            mBinding.bottomSheetDirection.tvDirectionError2,
            mBinding.bottomSheetDirection.ivInfo,
        )
    }

    private fun checkErrorDistance(
        originLatLng: LatLng?,
        destinationLatLng: LatLng?,
    ) {
        originLatLng?.let {
            val distance = destinationLatLng?.latitude?.let { it1 ->
                destinationLatLng.longitude.let { it2 ->
                    mAWSLocationHelper.getDistance(
                        it,
                        it1,
                        it2,
                    )
                }
            }
            if (distance != null) {
                val mapName = mPreferenceManager.getValue(
                    KEY_MAP_NAME,
                    getString(R.string.map_esri),
                )
                if (mapName == getString(R.string.map_esri)) {
                    if (distance < 400) {
                        showError(getString(R.string.no_route_found))
                    } else {
                        showError(getString(R.string.error_distance_400))
                    }
                } else {
                    showError(getString(R.string.no_route_found))
                }
            } else {
                showError(getString(R.string.no_route_found))
            }
        }
    }

    fun navigationExit() {
        mBinding.apply {
            bottomSheetNavigation.apply {
                if (checkInternetConnection()) {
                    showViews(
                        cardDirection,
                        cardNavigation,
                        cardMap,
                        cardGeofenceMap,
                    )
                    clearNavigationExitData()
                }
            }
        }
    }

    private fun checkInternetConnection(): Boolean {
        return if (context?.isInternetAvailable() == true) {
            true
        } else {
            showError(getString(R.string.check_your_internet_connection_and_try_again))
            false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentExploreBinding.openDirectionBottomSheet() {
        notifyAdapters()
        cardDirection.hide()
        bottomSheetDirectionSearch.clSearchLoaderDirectionSearch.root.hide()
        bottomSheetSearch.edtSearchPlaces.setText("")
        bottomSheetSearch.edtSearchPlaces.clearFocus()
        mBaseActivity?.bottomNavigationVisibility(false)
        mBottomSheetHelper.hideSearchBottomSheet(true)
        mBottomSheetHelper.hideDirectionSheet()
        bottomSheetDirectionSearch.apply {
            mIsDirectionDataSet = true
            edtSearchDest.setText("")
            lifecycleScope.launch {
                delay(600)
                mIsDirectionDataSet = false
            }
            hideViews(
                cardListRoutesOption,
                cardListRoutesOption,
                cardRoutingOption,
                cardMapOption,
            )
            clMyLocation.root.show()
            switchAvoidTools.isChecked = mIsAvoidTolls
            switchAvoidFerries.isChecked = mIsAvoidFerries
            mPlaceList.clear()
            edtSearchDirection.requestFocus()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
        mBottomSheetHelper.expandDirectionSearchSheet(this@ExploreFragment)
        mIsDirectionSheetHalfExpanded = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun routeOption() {
        if (mViewModel.mCarData?.legs != null) {
            mViewModel.mCarData?.legs?.let { legs ->
                adjustMapBound()
                drawPolyLineOnMap(
                    legs,
                    isLineUpdate = false,
                    isWalk = false,
                    isLocationIcon = false,
                )
                mBinding.bottomSheetDirectionSearch.apply {
                    clearTruckAndWalkData()
                    tvDriveGo.text = getString(R.string.btn_go)
                    switchAvoidTools.isChecked = mIsAvoidTolls
                    switchAvoidFerries.isChecked = mIsAvoidFerries
                    edtSearchDirection.setText(getString(R.string.label_my_location))
                    mIsDirectionDataSet = true
                    showViews(
                        cardRoutingOption,
                        cardMapOption,
                        clTruckLoader,
                        clWalkLoader,
                        clDrive,
                    )
                    hideViews(
                        rvSearchPlacesSuggestionDirection,
                        rvSearchPlacesDirection,
                        clMyLocation.root,
                        clSearchLoaderDirectionSearch.root,
                        clDriveLoader,
                    )
                    layoutCardError.groupCardErrorNoSearchFound.hide()
                    layoutCardError.root.hide()
                    clTruck.invisible()
                    clWalk.invisible()
                    mPlaceList.clear()
                    mAdapterDirection?.notifyDataSetChanged()
                    mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
                }
                openDirectionSearch()
                val liveLocationLatLng = mMapHelper.getLiveLocation()
                isCalculateWalkApiError = false
                isCalculateTruckApiError = false
                mViewModel.calculateDistance(
                    latitude = liveLocationLatLng?.latitude,
                    longitude = liveLocationLatLng?.longitude,
                    latDestination = mViewModel.mSearchSuggestionData?.amazonLocationPlace?.coordinates?.latitude,
                    lngDestination = mViewModel.mSearchSuggestionData?.amazonLocationPlace?.coordinates?.longitude,
                    isAvoidFerries = mIsAvoidFerries,
                    isAvoidTolls = mIsAvoidTolls,
                    isWalkingAndTruckCall = true,
                )
            }
        } else {
            showError(getString(R.string.no_route_found))
        }
    }

    private fun openDirectionSearch() {
        mBaseActivity?.bottomNavigationVisibility(false)
        mBottomSheetHelper.hideDirectionSheet()
        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
        mIsDirectionSheetHalfExpanded = true
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.cardDirection.hide()
                mBinding.cardNavigation.show()
            }
        }
    }

    private fun checkLocationPermission() {
        if (activity?.checkLocationPermission() != true) {
            if (mBaseActivity?.getLocationPermissionCount() == 2) {
                mBaseActivity?.showLocationPermissionDialogBox()
            } else {
                checkPermission()
            }
        } else {
            checkGpsLocationProvider(false, isCurrentLocationClicked = true)
        }
    }

    private fun checkRouteData() {
        mBinding.bottomSheetDirectionSearch.apply {
            val mData = when (mTravelMode) {
                TravelMode.Car.value -> mViewModel.mCarData
                TravelMode.Walking.value -> mViewModel.mWalkingData
                TravelMode.Truck.value -> mViewModel.mTruckData
                else -> mViewModel.mCarData
            }
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                mData?.let {
                    drawPolyLineOnMap(
                        it.legs,
                        isLineUpdate = false,
                        isWalk = mTravelMode == TravelMode.Walking.value,
                        isLocationIcon = true,
                    )
                    mMapHelper.getLiveLocation()?.let { mLatLng ->
                        mMapHelper.navigationZoomCamera(mLatLng, isZooming)
                    }
                    mMapHelper.clearOriginMarker()
                    isLocationUpdatedNeeded = true
                    fetchAddressFromLatLng(it)
                }
            } else {
                isLocationUpdatedNeeded = false
                mViewModel.mSearchDirectionOriginData?.let {
                    it.amazonLocationPlace?.coordinates?.latitude?.let { it1 ->
                        it.amazonLocationPlace?.coordinates?.longitude?.let { it2 ->
                            LatLng(
                                it1,
                                it2,
                            )
                        }
                    }?.let { it2 -> mMapHelper.navigationZoomCamera(it2, isZooming) }
                }
                mData?.let {
                    drawPolyLineOnMapCardClick(
                        it.legs,
                        isLineUpdate = false,
                        isWalk = mTravelMode == TravelMode.Walking.value,
                        isLocationIcon = true,
                    )
                    fetchAddressFromLatLng(it)
                }
            }
        }
    }

    private fun directionMyLocation() {
        mBinding.bottomSheetDirectionSearch.apply {
            mIsDirectionDataSet = true
            if (edtSearchDirection.hasFocus()) {
                edtSearchDirection.setText(getString(R.string.label_my_location))
                edtSearchDirection.text?.length?.let { it1 -> edtSearchDirection.setSelection(it1) }
            } else {
                edtSearchDest.setText(getString(R.string.label_my_location))
                edtSearchDest.text?.length?.let { it1 -> edtSearchDest.setSelection(it1) }
            }
            enableDirectionSearch()
            requireActivity().hideKeyboard()
            clMyLocation.root.hide()
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                if (edtSearchDest.text.toString().trim()
                        .isNotEmpty() && mViewModel.mSearchDirectionDestinationData != null
                ) {
                    mViewModel.mSearchDirectionDestinationData?.let { it1 ->
                        showCurrentLocationDestinationRoute(
                            it1,
                        )
                    }
                } else {
                    hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
                }
            } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                if (edtSearchDirection.text.toString().trim()
                        .isNotEmpty() && mViewModel.mSearchDirectionOriginData != null
                ) {
                    mViewModel.mSearchDirectionOriginData?.let { it1 ->
                        showCurrentLocationOriginRoute(
                            it1,
                        )
                    }
                } else {
                    hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
                }
            }
        }
    }

    private fun checkMyLocationUI(
        text: CharSequence?,
        edtSearch: TextInputEditText,
    ) {
        if (!text.isNullOrEmpty() && text.toString() == getString(R.string.label_my_location)) {
            mBinding.bottomSheetDirectionSearch.clMyLocation.root.hide()
        } else if (!edtSearch.text.isNullOrEmpty() && edtSearch.text.toString() == getString(
                R.string.label_my_location,
            )
        ) {
            mBinding.bottomSheetDirectionSearch.clMyLocation.root.hide()
        } else {
            if (mBinding.bottomSheetDirectionSearch.cardMapOption.visibility == View.GONE) {
                mBinding.bottomSheetDirectionSearch.clMyLocation.root.show()
            }
        }
    }

    private fun FragmentExploreBinding.hideArrivedBottomSheet() {
        bottomSheetNavigationComplete.tvNavigationCompleteAddress.text = ""
        mBottomSheetHelper.hideNavigationCompleteSheet()
        clearNavigationData()
    }

    private fun adjustMapBound() {
        val latLngList: ArrayList<LatLng> = ArrayList()
        if (mViewModel.mSearchDirectionDestinationData?.isDestination == true) {
            mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.latitude?.let {
                mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1,
                    )
                }
            }?.let {
                latLngList.add(
                    it,
                )
            }
        } else if (mViewModel.mSearchDirectionOriginData?.isDestination == true) {
            mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.latitude?.let {
                mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1,
                    )
                }
            }?.let {
                latLngList.add(
                    it,
                )
            }
        }

        if (mViewModel.mSearchDirectionOriginData != null) {
            mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.latitude?.let {
                mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1,
                    )
                }
            }?.let {
                latLngList.add(
                    it,
                )
            }
        }
        mMapHelper.getLiveLocation()?.let { it1 -> latLngList.add(it1) }
        mMapHelper.adjustMapBounds(
            latLngList,
            resources.getDimension(R.dimen.dp_90).roundToInt(),
        )
    }

    private fun clearNavigationData() {
        clearNavigationSheetData()
        mViewModel.mStartLatLng = null
        mViewModel.mDestinationLatLng = null
        mViewModel.mNavigationResponse = null
        hideDirectionBottomSheet()
    }

    private fun clearNavigationExitData() {
        clearNavigationSheetData()
        mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
        showDirectionAndCurrentLocationIcon()
        adjustMapBound()
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.cardDirection.hide()
                mBinding.cardNavigation.show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearNavigationSheetData() {
        mBinding.bottomSheetNavigation.apply {
            tvNavigationDistance.text = ""
            tvNavigationTime.text = ""
        }
        mMapHelper.addLiveLocationMarker(false)
        mBinding.tvDistance.text = ""
        mBinding.tvNavigationName.text = ""
        mBinding.bottomSheetDirectionSearch.clDrive.performClick()
        mRouteFinish = true
        mNavigationList.clear()
        mMapHelper.removeLocationListener()
        mMapboxMap?.removeOnScaleListener(this)
        mBinding.cardNavigationTimeDialog.hide()
        mNavigationAdapter?.notifyDataSetChanged()
        mBottomSheetHelper.hideNavigationSheet()
    }

    private fun drawPolyLineOnMapCardClick(
        legs: List<Leg>,
        isLineUpdate: Boolean,
        isWalk: Boolean = false,
        isLocationIcon: Boolean,
        sourceLatLng: LatLng? = null,
        destinationLatLng: LatLng? = null,
    ) {
        val lineString = arrayListOf<Point>()

        val dotStartPoint = arrayListOf<Point>()
        val dotDestinationPoint = arrayListOf<Point>()

        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        false,
                    )
                } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        true,
                    )
                } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
                    setMapLineData(
                        sourceLatLng,
                        dotStartPoint,
                        legs,
                        destinationLatLng,
                        dotDestinationPoint,
                        false,
                    )
                }
            }
        }
        for (data in legs[0].geometry.lineString) {
            lineString.add(fromLngLat(data[0], data[1]))
        }
        if (isLineUpdate) {
            mMapHelper.updateLine(lineString)
            mMapHelper.removeStartDot()
        } else {
            mMapHelper.addLine(lineString, isWalk)
            mBinding.apply {
                bottomSheetDirectionSearch.apply {
                    if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                        mMapHelper.addStartDot(dotStartPoint)
                    } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                        mMapHelper.addStartDot(dotStartPoint)
                    }
                }
            }
            mMapHelper.addDotDestination(dotDestinationPoint)
        }
        mBinding.apply {
            bottomSheetDirectionSearch.apply {
                if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                    mMapHelper.addLiveLocationMarker(isLocationIcon)
                }
            }
        }
    }

    private fun setMapLineData(
        sourceLatLng: LatLng?,
        dotStartPoint: ArrayList<Point>,
        legs: List<Leg>,
        destinationLatLng: LatLng?,
        dotDestinationPoint: ArrayList<Point>,
        isDestination: Boolean,
    ) {
        if (sourceLatLng != null) {
            dotStartPoint.add(
                fromLngLat(
                    sourceLatLng.longitude,
                    sourceLatLng.latitude,
                ),
            )
        } else {
            if (isDestination) {
                mViewModel.mDestinationLatLng?.longitude?.let {
                    mViewModel.mDestinationLatLng?.latitude?.let { it1 ->
                        fromLngLat(
                            it,
                            it1,
                        )
                    }
                }?.let {
                    dotStartPoint.add(
                        it,
                    )
                }
            } else {
                mMapHelper.getLiveLocation()?.longitude?.let {
                    mMapHelper.getLiveLocation()?.latitude?.let { it1 ->
                        fromLngLat(
                            it,
                            it1,
                        )
                    }
                }?.let {
                    dotStartPoint.add(
                        it,
                    )
                }
            }
        }
        if (isDestination) {
            val lastLeg = legs[0].geometry.lineString[legs[0].geometry.lineString.size - 1]
            dotStartPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
        } else {
            dotStartPoint.add(
                fromLngLat(
                    legs[0].geometry.lineString[0][0],
                    legs[0].geometry.lineString[0][1],
                ),
            )
        }
        if (destinationLatLng != null) {
            dotDestinationPoint.add(
                fromLngLat(
                    destinationLatLng.longitude,
                    destinationLatLng.latitude,
                ),
            )
        } else {
            if (isDestination) {
                mViewModel.mStartLatLng?.longitude?.let {
                    mViewModel.mStartLatLng?.latitude?.let { it1 ->
                        fromLngLat(
                            it,
                            it1,
                        )
                    }
                }?.let {
                    dotDestinationPoint.add(
                        it,
                    )
                }
            } else {
                mViewModel.mDestinationLatLng?.latitude?.let {
                    mViewModel.mDestinationLatLng?.longitude?.let { it1 ->
                        fromLngLat(
                            it1,
                            it,
                        )
                    }
                }?.let {
                    dotDestinationPoint.add(
                        it,
                    )
                }
            }
        }
        if (isDestination) {
            dotDestinationPoint.add(
                fromLngLat(
                    legs[0].geometry.lineString[0][0],
                    legs[0].geometry.lineString[0][1],
                ),
            )
        } else {
            val lastLeg = legs[0].geometry.lineString[legs[0].geometry.lineString.size - 1]
            dotDestinationPoint.add(fromLngLat(lastLeg[0], lastLeg[1]))
        }
    }

    private fun drawPolyLineOnMap(
        legs: List<Leg>,
        isLineUpdate: Boolean,
        isWalk: Boolean = false,
        isLocationIcon: Boolean,
        sourceLatLng: LatLng? = null,
        destinationLatLng: LatLng? = null,
    ) {
        val lineString = arrayListOf<Point>()

        val dotStartPoint = arrayListOf<Point>()
        val dotDestinationPoint = arrayListOf<Point>()

        setMapLineData(
            sourceLatLng,
            dotStartPoint,
            legs,
            destinationLatLng,
            dotDestinationPoint,
            false,
        )
        for (data in legs[0].geometry.lineString) {
            lineString.add(fromLngLat(data[0], data[1]))
        }
        if (isLineUpdate) {
            mMapHelper.updateLine(lineString)
            mMapHelper.removeStartDot()
        } else {
            mMapHelper.addLine(lineString, isWalk)
            mMapHelper.addStartDot(dotStartPoint)
            mMapHelper.addDotDestination(dotDestinationPoint)
        }
        mMapHelper.addLiveLocationMarker(isLocationIcon)
    }

    private fun fetchAddressFromLatLng(it: CalculateRouteResult) {
        mRouteFinish = false
        mViewModel.calculateNavigationLine(it)
        mBottomSheetHelper.showNavigationSheet()
        mBottomSheetHelper.hideDirectionSearch(this@ExploreFragment)
        mBaseActivity?.isTablet?.let {
            if (it) {
                mBinding.bottomSheetNavigation.cardNavigationLocation.hide()
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.changeRouteListUI() {
        if (mIsRouteOptionsOpened) {
            ivUp.show()
            cardRoutingOption.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white),
            )
            ivDown.hide()
            cardListRoutesOption.show()
            cardRoutingOption.shapeAppearanceModel =
                cardRoutingOption.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setTopRightCorner(CornerFamily.ROUNDED, 12f).setBottomRightCornerSize(0F)
                    .setBottomLeftCornerSize(0F).build()
            cardListRoutesOption.shapeAppearanceModel =
                cardListRoutesOption.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 12f)
                    .setBottomRightCorner(CornerFamily.ROUNDED, 12f)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 12f).setTopRightCornerSize(0f)
                    .build()
        } else {
            routeOptionClose()
        }
    }

    private fun BottomSheetDirectionSearchBinding.routeOptionClose() {
        mIsRouteOptionsOpened = false
        mIsAvoidTolls = mPreferenceManager.getValue(KEY_AVOID_TOLLS, false)
        mIsAvoidFerries = mPreferenceManager.getValue(KEY_AVOID_FERRIES, false)
        ivDown.show()
        cardRoutingOption.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_route_option_unselected,
            ),
        )
        hideViews(cardListRoutesOption, ivUp)
        cardRoutingOption.radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            requireContext().resources.displayMetrics,
        )
        cardMapOption.radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            requireContext().resources.displayMetrics,
        )
    }

    private fun BottomSheetDirectionSearchBinding.clearDirectionData() {
        clearTruckAndWalkData()
        tvDriveDistance.text = ""
        tvDriveMinute.text = ""
    }

    private fun BottomSheetDirectionSearchBinding.clearTruckAndWalkData() {
        tvTruckMinute.text = ""
        tvTruckDistance.text = ""
        tvWalkMinute.text = ""
        tvWalkDistance.text = ""
    }

    private fun clearMapLineMarker() {
        mMapHelper.removeLine()
        mMapHelper.clearMarker()
    }

    fun hideDirectionBottomSheet() {
        mBinding.bottomSheetDirectionSearch.edtSearchDirection.setText("")
        mBinding.bottomSheetDirectionSearch.edtSearchDest.setText("")
        clearDirectionBottomSheet()
        mIsDirectionDataSet = false
        mViewModel.mIsPlaceSuggestion = true
        mBinding.bottomSheetDirectionSearch.apply {
            routeOptionClose()
        }
        activity?.hideKeyboard()
    }

    fun showDirectionAndCurrentLocationIcon() {
        mBinding.let {
            showViews(
                it.cardDirection,
                it.cardNavigation,
            )
        }
    }

    fun showCurrentLocationIcon() {
        mBinding.let {
            it.cardDirection.hide()
            it.cardNavigation.show()
        }
    }

    fun hideDirectionAndCurrentLocationIcon() {
        hideViews(
            mBinding.cardDirection,
            mBinding.cardNavigation,
        )
    }

    // clear direction sheet with data
    private fun clearDirectionBottomSheet() {
        lifecycleScope.launch {
            mViewModel.mSearchDirectionOriginData = null
            mViewModel.mSearchDirectionDestinationData = null
            mViewModel.mCarData = null
            mViewModel.mWalkingData = null
            mViewModel.mTruckData = null
            mTravelMode = TravelMode.Car.value
            mBinding.bottomSheetDirectionSearch.apply {
                tvDriveSelected.show()
                hideViews(tvTruckSelected, tvWalkSelected, layoutCardError.root)
            }
            showViews(
                mBinding.cardDirection,
                mBinding.cardNavigation,
                mBinding.cardMap,
                mBinding.cardGeofenceMap,
            )
            clearMapLineMarker()
            clearSearchList()
            mBaseActivity?.bottomNavigationVisibility(true)
            mBottomSheetHelper.hideSearchBottomSheet(false)
            mBottomSheetHelper.hideDirectionSheet()
            mViewModel.mSearchSuggestionData = null
            mMapHelper.getLiveLocation()?.let { it1 ->
                mMapHelper.moveCameraToLocation(it1)
            }
        }
    }

    // clear search list
    private fun clearSearchList() {
        mViewModel.mIsPlaceSuggestion = true
        mBinding.bottomSheetSearch.apply {
            layoutNoDataFound.groupNoSearchFound.hide()
            nsSearchPlaces.show()
        }
        notifyAdapters()
    }

    // update search ui
    private fun updateSearchUI(isSearchText: Boolean = false) {
        mBinding.bottomSheetSearch.apply {
            if (isSearchText) {
                clSearchLoaderSearchSheet.root.hide()
                clNoInternetConnectionSearchSheet.hide()
                ivClose.hide()
                cardUserProfile.hide()
                clearSearchList()
            } else {
                ivClose.show()
                cardUserProfile.hide()
            }
        }
    }

    // update search ui
    @SuppressLint("NotifyDataSetChanged")
    private fun updateDirectionSearchUI(isSearchText: Boolean = false) {
        mBinding.bottomSheetDirectionSearch.apply {
            if (isSearchText) {
                hideViews(
                    rvSearchPlacesDirection,
                    rvSearchPlacesSuggestionDirection,
                    clSearchLoaderDirectionSearch.root,
                )
                mPlaceList.clear()
                mAdapterDirection?.notifyDataSetChanged()
                mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceDirectionAdapter() {
        mBinding.bottomSheetDirectionSearch.apply {
            rvSearchPlacesDirection.layoutManager = LinearLayoutManager(requireContext())
            mAdapterDirection =
                SearchPlacesAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    object : SearchPlacesAdapter.SearchPlaceInterface {
                        override fun placeClick(position: Int) {
                            if (checkInternetConnection()) {
                                mIsDirectionDataSet = true
                                setPlaceData(position)
                                notifyDirectionAdapters()
                            }
                        }
                    },
                )
            rvSearchPlacesDirection.adapter = mAdapterDirection
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceDirectionSuggestionAdapter() {
        mBinding.bottomSheetDirectionSearch.apply {
            rvSearchPlacesSuggestionDirection.layoutManager = LinearLayoutManager(requireContext())
            mSearchPlacesDirectionSuggestionAdapter = SearchPlacesSuggestionAdapter(
                mPlaceList,
                mPreferenceManager,
                object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                    override fun suggestedPlaceClick(position: Int) {
                        if (checkInternetConnection()) {
                            mViewModel.mIsPlaceSuggestion = false
                            mIsDirectionDataSet = true
                            if (edtSearchDirection.hasFocus()) {
                                edtSearchDirection.setText(mPlaceList[position].text)
                                edtSearchDirection.text?.length?.let { edtSearchDirection.setSelection(it) }
                            } else {
                                edtSearchDest.setText(mPlaceList[position].text)
                                edtSearchDest.text?.length?.let { edtSearchDest.setSelection(it) }
                            }
                            if (mPlaceList[position].placeId.isNullOrEmpty()) {
                                mPlaceList[position].text?.let {
                                    mViewModel.searchPlaceIndexForText(
                                        it,
                                    )
                                }
                            } else {
                                setPlaceData(position)
                            }
                            notifyDirectionAdapters()
                        }
                    }
                },
            )
            rvSearchPlacesSuggestionDirection.adapter = mSearchPlacesDirectionSuggestionAdapter
        }
    }

    private fun BottomSheetDirectionSearchBinding.setPlaceData(position: Int) {
        activity?.hideKeyboard()
        changeRouteListUI()
        mPlaceList[position].let {
            if (edtSearchDirection.hasFocus()) {
                edtSearchDirection.setText(it.text)
                mViewModel.mSearchDirectionOriginData = it
                mViewModel.mSearchDirectionOriginData?.isDestination = false
            } else {
                edtSearchDest.setText(it.text)
                mViewModel.mSearchDirectionDestinationData = it
                mViewModel.mSearchDirectionDestinationData?.isDestination = true
            }
            edtSearchDirection.clearFocus()
            edtSearchDest.clearFocus()
            clMyLocation.root.hide()
            enableDirectionSearch()
            if (edtSearchDirection.text.toString() == resources.getString(R.string.label_my_location)) {
                showCurrentLocationDestinationRoute(it)
            } else if (edtSearchDest.text.toString() == resources.getString(R.string.label_my_location)) {
                showCurrentLocationOriginRoute(it)
            } else if (!edtSearchDirection.text.isNullOrEmpty() && !edtSearchDest.text.isNullOrEmpty()) {
                showOriginToDestinationRoute()
            }
        }
    }

    private fun enableDirectionSearch() {
        lifecycleScope.launch {
            delay(600)
            mIsDirectionDataSet = false
            mViewModel.mIsPlaceSuggestion = true
        }
    }

    private fun BottomSheetDirectionSearchBinding.showOriginToDestinationRoute() {
        clearDirectionData()
        setApiError()
        mViewModel.calculateDistance(
            latitude = mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.latitude,
            longitude = mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.longitude,
            latDestination = mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.latitude,
            lngDestination = mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude = mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.latitude,
            longitude = mViewModel.mSearchDirectionOriginData?.amazonLocationPlace?.coordinates?.longitude,
            latDestination = mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.latitude,
            lngDestination = mViewModel.mSearchDirectionDestinationData?.amazonLocationPlace?.coordinates?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = true,
        )
        showDirectionSearchShimmer()
        mViewModel.mSearchDirectionOriginData?.let { it1 ->
            mMapHelper.addMarker(
                requireActivity(),
                MarkerEnum.ORIGIN_ICON,
                it1,
            )
        }
        mViewModel.mSearchDirectionDestinationData?.let { it1 ->
            mViewModel.mSearchDirectionOriginData?.let { data ->
                mMapHelper.addMarker(
                    requireActivity(),
                    MarkerEnum.DIRECTION_ICON,
                    it1,
                    data,
                )
            }
        }
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardDirection.hide()
                    mBinding.cardNavigation.show()
                }
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.setApiError() {
        layoutCardError.groupCardErrorNoSearchFound.hide()
        layoutCardError.root.hide()
        isCalculateDriveApiError = false
        isCalculateWalkApiError = false
        isCalculateTruckApiError = false
    }

    private fun BottomSheetDirectionSearchBinding.showCurrentLocationDestinationRoute(
        it: SearchSuggestionData,
    ) {
        setApiError()
        clearDirectionData()
        val liveLocationLatLng: LatLng? = if (isRunningTestLiveLocation || isRunningTest2LiveLocation || isRunningTest3LiveLocation) {
            LatLng(22.995545, 72.534031)
        } else {
            mMapHelper.getLiveLocation()
        }
        mViewModel.calculateDistance(
            latitude = liveLocationLatLng?.latitude,
            longitude = liveLocationLatLng?.longitude,
            latDestination = it.amazonLocationPlace?.coordinates?.latitude,
            lngDestination = it.amazonLocationPlace?.coordinates?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude = liveLocationLatLng?.latitude,
            longitude = liveLocationLatLng?.longitude,
            latDestination = it.amazonLocationPlace?.coordinates?.latitude,
            lngDestination = it.amazonLocationPlace?.coordinates?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = true,
        )

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mMapHelper.addMarker(requireActivity(), MarkerEnum.DIRECTION_ICON, it)
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardNavigation.show()
                    mBinding.cardDirection.hide()
                }
            }
        }
    }

    private fun BottomSheetDirectionSearchBinding.showCurrentLocationOriginRoute(
        it: SearchSuggestionData,
    ) {
        setApiError()
        clearDirectionData()
        val liveLocationLatLng = mMapHelper.getLiveLocation()
        mViewModel.calculateDistance(
            latitude = it.amazonLocationPlace?.coordinates?.latitude,
            longitude = it.amazonLocationPlace?.coordinates?.longitude,
            latDestination = liveLocationLatLng?.latitude,
            lngDestination = liveLocationLatLng?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = false,
        )
        mViewModel.calculateDistance(
            latitude = it.amazonLocationPlace?.coordinates?.latitude,
            longitude = it.amazonLocationPlace?.coordinates?.longitude,
            latDestination = liveLocationLatLng?.latitude,
            lngDestination = liveLocationLatLng?.longitude,
            isAvoidFerries = mIsAvoidFerries,
            isAvoidTolls = mIsAvoidTolls,
            isWalkingAndTruckCall = true,
        )

        showDirectionSearchShimmer()
        if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
            mViewModel.mSearchDirectionOriginData?.let { it1 ->
                mMapHelper.addMarker(
                    requireActivity(),
                    MarkerEnum.ORIGIN_ICON,
                    it1,
                )
            }
            mMapHelper.setDirectionMarker(
                liveLocationLatLng?.latitude!!,
                liveLocationLatLng.longitude,
                requireActivity(),
                MarkerEnum.DIRECTION_ICON,
                "",
            )
            mBottomSheetHelper.halfExpandDirectionSearchBottomSheet()
            cardRouteOptionShow()
            mBaseActivity?.isTablet?.let {
                if (it) {
                    mBinding.cardNavigation.show()
                    mBinding.cardDirection.hide()
                }
            }
        }
    }

    private fun showDirectionSearchShimmer() {
        mBinding.bottomSheetDirectionSearch.apply {
            clDriveLoader.show()
            clWalkLoader.show()
            clTruckLoader.show()

            clTruck.invisible()
            clWalk.invisible()
            clDrive.invisible()
        }
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionShow() {
        hideViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection, clMyLocation.root)
        showViews(cardRoutingOption, cardMapOption)
    }

    private fun BottomSheetDirectionSearchBinding.cardRouteOptionHide() {
        showViews(rvSearchPlacesDirection, rvSearchPlacesSuggestionDirection)
        hideViews(cardRoutingOption, cardMapOption, cardListRoutesOption, layoutCardError.root)
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceAdapter() {
        mBinding.bottomSheetSearch.apply {
            rvSearchPlaces.layoutManager = LinearLayoutManager(requireContext())
            mAdapter =
                SearchPlacesAdapter(
                    mPlaceList,
                    mPreferenceManager,
                    object : SearchPlacesAdapter.SearchPlaceInterface {
                        override fun placeClick(position: Int) {
                            if (checkInternetConnection()) {
                                setDirectionData(mPlaceList[position], false)
                            }
                        }
                    },
                )
            rvSearchPlaces.adapter = mAdapter
        }
    }

    /**
     * Set search places adapter
     * Tap on item user navigate to direction sheet
     * */
    private fun setSearchPlaceSuggestionAdapter() {
        mBinding.bottomSheetSearch.apply {
            rvSearchPlacesSuggestion.layoutManager = LinearLayoutManager(requireContext())
            mSearchPlacesSuggestionAdapter = SearchPlacesSuggestionAdapter(
                mPlaceList,
                mPreferenceManager,
                object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                    override fun suggestedPlaceClick(position: Int) {
                        if (checkInternetConnection()) {
                            mViewModel.mIsPlaceSuggestion = false
                            edtSearchPlaces.setText(mPlaceList[position].text)
                            edtSearchPlaces.setSelection(edtSearchPlaces.text.toString().length)
                            if (mPlaceList[position].placeId.isNullOrEmpty()) {
                                mPlaceList[position].text?.let {
                                    mViewModel.searchPlaceIndexForText(
                                        it,
                                    )
                                }
                            } else {
                                setDirectionData(mPlaceList[position], false)
                            }
                            notifyAdapters()
                        }
                    }
                },
            )
            rvSearchPlacesSuggestion.adapter = mSearchPlacesSuggestionAdapter
        }
    }

    // get direction data from places
    private fun setDirectionData(data: SearchSuggestionData, isFromMapClick: Boolean) {
        lifecycleScope.launch {
            activity?.hideKeyboard()
            mBinding.bottomSheetSearch.apply {
                ivClose.hide()
                edtSearchPlaces.clearFocus()
                edtSearchPlaces.setText("")
            }
            mViewModel.mSearchSuggestionData = data
            mViewModel.mSearchDirectionDestinationData = data
            mViewModel.mSearchDirectionDestinationData?.isDestination = true
            hideViews(mBinding.cardDirection, mBinding.cardNavigation)
            mBinding.bottomSheetDirection.apply {
                tvDirectionTime.invisible()
                groupDistance.invisible()
                hideViews(tvDirectionError, tvDirectionError2, ivInfo)
                val liveLocationLatLng = mMapHelper.getLiveLocation()
                isCalculateDriveApiError = false
                mViewModel.calculateDistance(
                    latitude = liveLocationLatLng?.latitude,
                    longitude = liveLocationLatLng?.longitude,
                    latDestination = data.amazonLocationPlace?.coordinates?.latitude,
                    lngDestination = data.amazonLocationPlace?.coordinates?.longitude,
                    isAvoidFerries = mIsAvoidFerries,
                    isAvoidTolls = mIsAvoidTolls,
                    isWalkingAndTruckCall = false,
                )
                if (data.amazonLocationPlace?.label?.let { validateLatLng(it) } != null) {
                    tvDirectionAddress.text = data.amazonLocationPlace?.label
                    sheetDirectionTvDirectionStreet.hide()
                } else {
                    val label = data.amazonLocationPlace?.label
                    if (label != null) {
                        if (label.contains(",")) {
                            val index = label.indexOf(",")
                            val result: String = index.let { label.substring(0, it) }
                            tvDirectionAddress.text = result
                            if (!data.amazonLocationPlace?.region.isNullOrEmpty()) {
                                sheetDirectionTvDirectionStreet.show()
                                sheetDirectionTvDirectionStreet.text =
                                    index.let { label.substring(index + 1, label.length).trim() }
                            } else {
                                sheetDirectionTvDirectionStreet.hide()
                            }
                        } else {
                            tvDirectionAddress.text = label
                        }
                    }
                }
                data.distance?.let {
                    val isMetric = isMetric(mPreferenceManager.getValue(KEY_UNIT_SYSTEM, ""))
                    val showDistance = if (isMetric) {
                        it > 400
                    } else {
                        it > 248.5
                    }
                    if (showDistance) {
                        tvDirectionDistance.text = getMetricsNew(it, isMetric)
                    }
                }
                notifyAdapters()
                mBottomSheetHelper.hideSearchBottomSheet(true)
                mBaseActivity?.bottomNavigationVisibility(false)
                mBottomSheetHelper.expandDirectionSheet()
            }
            mMapHelper.clearMarker()
            mMapHelper.addDirectionMarker(
                requireActivity(),
                MarkerEnum.DIRECTION_ICON,
                mViewModel.mSearchDirectionDestinationData,
                isFromMapClick,
            )
        }
    }

    // clear markers from map and clear places list
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyAdapters() {
        if (mPlaceList.isNotEmpty()) {
            mMapHelper.clearMarker()
            mPlaceList.clear()
            mAdapter?.notifyDataSetChanged()
            mSearchPlacesSuggestionAdapter?.notifyDataSetChanged()
        }
    }

    // clear markers from map and clear places list
    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDirectionAdapters() {
        if (mPlaceList.isNotEmpty()) {
            mPlaceList.clear()
            mAdapterDirection?.notifyDataSetChanged()
            mSearchPlacesDirectionSuggestionAdapter?.notifyDataSetChanged()
        }
    }

    private fun setMap(
        savedInstanceState: Bundle?,
    ) {
        mBinding.mapView.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)
    }

    override fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean) {
        mSignInViewModel.signOutWithAmazon(requireContext(), isDisconnectFromAWSRequired)
        dialog.dismiss()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.addOnMapClickListener(this)
        mAWSLocationHelper.getCognitoCachingCredentialsProvider()?.let {
            HttpRequestUtil.setOkHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor(SigV4Interceptor(it, mServiceName))
                    .build(),
            )
        }
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                ?: getString(R.string.map_light)
        val mapName: String
        val mapStyleName: String
        when (mapStyleNameDisplay) {
            getString(R.string.map_light) -> {
                mapName = ESRI_LIGHT
                mapStyleName = VECTOR_ESRI_TOPOGRAPHIC
            }
            getString(R.string.map_streets) -> {
                mapName = MapNames.ESRI_STREET_MAP
                mapStyleName = MapStyles.VECTOR_ESRI_STREETS
            }
            getString(R.string.map_navigation) -> {
                mapName = MapNames.ESRI_NAVIGATION
                mapStyleName = MapStyles.VECTOR_ESRI_NAVIGATION
            }
            getString(R.string.map_dark_gray) -> {
                mapName = MapNames.ESRI_DARK_GRAY_CANVAS
                mapStyleName = MapStyles.VECTOR_ESRI_DARK_GRAY_CANVAS
            }
            getString(R.string.map_light_gray) -> {
                mapName = MapNames.ESRI_LIGHT_GRAY_CANVAS
                mapStyleName = MapStyles.VECTOR_ESRI_LIGHT_GRAY_CANVAS
            }
            getString(R.string.map_imagery) -> {
                mapName = MapNames.ESRI_IMAGERY
                mapStyleName = MapStyles.RASTER_ESRI_IMAGERY
            }
            resources.getString(R.string.map_contrast) -> {
                mapName = MapNames.HERE_CONTRAST
                mapStyleName = MapStyles.VECTOR_HERE_CONTRAST
            }
            resources.getString(R.string.map_explore) -> {
                mapName = MapNames.HERE_EXPLORE
                mapStyleName = MapStyles.VECTOR_HERE_EXPLORE
            }
            resources.getString(R.string.map_explore_truck) -> {
                mapName = MapNames.HERE_EXPLORE_TRUCK
                mapStyleName = MapStyles.VECTOR_HERE_EXPLORE_TRUCK
            }
            resources.getString(R.string.map_hybrid) -> {
                mapName = MapNames.HERE_HYBRID
                mapStyleName = MapStyles.HYBRID_HERE_EXPLORE_SATELLITE
            }
            resources.getString(R.string.map_raster) -> {
                mapName = MapNames.HERE_IMAGERY
                mapStyleName = MapStyles.RASTER_HERE_EXPLORE_SATELLITE
            }
            else -> {
                mapName = ESRI_LIGHT
                mapStyleName = VECTOR_ESRI_TOPOGRAPHIC
            }
        }
        mMapHelper.initSymbolManager(mBinding.mapView, mapboxMap, mapName, mapStyleName, this, this, activity)
        activity?.let {
            mBaseActivity?.mGeofenceUtils?.setMapBox(
                it,
                mapboxMap,
                mMapHelper,
                mPreferenceManager,
            )
            mBaseActivity?.mTrackingUtils?.setMapBox(
                it,
                mapboxMap,
                mMapHelper,
            )
        }
        mapboxMap.uiSettings.isCompassEnabled = false
        this.mMapboxMap = mapboxMap
        mapboxMap.addOnCameraIdleListener {
            mViewModel.mLatLng = LatLng(
                mapboxMap.cameraPosition.target.latitude,
                mapboxMap.cameraPosition.target.longitude,
            )
        }
    }

    // check gps enable or not
    @SuppressLint("VisibleForTests")
    private fun checkGpsLocationProvider(
        isLocationAlreadyEnabled: Boolean,
        isCurrentLocationClicked: Boolean,
    ) {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(builder.build())
        builder.setAlwaysShow(true)
        this.mIsLocationAlreadyEnabled = isLocationAlreadyEnabled
        this.mIsCurrentLocationClicked = isCurrentLocationClicked
        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                checkAndEnableLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(resolvable.resolution).build()
                        if (isAdded) {
                            gpsActivityResult.launch(intentSenderRequest)
                        }
                    } catch (_: IntentSender.SendIntentException) {
                    } catch (_: ClassCastException) {
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    private fun checkAndEnableLocation() {
        if (mIsTrackingLocationClicked) {
            mIsTrackingLocationClicked = false
            mPreferenceManager.setValue(IS_LOCATION_TRACKING_ENABLE, true)
            mBaseActivity?.mTrackingUtils?.locationPermissionAdded()
        } else {
            if (mIsCurrentLocationClicked) {
                mBaseActivity?.resetLocationPermission()
                mMapHelper.checkLocationComponentEnable()
            } else {
                if (mIsLocationAlreadyEnabled) {
                    mMapHelper.checkLocationComponentEnable()
                } else {
                    mBaseActivity?.resetLocationPermission()
                    mMapHelper.enableLocationComponent()
                    mMapHelper.setInitialLocation()
                }
            }

            mRedirectionType?.let { type ->
                when (type) {
                    RedirectionType.ROUTE_OPTION.name -> {
                        checkRouteData()
                    }
                    RedirectionType.SEARCH_DIRECTION_CAR.name -> {
                        routeOption()
                    }
                    RedirectionType.MY_LOCATION.name -> directionMyLocation()
                }
                mRedirectionType = null
            }
        }
    }

    fun changeDirectionCardMargin(marginBottom: Int) {
        mBaseActivity?.isTablet?.let {
            if (!it) {
                showViews(mBinding.cardDirection, mBinding.cardNavigation)
                val layoutParams: ViewGroup.MarginLayoutParams =
                    mBinding.cardDirection.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(
                    0,
                    0,
                    resources.getDimension(R.dimen.dp_16).toInt(),
                    marginBottom
                )
                mBinding.cardDirection.requestLayout()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        val option = AuthFetchSessionOptions.builder().forceRefresh(true).build()
        Amplify.Auth.fetchAuthSession(
            option,
            {},
            {
                it.printStackTrace()
            },
        )
        mBinding.mapView.onResume()
        if (mBottomSheetHelper.isDirectionSheetVisible()) {
            mBinding.bottomSheetDirection.apply {
                if (tvDirectionError2.isVisible) {
                    if (tvDirectionError2.text.equals(getString(R.string.label_location_permission_denied))) {
                        if (activity?.checkLocationPermission() == true) {
                            if (!isGPSEnabled(requireContext())) {
                                mBinding.cardNavigation.performClick()
                                tvDirectionError2.text =
                                    getString(R.string.label_location_permission_denied)
                            } else {
                                hideKeyBoardAndGetLiveData()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun BottomSheetDirectionBinding.hideKeyBoardAndGetLiveData() {
        hideViews(tvDirectionError, tvDirectionError2, ivInfo)
        mBinding.cardNavigation.performClick()
        lifecycleScope.launch {
            delay(1000)
            mViewModel.mSearchSuggestionData.let {
                val liveLocationLatLng = mMapHelper.getLiveLocation()
                isCalculateDriveApiError = false
                mViewModel.calculateDistance(
                    latitude = liveLocationLatLng?.latitude,
                    longitude = liveLocationLatLng?.longitude,
                    latDestination = it?.amazonLocationPlace?.coordinates?.latitude,
                    lngDestination = it?.amazonLocationPlace?.coordinates?.longitude,
                    isAvoidFerries = mIsAvoidFerries,
                    isAvoidTolls = mIsAvoidTolls,
                    isWalkingAndTruckCall = false,
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mBinding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsActivityResult.unregister()
        mBinding.mapView.onDestroy()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun mapStyleChange(position: Int, innerPosition: Int) {
        clearAllMapData()
        mViewModel.mStyleList.forEach {
            it.mapInnerData?.forEach { innerData ->
                innerData.isSelected = false
            }
        }
        mViewModel.mStyleList[position].mapInnerData?.let {
            it[innerPosition].isSelected = true
            when (it[innerPosition].mapName) {
                getString(R.string.map_light) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        ESRI_LIGHT,
                        VECTOR_ESRI_TOPOGRAPHIC,
                    )
                }
                getString(R.string.map_streets) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.ESRI_STREET_MAP,
                        MapStyles.VECTOR_ESRI_STREETS,
                    )
                }
                getString(R.string.map_navigation) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.ESRI_NAVIGATION,
                        MapStyles.VECTOR_ESRI_NAVIGATION,
                    )
                }
                getString(R.string.map_dark_gray) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.ESRI_DARK_GRAY_CANVAS,
                        MapStyles.VECTOR_ESRI_DARK_GRAY_CANVAS,
                    )
                }
                getString(R.string.map_light_gray) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.ESRI_LIGHT_GRAY_CANVAS,
                        MapStyles.VECTOR_ESRI_LIGHT_GRAY_CANVAS,
                    )
                }
                getString(R.string.map_imagery) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.ESRI_IMAGERY,
                        MapStyles.RASTER_ESRI_IMAGERY,
                    )
                }
                resources.getString(R.string.map_contrast) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.HERE_CONTRAST,
                        MapStyles.VECTOR_HERE_CONTRAST,
                    )
                }
                resources.getString(R.string.map_explore) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.HERE_EXPLORE,
                        MapStyles.VECTOR_HERE_EXPLORE,
                    )
                }
                resources.getString(R.string.map_explore_truck) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.HERE_EXPLORE_TRUCK,
                        MapStyles.VECTOR_HERE_EXPLORE_TRUCK,
                    )
                }
                resources.getString(R.string.map_hybrid) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.HERE_HYBRID,
                        MapStyles.HYBRID_HERE_EXPLORE_SATELLITE,
                    )
                }
                resources.getString(R.string.map_raster) -> {
                    mMapHelper.updateStyle(
                        mBinding.mapView,
                        MapNames.HERE_IMAGERY,
                        MapStyles.RASTER_HERE_EXPLORE_SATELLITE,
                    )
                }
            }
            mViewModel.mStyleList[position].styleNameDisplay?.let { it1 ->
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    it1,
                )
            }
            it[innerPosition].mapName?.let { it1 ->
                mPreferenceManager.setValue(
                    KEY_MAP_STYLE_NAME,
                    it1,
                )
            }
        }
        mBaseActivity?.isTablet?.let {
            if (it) {
                mapStyleBottomSheetFragment.notifyAdapter()
            } else {
                mMapStyleAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun clearAllMapData() {
        try {
            mBinding.bottomSheetSearch.edtSearchPlaces.setText("")
            mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
            mMapHelper.addLiveLocationMarker(false)
            mBottomSheetHelper.hideDirectionSearchBottomSheet(this@ExploreFragment)
            hideDirectionBottomSheet()
            mBinding.apply {
                bottomSheetNavigation.apply {
                    showViews(
                        cardDirection,
                        cardNavigation,
                        cardMap,
                        cardGeofenceMap,
                    )
                }
            }
            clearNavigationData()
            if (activity is MainActivity) {
                (activity as MainActivity).moveToExploreScreen()
                (activity as MainActivity).mGeofenceUtils?.hideAllGeofenceBottomSheet()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearKeyboardFocus() {
        mBinding.bottomSheetSearch.edtSearchPlaces.clearFocus()
    }

    override fun onMapClick(point: LatLng): Boolean {
        if (!mBottomSheetHelper.isSearchPlaceSheetVisible() || mBottomSheetHelper.isDirectionSheetVisible()) {
            mMapHelper.clearMarker()
            mMapHelper.setMarker(
                point.latitude,
                point.longitude,
                requireActivity(),
                MarkerEnum.DIRECTION_ICON,
                "",
            )
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            return true
        }
        if (mBaseActivity?.mGeofenceUtils?.isAddGeofenceBottomSheetVisible() == true) {
            mBaseActivity?.mGeofenceUtils?.mapClick(point)
            mViewModel.getAddressLineFromLatLng(point.longitude, point.latitude)
            return true
        }
        return false
    }

    override fun mapLoadedSuccess() {
        mBinding.mapView.contentDescription = "Amazon Map Ready"
        mBinding.groupMapLoad.hide()
    }

    override fun onScaleBegin(detector: StandardScaleGestureDetector) {
        isZooming = true
    }

    override fun onScale(detector: StandardScaleGestureDetector) {
        mMapHelper.getLiveLocation()?.let { mLatLng ->
            mMapboxMap?.easeCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(mLatLng)
                        .build()
                ),
                Durations.CAMERA_DURATION_1000
            )
        }
    }

    override fun onScaleEnd(detector: StandardScaleGestureDetector) {
        isZooming = false
        mMapHelper.getLiveLocation()?.let { mLatLng ->
            mMapHelper.navigationZoomCamera(mLatLng, false)
        }
    }

    override fun onMapStyleChanged(mapStyle: String) {
        val logoResId = when (mapStyle) {
            MapNames.ESRI_LIGHT,
            MapNames.ESRI_STREET_MAP,
            MapNames.ESRI_NAVIGATION,
            MapNames.ESRI_LIGHT_GRAY_CANVAS,
            MapNames.HERE_CONTRAST,
            MapNames.HERE_EXPLORE,
            MapNames.HERE_EXPLORE_TRUCK
            -> R.drawable.ic_amazon_logo_on_light

            MapNames.ESRI_DARK_GRAY_CANVAS,
            MapNames.ESRI_IMAGERY,
            MapNames.HERE_IMAGERY,
            MapNames.HERE_HYBRID
            -> R.drawable.ic_amazon_logo_on_dark

            else -> {
                R.drawable.ic_amazon_logo_on_light
            }
        }
        if(activity is MainActivity) {
            (activity as MainActivity).changeAmazonLogo(logoResId)
        }
        mBinding.bottomSheetSearch.imgAmazonLogoSearchSheet.setImageResource(logoResId)
        mBinding.bottomSheetDirection.imgAmazonLogoDirection?.setImageResource(logoResId)
        mBinding.bottomSheetDirectionSearch.imgAmazonLogoDirectionSearchSheet.setImageResource(logoResId)
        mBinding.bottomSheetNavigation.imgAmazonLogoNavigation.setImageResource(logoResId)
        mBinding.bottomSheetNavigationComplete.imgAmazonLogoNavigationComplete.setImageResource(logoResId)
        mBinding.bottomSheetGeofenceList.imgAmazonLogoGeofenceList?.setImageResource(logoResId)
        mBinding.bottomSheetAddGeofence.imgAmazonLogoAddGeofence?.setImageResource(logoResId)
        mBinding.bottomSheetTracking.imgAmazonLogoTrackingSheet?.setImageResource(logoResId)
        mBinding.bottomSheetMapStyle.imgAmazonLogoMapStyle?.setImageResource(logoResId)
    }
}
