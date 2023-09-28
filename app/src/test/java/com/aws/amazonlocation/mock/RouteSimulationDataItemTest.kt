package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.Geometry
import com.aws.amazonlocation.data.response.Properties
import com.aws.amazonlocation.data.response.RouteSimulationDataItem
import com.aws.amazonlocation.data.response.StopCoordinate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RouteSimulationDataItemTest {

    private lateinit var routeSimulationDataItem: RouteSimulationDataItem

    @Before
    fun setUp() {
        routeSimulationDataItem = RouteSimulationDataItem(
            coordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)),
            geofenceCollection = "GeofenceCollection",
            id = "RouteId",
            name = "RouteName",
            stopCoordinates = listOf(
                StopCoordinate(Geometry(listOf(1.0, 2.0), "Point"), 1, Properties(1, "stop123", "Bus Stop")),
                StopCoordinate(Geometry(listOf(3.0, 4.0), "Point"), 2, Properties(2, "stop456", "Bus Station"))
            )
        )
        routeSimulationDataItem.coordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        routeSimulationDataItem.geofenceCollection = "GeofenceCollection"
        routeSimulationDataItem.id = "RouteId"
        routeSimulationDataItem.name = "RouteName"
        routeSimulationDataItem.stopCoordinates = listOf(
            StopCoordinate(Geometry(listOf(1.0, 2.0), "Point"), 1, Properties(1, "stop123", "Bus Stop")),
            StopCoordinate(Geometry(listOf(3.0, 4.0), "Point"), 2, Properties(2, "stop456", "Bus Station"))
        )
        routeSimulationDataItem.stopCoordinates?.let {
            it[0]?.properties?.id = 1
            it[0]?.properties?.stop_id = "stop123"
            it[0]?.properties?.stop_name = "Bus Stop"
        }
        routeSimulationDataItem.stopCoordinates?.let {
            it[0]?.geometry?.type = it[0]?.geometry?.type
            it[0]?.geometry?.coordinates = it[0]?.geometry?.coordinates
        }
    }

    @Test
    fun testCoordinates() {
        val expectedCoordinates = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        assertEquals(expectedCoordinates, routeSimulationDataItem.coordinates)
    }

    @Test
    fun testGeofenceCollection() {
        val expectedGeofenceCollection = "GeofenceCollection"
        assertEquals(expectedGeofenceCollection, routeSimulationDataItem.geofenceCollection)
    }

    @Test
    fun testId() {
        val expectedId = "RouteId"
        assertEquals(expectedId, routeSimulationDataItem.id)
    }

    @Test
    fun testName() {
        val expectedName = "RouteName"
        assertEquals(expectedName, routeSimulationDataItem.name)
    }

    @Test
    fun testStopCoordinates() {
        val expectedStopCoordinates = listOf(
            StopCoordinate(Geometry(listOf(1.0, 2.0), "Point"), 1, Properties(1, "stop123", "Bus Stop")),
            StopCoordinate(Geometry(listOf(3.0, 4.0), "Point"), 2, Properties(2, "stop456", "Bus Station"))
        )
        assertEquals(expectedStopCoordinates[0].properties?.id, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.id)
        assertEquals(expectedStopCoordinates[0].properties?.stop_name, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.stop_name)
        assertEquals(expectedStopCoordinates[0].properties?.stop_id, routeSimulationDataItem.stopCoordinates?.get(0)?.properties?.stop_id)
    }
}
