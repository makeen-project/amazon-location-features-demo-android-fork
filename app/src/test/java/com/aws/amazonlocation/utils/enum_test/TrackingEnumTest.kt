package com.aws.amazonlocation.utils.enum_test

import com.aws.amazonlocation.data.enum.TrackingEnum
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrackingEnumTest {

    @Test
    fun `Enum values should be correctly defined`() {
        // Assert that the enum values are defined as expected
        assertEquals("ENABLE_TRACKING", TrackingEnum.ENABLE_TRACKING.name)
        assertEquals("TRACKING_HISTORY", TrackingEnum.TRACKING_HISTORY.name)
    }

    @Test
    fun `Enum values should have the correct ordinal`() {
        // Assert that the enum values have the expected ordinal values
        assertEquals(0, TrackingEnum.ENABLE_TRACKING.ordinal)
        assertEquals(1, TrackingEnum.TRACKING_HISTORY.ordinal)
    }

    @Test
    fun `Enum values should be retrievable by their names`() {
        // Assert that you can retrieve enum values by their names
        assertEquals(TrackingEnum.ENABLE_TRACKING, TrackingEnum.valueOf("ENABLE_TRACKING"))
        assertEquals(TrackingEnum.TRACKING_HISTORY, TrackingEnum.valueOf("TRACKING_HISTORY"))
    }
}
