package com.aws.amazonlocation.domain.usecase

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.repository.GeofenceRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Date
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceUseCase @Inject constructor(private val mGeofenceRepository: GeofenceRepository) {

    suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceInterface: GeofenceAPIInterface
    ) =
        mGeofenceRepository.getGeofenceList(collectionName, mGeofenceInterface)

    suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceInterface: GeofenceAPIInterface
    ) =
        mGeofenceRepository.addGeofence(geofenceId, collectionName, radius, latLng, mGeofenceInterface)

    suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceInterface: GeofenceAPIInterface
    ) =
        mGeofenceRepository.deleteGeofence(position, data, mGeofenceInterface)

    fun searchPlaceSuggestionList(
        lat: Double?,
        lng: Double?,
        searchText: String,
        isGrabMapSelected: Boolean,
        searchPlace: SearchPlaceInterface
    ) =
        mGeofenceRepository.searchPlaceSuggestions(lat, lng, searchText, searchPlace, isGrabMapSelected)

    fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    ) = mGeofenceRepository.searchPlaceIndexForText(lat, lng, searchText, searchPlace)

    suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        date: Date,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    ) = mGeofenceRepository.batchUpdateDevicePosition(trackerName, position, deviceId, date, batchLocationUpdateInterface)

    suspend fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        historyInterface: LocationHistoryInterface
    ) = mGeofenceRepository.getLocationHistory(trackerName, deviceId, dateStart, dateEnd, historyInterface)

    suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface
    ) = mGeofenceRepository.deleteLocationHistory(trackerName, deviceId, historyInterface)
}
