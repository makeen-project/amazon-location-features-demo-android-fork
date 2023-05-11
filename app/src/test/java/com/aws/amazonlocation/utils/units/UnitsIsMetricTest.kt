package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.KILOMETERS
import com.aws.amazonlocation.utils.MILES
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config()
class UnitsIsMetricTest : BaseTest() {

    @Test
    fun getDistanceUnitSuccess() {
        Locale.setDefault(Locale.US)
        var result = Units.isMetric("")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        Locale.setDefault(Locale("en", "IN"))
        result = Units.isMetric("")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = Units.isMetric("Imperial")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = Units.isMetric("Metric")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
    }
}
