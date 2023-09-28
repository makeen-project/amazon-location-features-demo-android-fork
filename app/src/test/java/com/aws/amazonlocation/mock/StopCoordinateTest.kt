package com.aws.amazonlocation.mock

import com.aws.amazonlocation.data.response.Geometry
import com.aws.amazonlocation.data.response.Properties
import com.aws.amazonlocation.data.response.StopCoordinate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StopCoordinateTest {

    private lateinit var stopCoordinate: StopCoordinate

    @Before
    fun setUp() {
        stopCoordinate = StopCoordinate(
            geometry = Geometry(listOf(1.0, 2.0), "Point"),
            id = 1,
            properties = Properties(2, "stop123", "Bus Stop"),
            type = "Feature"
        )
        stopCoordinate.geometry = Geometry(listOf(1.0, 2.0), "Point")
        stopCoordinate.id = 1
        stopCoordinate.properties = Properties(2, "stop123", "Bus Stop")
        stopCoordinate.type = "Feature"
    }

    @Test
    fun testGeometry() {
        val expectedGeometry = Geometry(listOf(1.0, 2.0), "Point")
        assertEquals(expectedGeometry, stopCoordinate.geometry)
    }

    @Test
    fun testId() {
        val expectedId = 1
        assertEquals(expectedId, stopCoordinate.id)
    }

    @Test
    fun testProperties() {
        val expectedProperties = Properties(2, "stop123", "Bus Stop")
        assertEquals(expectedProperties, stopCoordinate.properties)
    }

    @Test
    fun testType() {
        val expectedType = "Feature"
        assertEquals(expectedType, stopCoordinate.type)
    }
}