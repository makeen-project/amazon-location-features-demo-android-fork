package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsGetMetricsTest : BaseTest() {

    @Test
    fun getMetricsSuccess() {
        var result = Units.getMetrics(UNIT_METRICS_INPUT_1)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_OUTPUT_1)
        result = Units.getMetrics(UNIT_METRICS_INPUT_2)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_METRICS_OUTPUT_2)
    }
}
