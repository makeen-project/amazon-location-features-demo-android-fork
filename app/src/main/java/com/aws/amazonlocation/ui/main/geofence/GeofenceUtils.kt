package com.aws.amazonlocation.ui.main.geofence

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.geo.AmazonLocationClient
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.GeofenceBottomSheetEnum
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.databinding.BottomSheetAddGeofenceBinding
import com.aws.amazonlocation.databinding.BottomSheetGeofenceListBinding
import com.aws.amazonlocation.domain.*
import com.aws.amazonlocation.domain.`interface`.GeofenceInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.explore.SearchPlacesAdapter
import com.aws.amazonlocation.ui.main.explore.SearchPlacesSuggestionAdapter
import com.aws.amazonlocation.utils.*
import com.aws.amazonlocation.utils.Durations.DEFAULT_RADIUS
import com.aws.amazonlocation.utils.GeofenceCons.GEOFENCE_COLLECTION
import com.aws.amazonlocation.utils.geofence_helper.GeofenceHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.DecimalFormat
import java.util.regex.Pattern

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@SuppressLint("NotifyDataSetChanged")
class GeofenceUtils {

    private var mBottomSheetGeofenceListBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var mBottomSheetAddGeofenceBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var mBindingGeofenceList: BottomSheetGeofenceListBinding? = null
    private var mBindingAddGeofence: BottomSheetAddGeofenceBinding? = null

    private var mGeofenceSearchSuggestionAdapter: SearchPlacesSuggestionAdapter? = null
    private var mSearchPlacesAdapter: SearchPlacesAdapter? = null
    private var mPlaceList = ArrayList<SearchSuggestionData>()

    private var mGeofenceListAdapter: GeofenceListAdapter? = null
    private var mGeofenceList = ArrayList<ListGeofenceResponseEntry>()
    private var mFragmentActivity: FragmentActivity? = null
    private var mMapboxMap: MapboxMap? = null
    private var mActivity: Activity? = null
    private var mGeofenceHelper: GeofenceHelper? = null
    private var mClient: AmazonLocationClient? = null
    private var mGeofenceInterface: GeofenceInterface? = null
    private var mMapHelper: MapHelper? = null
    private var mIsBtnEnable = false
    private var connectivityObserver: ConnectivityObserveInterface? = null

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

