package com.aws.amazonlocation.domain.`interface`

import aws.sdk.kotlin.services.georoutes.model.CalculateRoutesResponse
import com.aws.amazonlocation.data.common.DataSourceException

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface DistanceInterface {

    fun distanceSuccess(success: CalculateRoutesResponse) {}

    fun distanceFailed(exception: DataSourceException) {}

    fun internetConnectionError(exception: String) {}
}
