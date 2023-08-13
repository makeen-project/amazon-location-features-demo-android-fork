package com.aws.amazonlocation.data.response

data class NotificationSimulationData(
    var coordinates: List<Double?>? = null,
    var eventTime: String? = null,
    var geofenceCollection: String? = null,
    var geofenceId: String? = null,
    var source: String? = null,
    var stopName: String? = null,
    var trackerEventType: String? = null
)
