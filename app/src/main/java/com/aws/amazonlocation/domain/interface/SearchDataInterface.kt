package com.aws.amazonlocation.domain.`interface`

import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SearchDataInterface {
    fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult)

    fun internetConnectionError(error: String) {}
    fun error(error: String) {}
}
