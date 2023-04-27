package com.aws.amazonlocation.data.repository

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.LocationDeleteHistoryInterface
import com.aws.amazonlocation.domain.`interface`.LocationHistoryInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.domain.repository.GeofenceRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.Date

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceImp(private val mRemoteDataSource: RemoteDataSourceImpl) :
    GeofenceRepository {

    override suspend fun getGeofenceList(
        collectionName: String,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        mRemoteDataSource.getGeofenceList(collectionName, mGeofenceAPIInterface)
    }

    override suspend fun addGeofence(
        geofenceId: String,
        collectionName: String,
        radius: Double?,
        latLng: LatLng?,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        mRemoteDataSource.addGeofence(geofenceId, collectionName, radius, latLng, mGeofenceAPIInterface)
    }

    override suspend fun deleteGeofence(
        position: Int,
        data: ListGeofenceResponseEntry,
        mGeofenceAPIInterface: GeofenceAPIInterface
    ) {
        mRemoteDataSource.deleteGeofence(
            position = position,
            data = data,
            mGeofenceAPIInterface = mGeofenceAPIInterface
        )
    }

    override fun searchPlaceSuggestions(
        lat: Double?,
        lng: Double?,
        searchText: String,
        searchPlace: SearchPlaceInterface
    ) {
        mRemoteDataSource.searchPlaceSuggestions(lat, lng, searchText, searchPlace)
    }

    override fun searchPlaceIndexForText(
        lat: Double?,
        lng: Double?,
        searchText: String?,
        searchPlace: SearchPlaceInterface
    ) {
        mRemoteDataSource.searchPlaceIndexForText(lat, lng, searchText, searchPlace)
    }

    override suspend fun batchUpdateDevicePosition(
        trackerName: String,
        position: List<Double>,
        deviceId: String,
        date: Date,
        batchLocationUpdateInterface: BatchLocationUpdateInterface
    ) {
        mRemoteDataSource.batchUpdateDevicePosition(trackerName, position, deviceId, date, batchLocationUpdateInterface)
    }

    override suspend fun getLocationHistory(
        trackerName: String,
        deviceId: String,
        dateStart: Date,
        dateEnd: Date,
        historyInterface: LocationHistoryInterface
    ) {
        mRemoteDataSource.getLocationHistory(trackerName, deviceId, dateStart, dateEnd, historyInterface)
    }

    override suspend fun deleteLocationHistory(
        trackerName: String,
        deviceId: String,
        historyInterface: LocationDeleteHistoryInterface
    ) {
        mRemoteDataSource.deleteLocationHistory(trackerName, deviceId, historyInterface)
    }
}