    fun isGeofenceListExpandedOrHalfExpand(): Boolean {
        return mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isAddGeofenceExpandedOrHalfExpand(): Boolean {
        return mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun collapseGeofenceList() {
        mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun collapseAddGeofence() {
        closeAddGeofence()
    }

    private fun initGeofence() {
        mMapboxMap?.let {
            mGeofenceHelper =
                GeofenceHelper(
                    mActivity!!,
                    mBindingAddGeofence?.tvSeekbarRadius,
                    mBindingAddGeofence?.seekbarGeofenceRadius,
                    mMapboxMap,
                    mMapLatLngListener
                )
            mGeofenceHelper?.initMapBoxStyle()
        }
    }

    fun initGeofenceView(
        fragmentActivity: FragmentActivity?,
        bottomSheetGeofenceList: BottomSheetGeofenceListBinding,
        bindingAddGeofence: BottomSheetAddGeofenceBinding,
        mGeofenceInterface: GeofenceInterface
    ) {
        this.mGeofenceInterface = mGeofenceInterface
        this.mFragmentActivity = fragmentActivity
        this.mBindingGeofenceList = bottomSheetGeofenceList
        this.mBindingAddGeofence = bindingAddGeofence
        initGeofenceListBottomSheet()
        initAddGeofenceBottomSheet()
    }

    fun emptyGeofenceBottomSheetAddBtn() {
        clearAddGeofenceSearch()
        mBindingGeofenceList?.clEmptyGeofenceList?.show()
        mBindingGeofenceList?.clGeofenceList?.hide()
        showAddGeofenceBottomSheet()
    }

    private fun initAddGeofenceBottomSheet() {
        mBindingAddGeofence?.apply {
            mBottomSheetAddGeofenceBehavior = BottomSheetBehavior.from(root)
            mBottomSheetAddGeofenceBehavior?.isFitToContents = false
            mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            setGeofenceSearchSuggestionAdapter()
            setGeofenceSearchPlaceAdapter()
            cardGeofenceLiveLocation.setOnClickListener {
                mMapHelper?.checkLocationComponentEnable()
            }

            cardAddGeofenceClose.setOnClickListener {
                closeAddGeofence()
            }

            btnAddGeofenceSave.setOnClickListener {
                if (checkInternetConnection()) {
                    val geofenceName: String = edtEnterGeofenceName.text.toString()
                    var isGeofenceId = false
                    if (geofenceName.isNotEmpty()) {
                        val pattern = Pattern.compile(GEOFENCE_NAME_REG_EXP)
                        if (pattern.matcher(geofenceName).matches()) {
                            if (mGeofenceList.isEmpty()) {
                                isGeofenceId = true
                            } else {
                                run list@{
                                    mGeofenceList.forEach {
                                        if (geofenceName.trim() == it.geofenceId) {
                                            isGeofenceId = false
                                            return@list
                                        } else {
                                            isGeofenceId = true
                                        }
                                    }
                                }
                            }
                            if (!edtEnterGeofenceName.isEnabled) {
                                addGeofenceAndClearData(geofenceName)
                            } else {
                                if (isGeofenceId) {
                                    addGeofenceAndClearData(geofenceName)
                                } else {
                                    showErrorMessage(
                                        mActivity?.resources?.getString(R.string.geofence_name_exists)
                                            .toString()
                                    )
                                }
                            }
                        } else {
                            showErrorMessage(
                                mActivity?.resources?.getString(R.string.invalid_name)
                                    .toString()
                            )
                        }
                    } else {
                        showErrorMessage(
                            mActivity?.resources?.getString(R.string.please_enter_geofence_name)
                                .toString()
                        )
                    }
                }
            }

            mBottomSheetAddGeofenceBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                showViews(cardGeofenceLiveLocation, imgAmazonLogoAddGeofence)
                                cardGeofenceLiveLocation.alpha = 1f
                                imgAmazonLogoAddGeofence.alpha = 1f
                                ivAmazonInfoAddGeofence.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                cardGeofenceLiveLocation.alpha = 0f
                                imgAmazonLogoAddGeofence.alpha = 0f
                                ivAmazonInfoAddGeofence.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                cardGeofenceLiveLocation.alpha = 1f
                                imgAmazonLogoAddGeofence.alpha = 1f
                                ivAmazonInfoAddGeofence.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
        }
    }

    private fun closeAddGeofence() {
        mBindingAddGeofence?.apply {
            edtEnterGeofenceName.isEnabled = true
            mActivity?.applicationContext?.let {
                edtEnterGeofenceName.setTextColor(
                    ContextCompat.getColor(
                        it,
                        R.color.color_medium_black
                    )
                )
            }
            btnDeleteGeofence.hide()
            edtEnterGeofenceName.setText("")
            edtAddGeofenceSearch.clearFocus()
            mActivity?.let { it1 -> hideKeyboard(it1, edtEnterGeofenceName) }
            edtEnterGeofenceName.clearFocus()
            mGeofenceInterface?.hideShowBottomNavigationBar(
                false,
                GeofenceBottomSheetEnum.ADD_GEOFENCE_BOTTOM_SHEET
            )
        }
    }

    private fun BottomSheetAddGeofenceBinding.addGeofenceAndClearData(
        geofenceName: String
    ) {
        removeGeofenceMarker()
        edtAddGeofenceSearch.clearFocus()
        edtEnterGeofenceName.clearFocus()
        addGeofence(geofenceName.trim())
        mActivity?.let { it1 -> hideKeyboard(it1, edtEnterGeofenceName) }
    }

    private fun showErrorMessage(message: String) {
        (mActivity as MainActivity).showError(message)
    }

    fun addGeofenceCloseBtn(context: Activity) {
        clearAddGeofenceSearch()
        mGeofenceHelper?.clearGeofence()
        hideAddGeofenceBottomSheet()
        changeBottomsheetBackStack(context)
    }

    fun mapClick(mapClickLatLng: LatLng) {
        if (isAddGeofenceBottomSheetVisible()) {
            mGeofenceHelper?.mapClick(mapClickLatLng)
        }
    }
    private fun hideKeyboard(activity: Activity, edtEnterGeofenceName: AppCompatEditText) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtEnterGeofenceName.windowToken, 0)
    }

    private fun clearAddGeofenceSearch() {
        mBindingAddGeofence?.apply {
            edtAddGeofenceSearch.clearFocus()
            groupAddGeofenceName.clearFocus()
            edtAddGeofenceSearch.setText("")
            edtEnterGeofenceName.setText("")
            groupAddGeofenceName.show()
            hideViews(
                layoutNoDataFound.root,
                layoutNoDataFound.groupNoSearchFound,
                nsGeofenceSearchPlaces
            )
        }
        mPlaceList.clear()
        mGeofenceSearchSuggestionAdapter?.notifyDataSetChanged()
    }

    private fun initGeofenceListBottomSheet() {
        mBindingGeofenceList?.apply {
            mBottomSheetGeofenceListBehavior = BottomSheetBehavior.from(root)
            mBottomSheetGeofenceListBehavior?.isHideable = true
            mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetGeofenceListBehavior?.isFitToContents = false
            btnAddGeofence.setOnClickListener {
                mGeofenceInterface?.hideShowBottomNavigationBar(
                    true,
                    GeofenceBottomSheetEnum.EMPTY_GEOFENCE_BOTTOM_SHEET
                )
            }

            clAddGeofence.setOnClickListener {
                if (checkInternetConnection()) {
                    if (mIsBtnEnable) {
                        mGeofenceInterface?.hideShowBottomNavigationBar(
                            true,
                            GeofenceBottomSheetEnum.NONE
                        )
                        clearAddGeofenceSearch()
                        removeGeofenceMarker()
                        if (mGeofenceHelper?.mIsDefaultGeofence == false) {
                            mGeofenceHelper?.setDefaultIconWithGeofence()
                        } else {
                            mGeofenceHelper?.setGeofence()
                        }
                        setLatLngOnTextView()
                        mGeofenceHelper?.mCircleRadius = DEFAULT_RADIUS

                        showAddGeofenceBottomSheet()
                    }
                }
            }

            mBottomSheetGeofenceListBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                imgAmazonLogoGeofenceList.alpha = 1f
                                ivAmazonInfoGeofenceList.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoGeofenceList.alpha = 0f
                                ivAmazonInfoGeofenceList.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoGeofenceList.alpha = 1f
                                ivAmazonInfoGeofenceList.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
        }
    }

    private fun setLatLngOnTextView() {
        mBindingAddGeofence?.edtAddGeofenceSearch?.setText(
            mGeofenceHelper?.mDefaultLatLng?.let { it1 ->
                getLatLngStr(
                    it1
                )
            }
        )
    }

    private fun setGeofenceAdapter() {
        mBindingGeofenceList?.let {
            mGeofenceListAdapter = GeofenceListAdapter(
                mGeofenceList,
                object : GeofenceListAdapter.GeofenceDeleteInterface {
                    override fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry) {
                        if (checkInternetConnection()) {
                            mActivity?.geofenceDeleteDialog(position, data, deleteGeofence)
                        }
                    }

                    override fun editGeofence(position: Int, data: ListGeofenceResponseEntry) {
                        if (checkInternetConnection()) {
                            editGeofenceBottomSheet(position, data)
                        }
                    }
                }
            )
            it.rvGeofence.layoutManager = LinearLayoutManager(it.rvGeofence.context)
            it.rvGeofence.adapter = mGeofenceListAdapter
        }
    }

