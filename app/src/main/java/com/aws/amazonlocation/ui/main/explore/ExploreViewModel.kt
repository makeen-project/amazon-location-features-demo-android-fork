package com.aws.amazonlocation.ui.main.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.services.location.model.TravelMode
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult
import com.amazonaws.services.geo.model.Step
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.CalculateDistanceResponse
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.NavigationResponse
import com.aws.amazonlocation.data.response.SearchResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapStyles
import com.aws.amazonlocation.utils.Units
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private var getLocationSearchUseCase: LocationSearchUseCase,
) :
    ViewModel() {

    var mLatLng: LatLng? = null
    var mStartLatLng: LatLng? = null
    var mDestinationLatLng: LatLng? = null
    var mIsPlaceSuggestion = true
    var mSearchSuggestionData: SearchSuggestionData? = null
    var mSearchDirectionOriginData: SearchSuggestionData? = null
    var mSearchDirectionDestinationData: SearchSuggestionData? = null
    var mCarData: CalculateRouteResult? = null
    var mWalkingData: CalculateRouteResult? = null
    var mTruckData: CalculateRouteResult? = null
    var mNavigationResponse: NavigationResponse? = null
    private val mNavigationListModel = ArrayList<NavigationData>()
    private val listMapInnerData = arrayListOf<MapStyleInnerData>()
    var mStyleList = ArrayList<MapStyleData>()
    private val _searchForSuggestionsResultList =
        Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
    val searchForSuggestionsResultList: Flow<HandleResult<SearchSuggestionResponse>> =
        _searchForSuggestionsResultList.receiveAsFlow()

    private val _searchLocationList =
        Channel<HandleResult<SearchSuggestionResponse>>(Channel.BUFFERED)
    val mSearchLocationList: Flow<HandleResult<SearchSuggestionResponse>> =
        _searchLocationList.receiveAsFlow()

    private val _calculateDistance =
        Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
    val mCalculateDistance: Flow<HandleResult<CalculateDistanceResponse>> =
        _calculateDistance.receiveAsFlow()

    private val _updateCalculateDistance =
        Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
    val mUpdateCalculateDistance: Flow<HandleResult<CalculateDistanceResponse>> =
        _updateCalculateDistance.receiveAsFlow()

    private val _updateRoute =
        Channel<HandleResult<CalculateDistanceResponse>>(Channel.BUFFERED)
    val mUpdateRoute: Flow<HandleResult<CalculateDistanceResponse>> =
        _updateRoute.receiveAsFlow()

    private val _navigationData =
        Channel<HandleResult<NavigationResponse>>(Channel.BUFFERED)
    val mNavigationData: Flow<HandleResult<NavigationResponse>> =
        _navigationData.receiveAsFlow()

    private val _navigationTimeDialogData =
        Channel<HandleResult<NavigationData>>(Channel.BUFFERED)
    val mNavigationTimeDialogData: Flow<HandleResult<NavigationData>> =
        _navigationTimeDialogData.receiveAsFlow()

    private val _addressLineData =
        Channel<HandleResult<SearchResponse>>(Channel.BUFFERED)
    val addressLineData: Flow<HandleResult<SearchResponse>> =
        _addressLineData.receiveAsFlow()

    fun searchPlaceSuggestion(
        searchText: String,
    ) {
        _searchForSuggestionsResultList.trySend(
            HandleResult.Loading,
        )
        viewModelScope.launch(Dispatchers.IO) {
            getLocationSearchUseCase.searchPlaceSuggestionList(
                mLatLng?.latitude,
                mLatLng?.longitude,
                searchText,
                object : SearchPlaceInterface {
                    override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                        _searchForSuggestionsResultList.trySend(
                            HandleResult.Success(
                                suggestionResponse!!,
                            ),
                        )
                    }

                    override fun internetConnectionError(error: String) {
                        _searchForSuggestionsResultList.trySend(
                            HandleResult.Error(
                                DataSourceException.Error(error),
                            ),
                        )
                    }
                },
            )
        }
    }

    fun searchPlaceIndexForText(
        searchText: String?,
    ) {
        _searchLocationList.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            getLocationSearchUseCase.searchPlaceIndexForText(
                mLatLng?.latitude,
                mLatLng?.longitude,
                searchText,
                object : SearchPlaceInterface {
                    override fun success(searchResponse: SearchSuggestionResponse) {
                        _searchLocationList.trySend(HandleResult.Success(searchResponse))
                    }

                    override fun error(searchResponse: SearchSuggestionResponse) {
                        searchResponse.error?.let {
                            DataSourceException.Error(
                                it,
                            )
                        }?.let {
                            HandleResult.Error(
                                it,
                            )
                        }?.let { _searchLocationList.trySend(it) }
                    }

                    override fun internetConnectionError(error: String) {
                        _searchLocationList.trySend(
                            HandleResult.Error(
                                DataSourceException.Error(
                                    error,
                                ),
                            ),
                        )
                    }
                },
            )
        }
    }

    fun calculateDistance(
        latitude: Double?,
        longitude: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        isWalkingAndTruckCall: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isWalkingAndTruckCall) {
                val two = async {
                    calculateDistanceFromMode(
                        latitude,
                        longitude,
                        latDestination,
                        lngDestination,
                        isAvoidFerries,
                        isAvoidTolls,
                        TravelMode.Walking.value,
                    )
                }
                two.await()
                val three = async {
                    calculateDistanceFromMode(
                        latitude,
                        longitude,
                        latDestination,
                        lngDestination,
                        isAvoidFerries,
                        isAvoidTolls,
                        TravelMode.Truck.value,
                    )
                }
                three.await()
            } else {
                val one = async {
                    calculateDistanceFromMode(
                        latitude,
                        longitude,
                        latDestination,
                        lngDestination,
                        isAvoidFerries,
                        isAvoidTolls,
                        TravelMode.Car.value,
                    )
                }
                one.await()
            }
        }
    }

    private fun calculateDistanceFromMode(
        latitude: Double?,
        longitude: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
    ) {
        _calculateDistance.trySend(HandleResult.Loading)
        mDestinationLatLng = lngDestination?.let { latDestination?.let { it1 -> LatLng(it1, it) } }
        mStartLatLng = longitude?.let { latitude?.let { it1 -> LatLng(it1, it) } }
        getLocationSearchUseCase.calculateRoute(
            latitude,
            longitude,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            travelMode,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    _calculateDistance.trySend(
                        HandleResult.Success(
                            CalculateDistanceResponse(
                                "$travelMode",
                                success,
                                sourceLatLng = if (latitude != null && longitude != null) {
                                    LatLng(latitude, longitude)
                                } else {
                                    null
                                },
                                destinationLatLng = latDestination?.let {
                                    lngDestination?.let { it1 ->
                                        LatLng(
                                            it,
                                            it1,
                                        )
                                    }
                                },
                            ),
                        ),
                    )
                }

                override fun distanceFailed(exception: DataSourceException) {
                    _calculateDistance.trySend(HandleResult.Error(exception))
                }

                override fun internetConnectionError(exception: String) {
                    _calculateDistance.trySend(
                        HandleResult.Error(
                            DataSourceException.Error(
                                exception,
                            ),
                        ),
                    )
                }
            },
        )
    }

    fun updateCalculateDistanceFromMode(
        latitude: Double?,
        longitude: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?,
    ) {
        _updateCalculateDistance.trySend(HandleResult.Loading)
        getLocationSearchUseCase.calculateRoute(
            latitude,
            longitude,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            travelMode,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    _updateCalculateDistance.trySend(
                        HandleResult.Success(
                            CalculateDistanceResponse(
                                "$travelMode",
                                success,
                            ),
                        ),
                    )
                }

                override fun distanceFailed(exception: DataSourceException) {
                    _updateCalculateDistance.trySend(HandleResult.Error(exception))
                }

                override fun internetConnectionError(exception: String) {
                    _updateCalculateDistance.trySend(
                        HandleResult.Error(
                            DataSourceException.Error(
                                exception,
                            ),
                        ),
                    )
                }
            },
        )
    }

    fun calculateNavigationLine(data: CalculateRouteResult) {
        _navigationData.trySend(HandleResult.Loading)
        data.legs?.get(0).let { legs ->
            mNavigationListModel.clear()
            viewModelScope.launch(Dispatchers.IO) {
                val address = async {
                    legs?.steps?.let { steps ->
                        steps.forEach { step ->
                            getAddressFromLatLng(step.startPosition[0], step.startPosition[1], step)
                        }
                    }
                }
                address.await()
                mNavigationResponse = NavigationResponse()
                legs?.let { leg ->
                    mNavigationResponse?.duration = Units.getTime(leg.durationSeconds)
                    mNavigationResponse?.distance = leg.distance!!
                    mNavigationResponse?.startLat = leg.startPosition[0]
                    mNavigationResponse?.startLng = leg.startPosition[1]
                    mNavigationResponse?.endLat = leg.endPosition[0]
                    mNavigationResponse?.endLng = leg.endPosition[1]
                }
                mNavigationResponse?.destinationAddress =
                    mSearchSuggestionData?.amazonLocationPlace?.label
                mNavigationResponse?.navigationList = mNavigationListModel
                mNavigationResponse?.let {
                    _navigationData.trySend(HandleResult.Success(it))
                }
            }
        }
    }

    fun getAddressFromLatLng(
        latitude: Double?,
        longitude: Double?,
        step: Step,
        isTimeDialog: Boolean = false,
    ) {
        _navigationTimeDialogData.trySend(HandleResult.Loading)
        getLocationSearchUseCase.searchNavigationPlaceIndexForPosition(
            latitude,
            longitude,
            step,
            object : NavigationDataInterface {
                override fun getNavigationList(navigationData: NavigationData) {
                    if (isTimeDialog) {
                        _navigationTimeDialogData.trySend(
                            HandleResult.Success(
                                navigationData,
                            ),
                        )
                    } else {
                        mNavigationListModel.add(navigationData)
                    }
                }

                override fun internetConnectionError(error: String) {
                    _navigationTimeDialogData.trySend(
                        HandleResult.Error(
                            DataSourceException.Error(
                                error,
                            ),
                        ),
                    )
                }
            },
        )
    }
    fun getAddressLineFromLatLng(
        longitude: Double?,
        latitude: Double?,
    ) {
        _navigationTimeDialogData.trySend(HandleResult.Loading)
        getLocationSearchUseCase.searPlaceIndexForPosition(
            longitude,
            latitude,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    _addressLineData.trySend(
                        HandleResult.Success(
                            SearchResponse(searchPlaceIndexForPositionResult, latitude, longitude),
                        ),
                    )
                }

                override fun error(error: String) {
                    _addressLineData.trySend(
                        HandleResult.Success(
                            SearchResponse(null, latitude, longitude),
                        ),
                    )
                }
                override fun internetConnectionError(error: String) {
                    _addressLineData.trySend(
                        HandleResult.Error(
                            DataSourceException.Error(
                                error,
                            ),
                        ),
                    )
                }
            },
        )
    }

    fun setMapListData(context: Context) {
        mStyleList.clear()
        listMapInnerData.clear()
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_light),
                false,
                R.drawable.light,
            ),
        )
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_streets),
                false,
                R.drawable.streets,
            ),
        )
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_navigation),
                false,
                R.drawable.navigation,
            ),
        )
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_dark_gray),
                false,
                R.drawable.dark_gray,
            ),
        )
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_light_gray),
                false,
                R.drawable.light_gray,
            ),
        )
        listMapInnerData.add(
            MapStyleInnerData(
                context.getString(R.string.map_imagery),
                false,
                R.drawable.imagery,
            ),
        )
        mStyleList.add(MapStyleData(context.getString(R.string.map_esri), true, listMapInnerData))

        val hereList = ArrayList<MapStyleInnerData>()

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_contrast),
                image = R.mipmap.ic_here_contrast,
                isSelected = false,
                mMapName = MapNames.HERE_CONTRAST,
                mMapStyleName = MapStyles.VECTOR_HERE_CONTRAST,
            ),
        )
        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_explore),
                image = R.mipmap.ic_here_explore,
                isSelected = false,
                mMapName = MapNames.HERE_EXPLORE,
                mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE,
            ),
        )

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_explore_truck),
                image = R.mipmap.ic_here_explore_truck,
                isSelected = false,
                mMapName = MapNames.HERE_EXPLORE_TRUCK,
                mMapStyleName = MapStyles.VECTOR_HERE_EXPLORE_TRUCK,
            ),
        )

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_raster),
                image = R.mipmap.ic_here_imagery,
                isSelected = false,
                mMapName = MapNames.HERE_IMAGERY,
                mMapStyleName = MapStyles.RASTER_HERE_EXPLORE_SATELLITE,
            ),
        )

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_hybrid),
                image = R.mipmap.ic_here_hybrid,
                isSelected = false,
                mMapName = MapNames.HERE_HYBRID,
                mMapStyleName = MapStyles.HYBRID_HERE_EXPLORE_SATELLITE,
            ),
        )
        mStyleList.add(MapStyleData(context.resources.getString(R.string.here), false, hereList))
    }
}
