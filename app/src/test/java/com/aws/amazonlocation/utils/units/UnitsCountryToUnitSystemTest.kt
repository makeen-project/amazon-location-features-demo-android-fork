package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.LOCALE_IN
import com.aws.amazonlocation.mock.LOCALE_US
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.Units
import java.util.Locale
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config()
class UnitsCountryToUnitSystemTest : BaseTest() {

    @Test
    fun toLowerUnitSuccess() {
        Locale.setDefault(LOCALE_US)
        var result = Units.isMetricUsingCountry()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        Locale.setDefault(LOCALE_IN)
        result = Units.isMetricUsingCountry()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
    }
}