    fun editGeofenceBottomSheet(position: Int, data: ListGeofenceResponseEntry) {
        clearAddGeofenceSearch()
        removeGeofenceMarker()
        mGeofenceInterface?.hideShowBottomNavigationBar(true, GeofenceBottomSheetEnum.NONE)
        val latLng = LatLng(data.geometry.circle.center[1], data.geometry.circle.center[0])
        mBindingAddGeofence?.tvAddGeofenceSheet?.text =
            mActivity?.resources?.getString(R.string.edit_geofence)
        mBindingAddGeofence?.edtEnterGeofenceName?.setText(data.geofenceId)
        mGeofenceHelper?.mCircleRadius = data.geometry.circle.radius.toInt()
        mBindingAddGeofence?.groupRadius?.show()
        mGeofenceHelper?.mDefaultLatLng = latLng
        mBindingAddGeofence?.seekbarGeofenceRadius?.progress = data.geometry.circle.radius.toInt()
        hideGeofenceListBottomSheet()
        if (mGeofenceHelper?.mIsDefaultGeofence == false) {
            mGeofenceHelper?.setDefaultIconWithGeofence()
        } else {
            mGeofenceHelper?.editGeofence()
        }
        setLatLngOnTextView()
        mBindingAddGeofence?.edtAddGeofenceSearch?.clearFocus()
        mBottomSheetAddGeofenceBehavior?.isHideable = false
        mBindingAddGeofence?.edtEnterGeofenceName?.isEnabled = false
        mActivity?.let {
            mBindingAddGeofence?.edtEnterGeofenceName?.setTextColor(
                ContextCompat.getColor(
                    it.applicationContext,
                    R.color.color_medium_black_opacity_30
                )
            )
        }
        mBindingAddGeofence?.btnDeleteGeofence?.show()
        mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        mBindingAddGeofence?.btnDeleteGeofence?.setOnClickListener {
            mBindingAddGeofence?.edtAddGeofenceSearch?.clearFocus()
            mBindingAddGeofence?.edtEnterGeofenceName?.clearFocus()
            mActivity?.geofenceDeleteDialog(position, data, deleteGeofence)
        }
    }

