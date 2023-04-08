package com.aws.amazonlocation.data.response

import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchResponse(
    var searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)

