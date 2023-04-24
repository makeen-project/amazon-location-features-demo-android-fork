package com.aws.amazonlocation.utils

import android.location.Location
import android.os.StrictMode
import aws.sdk.kotlin.services.location.model.TravelMode
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.geo.AmazonLocationClient
import com.amazonaws.services.geo.model.AssociateTrackerConsumerRequest
import com.amazonaws.services.geo.model.BatchDeleteDevicePositionHistoryRequest
import com.amazonaws.services.geo.model.BatchDeleteGeofenceRequest
import com.amazonaws.services.geo.model.BatchUpdateDevicePositionRequest
import com.amazonaws.services.geo.model.CalculateRouteCarModeOptions
import com.amazonaws.services.geo.model.CalculateRouteRequest
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.CalculateRouteTruckModeOptions
import com.amazonaws.services.geo.model.Circle
import com.amazonaws.services.geo.model.DevicePositionUpdate
import com.amazonaws.services.geo.model.GeofenceGeometry
import com.amazonaws.services.geo.model.GetDevicePositionHistoryRequest
import com.amazonaws.services.geo.model.GetPlaceRequest
import com.amazonaws.services.geo.model.GetPlaceResult
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.amazonaws.services.geo.model.ListGeofencesRequest
import com.amazonaws.services.geo.model.PutGeofenceRequest
import com.amazonaws.services.geo.model.SearchForSuggestionsResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionRequest
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForSuggestionsRequest
import com.amazonaws.services.geo.model.SearchPlaceIndexForSuggestionsResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForSuggestionsSummary
import com.amazonaws.services.geo.model.SearchPlaceIndexForTextRequest
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.models.Coordinates
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.LocationHistoryResponse
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.utils.GeofenceCons.GEOFENCE_COLLECTION
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Date
import java.util.Locale

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AWSLocationHelper(
    private var mMapHelper: MapHelper,
    private var mPreferenceManager: PreferenceManager
) {

    private var mClient: AmazonLocationClient? = null
    private var mCognitoCredentialsProvider: CognitoCredentialsProvider? = null
    private var mBaseActivity: BaseActivity? = null
    private var apiError = "Please try again later"

    fun initAWSMobileClient(baseActivity: BaseActivity) {
        var region = mPreferenceManager.getValue(KEY_USER_REGION, "")
        if (region.isNullOrEmpty()) {
            region = BuildConfig.DEFAULT_REGION
        }
        mClient = AmazonLocationClient(initCognitoCachingCredentialsProvider())
        mClient?.setRegion(Region.getRegion(region))
        mBaseActivity = baseActivity
    }

    fun initCognitoCachingCredentialsProvider(): CognitoCredentialsProvider? {
        val idToken = mPreferenceManager.getValue(KEY_ID_TOKEN, "")
        var identityPoolId = mPreferenceManager.getValue(KEY_POOL_ID, "")
        val provider = mPreferenceManager.getValue(KEY_PROVIDER, "")
        var region = mPreferenceManager.getValue(KEY_USER_REGION, "")

        if (region.isNullOrEmpty()) {
            region = BuildConfig.DEFAULT_REGION
        }

        if (identityPoolId.isNullOrEmpty()) {
            identityPoolId = BuildConfig.DEFAULT_IDENTITY_POOL_ID
        }
        mCognitoCredentialsProvider = CognitoCredentialsProvider(
            identityPoolId,
            Regions.fromName(region)
        )
        val mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        if (identityPoolId != BuildConfig.DEFAULT_IDENTITY_POOL_ID && mAuthStatus == AuthEnum.SIGNED_IN.name) {
            mCognitoCredentialsProvider?.let {
                idToken?.let { idToken ->
                    it.clear()
                    val login: MutableMap<String, String> = HashMap()
                    login[provider.toString()] = idToken
                    it.logins = login
                }
            }
        }

        mClient =
            AmazonLocationClient(mCognitoCredentialsProvider) // update client based on details
        mClient?.setRegion(Region.getRegion(region))
        return mCognitoCredentialsProvider
    }

    private fun searchPlaceIndexForSuggestions(
        lat: Double?,
        lng: Double?,
        text: String
    ): SearchPlaceIndexForSuggestionsResult? {
        return try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_PLACE_INDEX
                }
                "HERE" -> {
                    HERE_PLACE_INDEX
                }
                else -> ESRI_PLACE_INDEX
            }
            return mClient?.searchPlaceIndexForSuggestions(
                SearchPlaceIndexForSuggestionsRequest().withBiasPosition(arrayListOf(lng, lat))
                    .withText(text).withLanguage(Locale.getDefault().language)
                    .withIndexName(indexName)
                    .withMaxResults(SEARCH_MAX_SUGGESTION_RESULT)
            )
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            SearchPlaceIndexForSuggestionsResult()
        }
    }

    fun getCognitoCachingCredentialsProvider(): CognitoCredentialsProvider? {
        return mCognitoCredentialsProvider
    }

    fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        travelMode: String?
    ): CalculateRouteResult? {
        return try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_ROUTE_CALCULATOR
                }
                "HERE" -> {
                    HERE_ROUTE_CALCULATOR
                }
                else -> ESRI_ROUTE_CALCULATOR
            }
            when (travelMode) {
                TravelMode.Car.value -> {
                    mClient?.calculateRoute(
                        CalculateRouteRequest().withDeparturePosition(
                            lngDeparture,
                            latDeparture
                        ).withDestinationPosition(lngDestination, latDestination)
                            .withCarModeOptions(
                                CalculateRouteCarModeOptions().withAvoidTolls(isAvoidTolls)
                                    .withAvoidFerries(isAvoidFerries)
                            ).withIncludeLegGeometry(true).withDistanceUnit(KILOMETERS)
                            .withDepartNow(true).withTravelMode(travelMode)
                            .withCalculatorName(indexName)
                    )
                }
                TravelMode.Truck.value -> {
                    mClient?.calculateRoute(
                        CalculateRouteRequest().withDeparturePosition(
                            lngDeparture,
                            latDeparture
                        ).withDestinationPosition(lngDestination, latDestination)
                            .withTruckModeOptions(
                                CalculateRouteTruckModeOptions().withAvoidTolls(isAvoidTolls)
                                    .withAvoidFerries(isAvoidFerries)
                            ).withIncludeLegGeometry(true).withDistanceUnit(KILOMETERS)
                            .withDepartNow(true).withTravelMode(travelMode)
                            .withCalculatorName(indexName)
                    )
                }
                else -> {
                    mClient?.calculateRoute(
                        CalculateRouteRequest().withDeparturePosition(
                            lngDeparture,
                            latDeparture
                        ).withDestinationPosition(lngDestination, latDestination)
                            .withIncludeLegGeometry(true).withDistanceUnit(KILOMETERS)
                            .withDepartNow(true).withTravelMode(travelMode)
                            .withCalculatorName(indexName)
                    )
                }
            }
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, "")
            CalculateRouteResult()
        }
    }

    fun searchPlaceSuggestion(
        lat: Double?,
        lng: Double?,
        searchText: String
    ): SearchSuggestionResponse {
        try {
            val liveLocation = mMapHelper.getLiveLocation()
            var isLatLng = false
            val searchPlaceIndexForSuggestionsResult = if (validateLatLng(searchText) != null) {
                isLatLng = true
                val mLatLng = validateLatLng(searchText)
                searchPlaceIndexForPosition(
                    lng = mLatLng?.longitude,
                    lat = mLatLng?.latitude
                )
            } else {
                searchPlaceIndexForSuggestions(
                    lat = lat,
                    lng = lng,
                    text = searchText
                )
            }

            val mList = ArrayList<SearchSuggestionData>()
            val response = SearchSuggestionResponse(
                text = searchPlaceIndexForSuggestionsResult?.summary?.text,
                maxResults = searchPlaceIndexForSuggestionsResult?.summary?.maxResults,
                language = searchPlaceIndexForSuggestionsResult?.summary?.language,
                dataSource = searchPlaceIndexForSuggestionsResult?.summary?.dataSource
            )
            if (isLatLng && searchPlaceIndexForSuggestionsResult?.results.isNullOrEmpty()) {
                addMarkerBasedOnLatLng(response, searchText, mList)
            }

            searchPlaceIndexForSuggestionsResult?.results?.forEach {
                val mSearchSuggestionData: SearchSuggestionData = if (!it.placeId.isNullOrEmpty()) {
                    val getSearchResult = getPlace(it.placeId)
                    SearchSuggestionData(
                        placeId = it.placeId,
                        searchText = searchPlaceIndexForSuggestionsResult.summary.text,
                        text = getSearchResult?.place?.label,
                        amazonLocationPlace = amazonLocationPlace(getSearchResult?.place),
                        distance = getDistance(
                            liveLocation,
                            getSearchResult?.place?.geometry?.point?.get(1)!!,
                            getSearchResult.place?.geometry?.point?.get(0)!!
                        )
                    )
                } else {
                    SearchSuggestionData(text = it.text)
                }
                mList.add(mSearchSuggestionData)
            }
            response.data = mList
            return response
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            return SearchSuggestionResponse(
                error = apiError
            )
        }
    }

    fun getDistance(
        liveLocation: LatLng?,
        destinationLat: Double,
        destinationLng: Double
    ): Double? {
        var distance: Double? = null
        if (liveLocation?.latitude != null) {
            val currentLocation = Location("currentLocation")
            currentLocation.latitude = liveLocation.latitude
            currentLocation.longitude = liveLocation.longitude

            val destinationLocation = Location("destinationLocation")
            destinationLocation.latitude = destinationLat
            destinationLocation.longitude = destinationLng
            distance = currentLocation.distanceTo(destinationLocation).toDouble()
        }
        return distance
    }

    private fun addMarkerBasedOnLatLng(
        mResponse: SearchSuggestionResponse,
        searchText: String,
        mList: ArrayList<SearchSuggestionData>
    ) {
        val mLatLng = validateLatLng(searchText)
        val amazonLocationPlace = AmazonLocationPlace(
            coordinates = Coordinates(
                mLatLng?.latitude!!,
                mLatLng.longitude
            ),
            label = mResponse.text
        )
        val response = SearchSuggestionData(
            searchText = mResponse.text,
            placeId = mResponse.text,
            text = mResponse.text,
            isPlaceIndexForPosition = true,
            amazonLocationPlace = amazonLocationPlace
        )
        mList.add(response)
    }

    fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        text: String?
    ): SearchSuggestionResponse {
        try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_PLACE_INDEX
                }
                "HERE" -> {
                    HERE_PLACE_INDEX
                }
                else -> ESRI_PLACE_INDEX
            }
            val liveLocation = mMapHelper.getLiveLocation()
            val response = mClient?.searchPlaceIndexForText(
                SearchPlaceIndexForTextRequest().withBiasPosition(arrayListOf(lng, lat))
                    .withIndexName(indexName).withText(text)
                    .withLanguage(Locale.getDefault().language)
                    .withMaxResults(SEARCH_MAX_RESULT)
            )
            val searchSuggestionResponse = SearchSuggestionResponse(
                text = response?.summary?.text,
                maxResults = response?.summary?.maxResults,
                language = response?.summary?.language,
                dataSource = response?.summary?.dataSource,
                error = null
            )
            val mList = ArrayList<SearchSuggestionData>()
            if (validateLatLng(text!!) != null && response?.results?.isEmpty()!!) {
                addMarkerBasedOnLatLng(searchSuggestionResponse, text, mList)
            } else {
                response?.results?.forEach {
                    val placeData = SearchSuggestionData(
                        searchText = response.summary.text,
                        amazonLocationPlace = amazonLocationPlace(it?.place),
                        text = it?.place?.label,
                        distance = getDistance(
                            liveLocation,
                            it.place?.geometry?.point?.get(1)!!,
                            it.place?.geometry?.point?.get(0)!!
                        )
                    )
                    mList.add(placeData)
                }
            }
            searchSuggestionResponse.data = mList
            return searchSuggestionResponse
        } catch (e: Exception) {
            mBaseActivity?.handleException(e, apiError)
            return SearchSuggestionResponse(
                error = apiError
            )
        }
    }

    private fun getPlace(placeId: String): GetPlaceResult? {
        return try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_PLACE_INDEX
                }
                "HERE" -> {
                    HERE_PLACE_INDEX
                }
                else -> ESRI_PLACE_INDEX
            }
            mClient?.getPlace(
                GetPlaceRequest().withIndexName(indexName).withPlaceId(placeId)
                    .withLanguage(Locale.getDefault().language)
            )
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            GetPlaceResult()
        }
    }

    private fun searchPlaceIndexForPosition(
        lng: Double?,
        lat: Double?
    ): SearchPlaceIndexForSuggestionsResult {
        try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_PLACE_INDEX
                }
                "HERE" -> {
                    HERE_PLACE_INDEX
                }
                else -> ESRI_PLACE_INDEX
            }
            val indexResponse = mClient?.searchPlaceIndexForPosition(
                SearchPlaceIndexForPositionRequest().withIndexName(indexName)
                    .withLanguage(Locale.getDefault().language).withPosition(
                        arrayListOf(lng, lat)
                    ).withMaxResults(
                        SEARCH_MAX_SUGGESTION_RESULT
                    )
            )
            val list = ArrayList<SearchForSuggestionsResult>()
            indexResponse?.results?.forEach {
                val data = SearchForSuggestionsResult()
                data.placeId = it.placeId
                data.text = it.place.label
                list.add(data)
            }
            val mSearchPlaceIndexForSuggestionsResult = SearchPlaceIndexForSuggestionsResult()
            mSearchPlaceIndexForSuggestionsResult.setResults(list)
            val summary = SearchPlaceIndexForSuggestionsSummary()
            summary.text = indexResponse?.summary?.position?.get(1)
                .toString() + "," + indexResponse?.summary?.position?.get(0).toString()
            summary.maxResults = indexResponse?.summary?.maxResults
            summary.language = indexResponse?.summary?.language
            summary.dataSource = indexResponse?.summary?.dataSource
            mSearchPlaceIndexForSuggestionsResult.summary = summary
            return mSearchPlaceIndexForSuggestionsResult
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            return SearchPlaceIndexForSuggestionsResult()
        }
    }

    fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?
    ): SearchPlaceIndexForPositionResult? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        return try {
            val indexName = when (mPreferenceManager.getValue(KEY_MAP_NAME, "Esri")) {
                "Esri" -> {
                    ESRI_PLACE_INDEX
                }
                "HERE" -> {
                    HERE_PLACE_INDEX
                }
                else -> ESRI_PLACE_INDEX
            }
            return mClient?.searchPlaceIndexForPosition(
                SearchPlaceIndexForPositionRequest().withIndexName(indexName)
                    .withLanguage(Locale.getDefault().language).withPosition(arrayListOf(lat, lng))
                    .withMaxResults(1)
            )
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            SearchPlaceIndexForPositionResult()
        }
    }

    fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?
    ): AddGeofenceResponse {
        val putGeofenceRequest =
            PutGeofenceRequest().withCollectionName(collectionName)
                .withGeofenceId(geofenceId)
        putGeofenceRequest.withGeometry(
            GeofenceGeometry().withCircle(
                Circle().withCenter(
                    arrayListOf(
                        latLng?.longitude,
                        latLng?.latitude
                    )
                ).withRadius(radius)
            )
        )
        return try {
            mClient?.putGeofence(putGeofenceRequest)
            AddGeofenceResponse(isGeofenceDataAdded = true, errorMessage = null)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            AddGeofenceResponse(isGeofenceDataAdded = false, errorMessage = e.message)
        }
    }

    fun getGeofenceList(collectionName: String): GeofenceData {
        return try {
            val response = mClient?.listGeofences(
                ListGeofencesRequest().withCollectionName(
                    collectionName
                )
            )
            GeofenceData(
                response?.entries as ArrayList<ListGeofenceResponseEntry>,
                message = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            GeofenceData(message = e.message)
        }
    }

    fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry): DeleteGeofence {
        return try {
            mClient?.batchDeleteGeofence(
                BatchDeleteGeofenceRequest().withCollectionName(
                    GEOFENCE_COLLECTION
                ).withGeofenceIds(data.geofenceId)
            )
            DeleteGeofence(data = data, position = position)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteGeofence(data = null, errorMessage = e.message)
        }
    }

    fun associateTrackerWithGeofence(trackerName: String, consumerArn: String) {
        val data = AssociateTrackerConsumerRequest().withTrackerName(trackerName)
            .withConsumerArn(consumerArn)
        try {
            mClient?.associateTrackerConsumer(data)
        } catch (e: Exception) {
            // handle error
        }
    }

    fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        date: Date
    ): UpdateBatchLocationResponse {
        val map: HashMap<String, String> = HashMap()
        val identityId = AWSMobileClient.getInstance().identityId
        identityId?.let { identityPId ->
            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    map["region"] = region
                }
                splitStringList[1].let { id ->
                    map["id"] = id
                }
            }
        }
        val devicePositionUpdate =
            DevicePositionUpdate().withPosition(position).withDeviceId(deviceId)
                .withSampleTime(date)
                .withPositionProperties(map)

        val data = BatchUpdateDevicePositionRequest()
            .withTrackerName(trackerName).withUpdates(devicePositionUpdate)
        return try {
            mClient?.batchUpdateDevicePosition(data)
            UpdateBatchLocationResponse(null, true)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            UpdateBatchLocationResponse(e.message, true)
        }
    }

    fun getDevicePositionHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date
    ): LocationHistoryResponse {
        val data = GetDevicePositionHistoryRequest()
            .withTrackerName(trackerName).withDeviceId(deviceId).withStartTimeInclusive(dateStart)
            .withEndTimeExclusive(dateEnd)
        return try {
            val response = mClient?.getDevicePositionHistory(data)
            LocationHistoryResponse(null, response)
        } catch (e: Exception) {
            e.printStackTrace()
            mBaseActivity?.handleException(e)
            LocationHistoryResponse(e.message, null)
        }
    }

    fun deleteDevicePositionHistory(
        trackerName: String,
        deviceId: String
    ): DeleteLocationHistoryResponse {
        val data = BatchDeleteDevicePositionHistoryRequest()
            .withTrackerName(trackerName).withDeviceIds(deviceId)
        return try {
            val response = mClient?.batchDeleteDevicePositionHistory(data)
            DeleteLocationHistoryResponse(null, response)
        } catch (e: Exception) {
            mBaseActivity?.handleException(e)
            DeleteLocationHistoryResponse(e.message, null)
        }
    }
}


