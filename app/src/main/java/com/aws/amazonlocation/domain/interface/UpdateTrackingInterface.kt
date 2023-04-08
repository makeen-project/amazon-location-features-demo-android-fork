package com.aws.amazonlocation.domain.`interface`

import android.location.Location

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface UpdateTrackingInterface {

    fun updateRoute(latLng: Location, bearing: Float?)
}
