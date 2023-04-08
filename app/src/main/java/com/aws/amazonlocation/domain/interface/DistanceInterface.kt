package com.aws.amazonlocation.domain.`interface`

import com.amazonaws.services.geo.model.CalculateRouteResult
import com.aws.amazonlocation.data.common.DataSourceException

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface DistanceInterface {

    fun distanceSuccess(success: CalculateRouteResult) {}

    fun distanceFailed(exception: DataSourceException) {}

    fun internetConnectionError(exception: String) {}
}
