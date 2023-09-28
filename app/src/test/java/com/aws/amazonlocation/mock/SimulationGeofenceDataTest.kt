package com.aws.amazonlocation.mock

import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.response.SimulationGeofenceData
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SimulationGeofenceDataTest {

    @Test
    fun testCollectionName() {
        val geofenceData = SimulationGeofenceData(
            collectionName = "GeofenceCollection",
            devicePositionData = ArrayList()
        )
        geofenceData.collectionName = geofenceData.collectionName
        geofenceData.devicePositionData = geofenceData.devicePositionData
        assert(geofenceData.collectionName == "GeofenceCollection")
    }

    @Test
    fun testDevicePositionData() {
        val devicePositionData = ArrayList<ListGeofenceResponseEntry>()
        val geofenceData = SimulationGeofenceData(
            collectionName = "GeofenceCollection",
            devicePositionData = devicePositionData
        )
        assert(geofenceData.devicePositionData == devicePositionData)
    }
}
