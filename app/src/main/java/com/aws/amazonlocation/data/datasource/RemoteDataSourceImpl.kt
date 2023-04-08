package com.aws.amazonlocation.data.datasource

import android.app.Activity
import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.HostedUIOptions
import com.amazonaws.mobile.client.SignInUIOptions
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.amazonaws.services.geo.model.Step
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.isInternetAvailable
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class RemoteDataSourceImpl(var mContext: Context, var mAWSLocationHelper: AWSLocationHelper) :
    RemoteDataSource {

    override fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface
    ) {
        if (mContext.isInternetAvailable()) {
            val mSearchSuggestionResponse =
                mAWSLocationHelper.searchPlaceSuggestion(lat, lng, searchText)
            if (mSearchSuggestionResponse.text == searchText) {
                searchPlace.getSearchPlaceSuggestionResponse(mSearchSuggestionResponse)
            } else {
                if (!mSearchSuggestionResponse.error.isNullOrEmpty()) {
                    searchPlace.error(mSearchSuggestionResponse)
                }
            }
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    ) {
        if (mContext.isInternetAvailable()) {
            if (!searchText.isNullOrEmpty()) {
                val response =
                    mAWSLocationHelper.searchPlaceIndexForText(
                        lat = lat,
                        lng = lng,
                        text = searchText
                    )
                if (response.text == searchText) {
                    searchPlace.success(response)
                } else {
                    if (!response.error.isNullOrEmpty()) {
                        searchPlace.error(response)
                    }
                }
            }
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override fun signInWithAmazon(activity: Activity, signInInterface: SignInInterface) {
        (activity as MainActivity).showProgress()
        val hostedUIOptions = HostedUIOptions.builder()
            .scopes("openid", "email", "profile")
            .build()
        val signInUIOptions = SignInUIOptions.builder()
            .hostedUIOptions(hostedUIOptions)
            .build()

        AWSMobileClient.getInstance()
            .showSignIn(
                activity,
                signInUIOptions,
                object : Callback<UserStateDetails> {
                    override fun onResult(result: UserStateDetails) {
                        val mLoginResponse = LoginResponse()
                        mLoginResponse.success =
                            mContext.resources.getString(R.string.login_success)
                        mLoginResponse.name = AWSMobileClient.getInstance().username

                        if (result.details.containsKey("token")) {
                            mLoginResponse.idToken = result.details["token"]
                        }

                        if (result.details.containsKey("provider")) {
                            mLoginResponse.provider = result.details["provider"]
                        }

                        signInInterface.getUserDetails(mLoginResponse)
                    }

                    override fun onError(e: Exception) {
                        activity.hideProgress()
                        signInInterface.signInFailed(e.message)
                    }
                }
            )
    }

    override fun signOutWithAmazon(
        context: Context,
        isDisconnectFromAWSRequired: Boolean,
        signInInterface: SignInInterface
    ) {
        try {
            AWSMobileClient.getInstance().signOut()
            signInInterface.signOutSuccess(
                context.resources.getString(R.string.sign_out_successfully),
                isDisconnectFromAWSRequired
            )
        } catch (e: Exception) {
            signInInterface.signOutFailed(context.resources.getString(R.string.sign_out_failed))
        }
    }

    override fun calculateRoute(
        latDeparture: Double?,
        lngDeparture: Double?,
        latDestination: Double?,
        lngDestination: Double?,
        isAvoidFerries: Boolean?,
        isAvoidTolls: Boolean?,
        distanceType: String?,
        distanceInterface: DistanceInterface
    ) {
        val mSearchSuggestionResponse = mAWSLocationHelper.calculateRoute(
            latDeparture,
            lngDeparture,
            latDestination,
            lngDestination,
            isAvoidFerries,
            isAvoidTolls,
            distanceType
        )

        if (mContext.isInternetAvailable()) {
            mSearchSuggestionResponse?.let {
                if (it.summary != null) {
                    distanceInterface.distanceSuccess(it)
                } else {
                    distanceInterface.distanceFailed(
                        DataSourceException.Error(
                            distanceType!!
                        )
                    )
                }
            }
        } else {
            distanceInterface.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override fun searchNavigationPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        step: Step,
        searchPlace: NavigationDataInterface
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mAWSLocationHelper.searchNavigationPlaceIndexForPosition(lat, lng)
            val navigationData = NavigationData()
            navigationData.duration = step.durationSeconds
            navigationData.distance = step.distance
            navigationData.startLat = step.startPosition[0]
            navigationData.startLng = step.startPosition[1]
            navigationData.endLat = step.endPosition[0]
            navigationData.endLng = step.endPosition[1]
            navigationData.destinationAddress = indexResponse?.results?.get(0)?.place?.label
            navigationData.region = indexResponse?.results?.get(0)?.place?.region
            navigationData.subRegion = indexResponse?.results?.get(0)?.place?.subRegion
            navigationData.country = indexResponse?.results?.get(0)?.place?.country
            searchPlace.getNavigationList(navigationData)
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }

    override fun searPlaceIndexForPosition(
        lat: Double?,
        lng: Double?,
        searchPlace: SearchDataInterface
    ) {
        if (mContext.isInternetAvailable()) {
            val indexResponse = mAWSLocationHelper.searchNavigationPlaceIndexForPosition(lat, lng)
            if (indexResponse != null) {
                searchPlace.getAddressData(indexResponse)
            } else {
                searchPlace.error("")
            }
        } else {
            searchPlace.internetConnectionError(mContext.resources.getString(R.string.check_your_internet_connection_and_try_again))
        }
    }
    override suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        val response = mAWSLocationHelper.getGeofenceList(collectionName)
        mGeofenceAPIInterface.getGeofenceList(response)
    }

    override suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        val response = mAWSLocationHelper.addGeofence(geofenceId, collectionName, radius, latLng)
        mGeofenceAPIInterface.addGeofence(response)
    }

    override suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        val response = mAWSLocationHelper.deleteGeofence(position, data)
        mGeofenceAPIInterface.deleteGeofence(response)
    }

    override suspend fun associateTrackerWithGeofence(trackerName: String, consumerArn: String) {
        mAWSLocationHelper.associateTrackerWithGeofence(trackerName, consumerArn)
    }

    override suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        date: Date,
        mTrackingInterface: BatchLocationUpdateInterface
    ) {
        val response =
            mAWSLocationHelper.batchUpdateDevicePosition(trackerName, position, deviceId, date)
        mTrackingInterface.success(response)
    }

    override suspend fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        historyInterface: LocationHistoryInterface
    ) {
        val response =
            mAWSLocationHelper.getDevicePositionHistory(trackerName, deviceId, dateStart, dateEnd)
        historyInterface.success(response)
    }

    override suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface
    ) {
        val response =
            mAWSLocationHelper.deleteDevicePositionHistory(trackerName, deviceId)
        historyInterface.success(response)
    }
}
