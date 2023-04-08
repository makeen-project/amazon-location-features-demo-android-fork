package com.aws.amazonlocation.data.response

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class DeleteGeofence(
    var position: Int? = null,
    var data: ListGeofenceResponseEntry? = null,
    var errorMessage: String? = null
)
