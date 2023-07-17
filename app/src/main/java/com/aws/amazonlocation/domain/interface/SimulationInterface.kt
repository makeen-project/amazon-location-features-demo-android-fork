package com.aws.amazonlocation.domain.`interface`

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SimulationInterface {
    fun getGeofenceList(collectionName: String) {}
    fun evaluateGeofence(
        position1: List<Double>? = null,
        position2: List<Double>? = null,
        position3: List<Double>? = null,
        position4: List<Double>? = null,
        position5: List<Double>? = null,
        position6: List<Double>? = null,
        position7: List<Double>? = null,
        position8: List<Double>? = null,
        position9: List<Double>? = null,
        position10: List<Double>? = null
    ) {
    }
}
