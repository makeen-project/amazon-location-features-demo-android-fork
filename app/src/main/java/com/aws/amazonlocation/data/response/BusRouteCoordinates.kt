package com.aws.amazonlocation.data.response

data class BusRouteCoordinates(
    var id: String? = null,
    var geofenceCollection: String? = null,
    var coordinates: List<Double>? = null,
    var isUpdateNeeded: Boolean = true
)