    private val deleteGeofence = object : GeofenceDeleteInterface {
        override fun deleteGeofence(
            position: Int,
            data: ListGeofenceResponseEntry,
            dialog: DialogInterface
        ) {
            mGeofenceInterface?.deleteGeofence(position, data)
        }
    }

    fun showGeofenceListBottomSheet(context: Activity) {
        connectivityObserver = NetworkConnectivityObserveInterface(context)
        connectivityObserver?.observer()?.onEach {
            when (it) {
                ConnectivityObserveInterface.ConnectionStatus.Available -> {
                    mBindingGeofenceList?.clNoInternetConnectionGeofenceList?.hide()
                    mBindingGeofenceList?.rvGeofence?.show()
                    setGeofenceAdapter()
                    mGeofenceInterface?.getGeofenceList(GEOFENCE_COLLECTION)
                }
                ConnectivityObserveInterface.ConnectionStatus.Lost -> {
                    mBindingGeofenceList?.clNoInternetConnectionGeofenceList?.show()
                    mBindingGeofenceList?.rvGeofence?.hide()
                }
                ConnectivityObserveInterface.ConnectionStatus.Unavailable -> {
                    mBindingGeofenceList?.clNoInternetConnectionGeofenceList?.show()
                    mBindingGeofenceList?.rvGeofence?.hide()
                }
                else -> {
                    mBindingGeofenceList?.clNoInternetConnectionGeofenceList?.show()
                    mBindingGeofenceList?.rvGeofence?.hide()
                }
            }
        }?.launchIn(CoroutineScope(Dispatchers.Main))
        enableAddGeofenceButton(false)
        mIsBtnEnable = false
        mBottomSheetGeofenceListBehavior?.isHideable = false
        mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun enableAddGeofenceButton(isButton: Boolean = false) {
        mBindingGeofenceList?.apply {
            if (isButton) {
                cardAddGeofence.isEnabled = true
                cardAddGeofence.alpha = 1f
            } else {
                cardAddGeofence.isEnabled = false
                cardAddGeofence.alpha = 0.4f
            }
        }
    }

    private fun hideGeofenceListBottomSheet() {
        mBottomSheetGeofenceListBehavior?.isHideable = true
        mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showAddGeofenceBottomSheet() {
        hideGeofenceListBottomSheet()
        if (mGeofenceHelper?.mIsDefaultGeofence == false) {
            mGeofenceHelper?.setDefaultIconWithGeofence()
        } else {
            mGeofenceHelper?.setGeofence()
        }
        setLatLngOnTextView()
        mBindingAddGeofence?.tvAddGeofenceSheet?.text =
            mActivity?.resources?.getString(R.string.add_geofence)
        mBindingAddGeofence?.groupRadius?.show()
        mBindingAddGeofence?.edtAddGeofenceSearch?.clearFocus()
        mBottomSheetAddGeofenceBehavior?.isHideable = false
        mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandAddGeofenceBottomSheet() {
        mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapseAddGeofenceBottomSheet() {
        mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideAddGeofenceBottomSheet() {
        mBottomSheetAddGeofenceBehavior?.isHideable = true
        mBottomSheetAddGeofenceBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun geofenceBottomSheetVisibility(): Boolean {
        return mBottomSheetGeofenceListBehavior?.state != BottomSheetBehavior.STATE_HIDDEN || mBottomSheetAddGeofenceBehavior?.state != BottomSheetBehavior.STATE_HIDDEN
    }

    fun isGeofenceSheetCollapsed(): Boolean {
        return mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isAddGeofenceBottomSheetVisible() =
        mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED || mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED || mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_EXPANDED

    fun hideAllGeofenceBottomSheet() {
        clearAddGeofenceSearch()
        removeGeofenceMarker()
        mGeofenceHelper?.removeMapClickListener()
        mGeofenceHelper = null
        hideGeofenceListBottomSheet()
        hideAddGeofenceBottomSheet()
    }

    private val mMapLatLngListener = object : GeofenceHelper.GeofenceMapLatLngInterface {
        override fun getMapLatLng(latLng: LatLng) {
            mGeofenceHelper?.mDefaultLatLng = latLng
            if (mGeofenceHelper != null && mBottomSheetAddGeofenceBehavior?.state == BottomSheetBehavior.STATE_HIDDEN) {
                if (mGeofenceList.isEmpty()) {
                    mGeofenceInterface?.hideShowBottomNavigationBar(
                        true,
                        GeofenceBottomSheetEnum.EMPTY_GEOFENCE_BOTTOM_SHEET
                    )
                } else {
                    mGeofenceInterface?.hideShowBottomNavigationBar(
                        true,
                        GeofenceBottomSheetEnum.NONE
                    )
                }
                showAddGeofenceBottomSheet()
            }
            removeGeofenceMarker()
            mBindingAddGeofence?.edtAddGeofenceSearch?.setText(getLatLngStr(latLng))
        }
    }

    fun getLatLngStr(latLng: LatLng): String {
        val dFormat = DecimalFormat("#.######")
        val lat = dFormat.format(latLng.latitude)
        val lng = dFormat.format(latLng.longitude)
        return "$lat, $lng"
    }

    private fun addGeofence(geofenceId: String) {
        mGeofenceInterface?.addGeofence(
            geofenceId,
            GEOFENCE_COLLECTION,
            mGeofenceHelper?.mCircleRadius?.toDouble(),
            mGeofenceHelper?.mDefaultLatLng
        )
    }

    fun manageGeofenceListUI(list: ArrayList<ListGeofenceResponseEntry>) {
        initGeofence()
        mGeofenceList.clear()
        mMapHelper?.mSymbolOptionList?.clear()
        mGeofenceList.addAll(list)
        mIsBtnEnable = true
        enableAddGeofenceButton(true)
        if (mGeofenceList.isNotEmpty()) {
            checkGeofenceList(false)
            val mLatLngList = ArrayList<LatLng>()
            mGeofenceList.forEach { data ->
                mMapHelper?.addGeofenceMarker(
                    mActivity!!,
                    data
                )
                mLatLngList.add(
                    LatLng(
                        data.geometry.circle.center[1],
                        data.geometry.circle.center[0]
                    )
                )
            }
            mMapHelper?.adjustMapBounds(
                mLatLngList,
                mActivity?.resources?.getDimension(R.dimen.dp_100)?.toInt()!!
            )
            mGeofenceListAdapter?.notifyDataSetChanged()
        } else {
            checkGeofenceList(true)
        }
    }

    private fun setGeofenceSearchSuggestionAdapter() {
        mBindingAddGeofence?.rvGeofenceSearchPlacesSuggestion?.layoutManager =
            LinearLayoutManager(mActivity)
        mGeofenceSearchSuggestionAdapter = SearchPlacesSuggestionAdapter(
            mPlaceList,
            object : SearchPlacesSuggestionAdapter.SearchPlaceSuggestionInterface {
                override fun suggestedPlaceClick(position: Int) {
                    if (checkInternetConnection()) {
                        if (mPlaceList[position].placeId.isNullOrEmpty()) {
                            mPlaceList[position].text?.let { text ->
                                mGeofenceInterface?.geofenceSearchPlaceIndexForText(text)
                                mBindingAddGeofence?.edtAddGeofenceSearch?.setText(text)
                                mBindingAddGeofence?.edtAddGeofenceSearch?.clearFocus()
                            }
                            mPlaceList.clear()
                            mGeofenceSearchSuggestionAdapter?.notifyDataSetChanged()
                        } else {
                            mBindingAddGeofence?.rvGeofenceSearchPlacesSuggestion?.hide()
                            setGeofenceSearchData(position)
                        }
                    }
                }
            }
        )
        mBindingAddGeofence?.rvGeofenceSearchPlacesSuggestion?.adapter =
            mGeofenceSearchSuggestionAdapter
    }

    private fun setGeofenceSearchData(position: Int) {
        val coordinates = mPlaceList[position].amazonLocationPlace?.coordinates
        val latLng = LatLng(coordinates?.latitude!!, coordinates.longitude)
        mBindingAddGeofence?.let { view ->
            view.edtAddGeofenceSearch.setText(mPlaceList[position].amazonLocationPlace?.label)
            view.edtAddGeofenceSearch.clearFocus()
            view.groupRadius.show()
            view.groupAddGeofenceName.show()
            view.nsGeofenceSearchPlaces.hide()
            if (!view.edtEnterGeofenceName.isEnabled) {
                view.btnDeleteGeofence.show()
            } else {
                view.btnDeleteGeofence.hide()
            }
        }
        mGeofenceHelper?.drawFillCircle(latLng)
        mPlaceList.clear()
        mSearchPlacesAdapter?.notifyDataSetChanged()
        mGeofenceSearchSuggestionAdapter?.notifyDataSetChanged()
    }

    fun updateGeofenceSearchSuggestionList(placeList: ArrayList<SearchSuggestionData>) {
        mBindingAddGeofence?.let { view ->
            if (placeList.isEmpty()) {
                showViews(view.layoutNoDataFound.root, view.layoutNoDataFound.groupNoSearchFound)
                hideViews(view.nsGeofenceSearchPlaces, view.groupAddGeofenceName)
            } else {
                this.mPlaceList.clear()
                this.mPlaceList.addAll(placeList)
                showViews(view.nsGeofenceSearchPlaces, view.rvGeofenceSearchPlacesSuggestion)
                hideViews(
                    view.btnDeleteGeofence,
                    view.groupRadius,
                    view.groupAddGeofenceName,
                    view.rvGeofenceSearchPlaces,
                    view.layoutNoDataFound.root,
                    view.layoutNoDataFound.groupNoSearchFound
                )
            }
        }
        mGeofenceSearchSuggestionAdapter?.notifyDataSetChanged()
    }

    private fun setGeofenceSearchPlaceAdapter() {
        mBindingAddGeofence?.apply {
            rvGeofenceSearchPlaces.layoutManager = LinearLayoutManager(mActivity)
            mSearchPlacesAdapter = SearchPlacesAdapter(
                mPlaceList,
                object : SearchPlacesAdapter.SearchPlaceInterface {
                    override fun placeClick(position: Int) {
                        if (checkInternetConnection()) {
                            rvGeofenceSearchPlaces.hide()
                            setGeofenceSearchData(position)
                        }
                    }
                }
            )
            rvGeofenceSearchPlaces.adapter = mSearchPlacesAdapter
        }
    }

    fun updateGeofenceSearchPlaceList(placeList: ArrayList<SearchSuggestionData>) {
        mBindingAddGeofence?.let { view ->
            if (placeList.isEmpty()) {
                showViews(view.layoutNoDataFound.root, view.layoutNoDataFound.groupNoSearchFound)
                hideViews(view.nsGeofenceSearchPlaces, view.groupAddGeofenceName)
            } else {
                this.mPlaceList.clear()
                this.mPlaceList.addAll(placeList)
                showViews(view.nsGeofenceSearchPlaces, view.rvGeofenceSearchPlaces)

                hideViews(
                    view.btnDeleteGeofence,
                    view.groupRadius,
                    view.groupAddGeofenceName,
                    view.rvGeofenceSearchPlacesSuggestion,
                    view.layoutNoDataFound.root,
                    view.layoutNoDataFound.groupNoSearchFound
                )
            }
        }
        mSearchPlacesAdapter?.notifyDataSetChanged()
    }

    fun showAddGeofenceDefaultUI() {
        mGeofenceHelper?.clearGeofence()
        initGeofence()
        mBindingAddGeofence?.let { view ->
            hideViews(view.nsGeofenceSearchPlaces, view.layoutNoDataFound.root)
            view.groupAddGeofenceName.show()
            view.groupRadius.show()
        }
    }

    fun mangeAddGeofenceUI(context: Activity) {
        mBindingAddGeofence?.edtEnterGeofenceName?.setText("")
        mBindingAddGeofence?.edtEnterGeofenceName?.isEnabled = true
        mActivity?.let {
            mBindingAddGeofence?.edtEnterGeofenceName?.setTextColor(
                ContextCompat.getColor(
                    it.applicationContext,
                    R.color.color_medium_black
                )
            )
        }
        mBindingAddGeofence?.edtEnterGeofenceName?.setText("")
        mBindingAddGeofence?.btnDeleteGeofence?.hide()
        mGeofenceHelper?.clearGeofence()
        checkGeofenceList(false)
        hideAddGeofenceBottomSheet()
        showGeofenceListBottomSheet(context = context)
    }

    fun notifyGeofenceList(position: Int, context: Activity) {
        mMapHelper?.deleteGeofenceMarker(position)
        mangeAddGeofenceUI(context = context)
        mGeofenceList.removeAt(position)
        if (mGeofenceList.isEmpty()) {
            mMapHelper?.checkLocationComponentEnable()
            mMapHelper?.mSymbolOptionList?.clear()
            mMapHelper?.deleteAllGeofenceMarker()
            checkGeofenceList(true)
        }
        mGeofenceListAdapter?.notifyDataSetChanged()
    }

    private fun changeBottomsheetBackStack(context: Activity) {
        showGeofenceListBottomSheet(context = context)
        if (mGeofenceList.isNotEmpty()) {
            checkGeofenceList(false)
        } else {
            checkGeofenceList(true)
        }
    }

    private fun checkGeofenceList(isListEmpty: Boolean = false) {
        if (!isListEmpty) {
            mBindingGeofenceList?.clEmptyGeofenceList?.hide()
            mBindingGeofenceList?.clGeofenceList?.show()
        } else {
            mBindingGeofenceList?.clEmptyGeofenceList?.show()
            mBindingGeofenceList?.clGeofenceList?.hide()
        }
    }

    private fun removeGeofenceMarker() {
        mMapHelper?.deleteAllGeofenceMarker()
    }

    private fun checkInternetConnection(): Boolean {
        return if (mActivity?.isInternetAvailable() == true) {
            true
        } else {
            mActivity?.getString(R.string.check_your_internet_connection_and_try_again)
                ?.let { showErrorMessage(it) }
            false
        }
    }
}
