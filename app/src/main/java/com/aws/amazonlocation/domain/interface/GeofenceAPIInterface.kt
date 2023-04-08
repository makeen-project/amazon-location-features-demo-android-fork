package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.DeleteGeofence
import com.aws.amazonlocation.data.response.GeofenceData

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface GeofenceAPIInterface {
    fun addGeofence(response: AddGeofenceResponse) {}
    fun getGeofenceList(geofenceData: GeofenceData) {}
    fun deleteGeofence(deleteGeofence: DeleteGeofence) {}
}
