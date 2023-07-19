package com.aws.amazonlocation.data.response

/**
 * Created by Abhin.
 */
class RouteSimulationData : ArrayList<RouteSimulationDataItem>()

data class RouteSimulationDataItem(
    var coordinates: List<List<Double>>? = null,
    var geofenceCollection: String? = null,
    var id: String? = null,
    var name: String? = null,
    var stopCoordinates: List<StopCoordinate?>? = null
)

data class StopCoordinate(
    var geometry: Geometry? = null,
    var id: Int? = null,
    var properties: Properties? = null,
    var type: String? = null
)

data class Geometry(
    var coordinates: List<Double>? = null,
    var type: String? = null
)

data class Properties(
    var id: Int? = null,
    var stop_id: String? = null,
    var stop_name: String? = null
)
