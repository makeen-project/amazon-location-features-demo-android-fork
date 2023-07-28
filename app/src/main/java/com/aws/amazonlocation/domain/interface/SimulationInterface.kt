package com.aws.amazonlocation.domain.`interface`

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SimulationInterface {
    fun getGeofenceList() {}
    fun evaluateGeofence(
        collectionName: String,
        position1: List<Double>? = null
    ) {
    }
}
