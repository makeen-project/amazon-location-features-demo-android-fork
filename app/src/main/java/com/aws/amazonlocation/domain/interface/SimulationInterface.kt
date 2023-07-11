package com.aws.amazonlocation.domain.`interface`

import com.mapbox.mapboxsdk.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SimulationInterface {
    fun getGeofenceList(collectionName: String) {}
    fun evaluateGeofence(latLng: LatLng) {}
}
