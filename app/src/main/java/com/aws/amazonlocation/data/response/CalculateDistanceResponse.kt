package com.aws.amazonlocation.data.response

import com.amazonaws.services.geo.model.CalculateRouteResult
import com.mapbox.mapboxsdk.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class CalculateDistanceResponse(
    var name: String? = null,
    var calculateRouteResult: CalculateRouteResult? = null,
    var sourceLatLng: LatLng? = null,
    var destinationLatLng: LatLng? = null
)
