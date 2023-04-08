package com.aws.amazonlocation.domain.repository

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface GeofenceRepository {

    suspend fun getGeofenceList(collectionName: String, mGeofenceAPIInterface: GeofenceAPIInterface)

    suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface
    )

    suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface
    )

    fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface
    )

    fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    )

    suspend fun associateTrackerWithGeofence(trackerName: String, consumerArn: String)

    suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        date: Date,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    )

    suspend fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        historyInterface: LocationHistoryInterface
    )

    suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface
    )
}
