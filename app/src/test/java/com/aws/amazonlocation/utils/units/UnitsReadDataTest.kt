package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UnitsReadDataTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    @Test
    fun readRouteData() {
        val result = Units.readRouteData(context)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result?.busRoutesData?.size == 5)
    }
}
