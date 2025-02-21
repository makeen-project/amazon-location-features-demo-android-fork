package com.aws.amazonlocation.domain.`interface`

import aws.sdk.kotlin.services.geoplaces.model.ReverseGeocodeResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SearchDataInterface {
    fun getAddressData(reverseGeocodeResponse: ReverseGeocodeResponse)

    fun internetConnectionError(error: String) {}
    fun error(error: String) {}
}
