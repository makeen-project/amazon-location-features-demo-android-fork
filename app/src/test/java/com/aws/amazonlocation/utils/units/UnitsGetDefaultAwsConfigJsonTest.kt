package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsGetDefaultAwsConfigJsonTest : BaseTest() {

    @Test
    fun getDefaultAwsConfigJsonSuccess() {
        var result = Units.getDefaultAwsConfigJson(UNIT_DEF_AWS_CONF_INPUT_POOL_ID, UNIT_DEF_AWS_CONF_INPUT_REGION)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_DEF_AWS_CONF)
    }
}
