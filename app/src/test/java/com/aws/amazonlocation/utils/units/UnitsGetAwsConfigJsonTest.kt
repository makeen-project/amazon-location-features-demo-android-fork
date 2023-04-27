package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Units
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsGetAwsConfigJsonTest : BaseTest() {

    @Test
    fun getAwsConfigJsonSuccess() {
        var result = Units.getAwsConfigJson(
            poolID = UNIT_AWS_CONF_JSON_INPUT_POOL_ID,
            userPoolId = UNIT_AWS_CONF_JSON_INPUT_USER_POOL_ID,
            appClientId = UNIT_AWS_CONF_JSON_INPUT_APP_CLIENT_ID,
            domain = UNIT_AWS_CONF_JSON_INPUT_DOMAIN,
            region = UNIT_AWS_CONF_JSON_INPUT_REGION,
            schema = UNIT_AWS_CONF_JSON_INPUT_SCHEMA,
        )
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_AWS_CONF_JSON)

        result = Units.getAwsConfigJson(
            poolID = UNIT_AWS_CONF_JSON_INPUT_POOL_ID,
            appClientId = UNIT_AWS_CONF_JSON_INPUT_APP_CLIENT_ID,
            domain = UNIT_AWS_CONF_JSON_INPUT_DOMAIN,
            region = UNIT_AWS_CONF_JSON_INPUT_REGION,
            schema = UNIT_AWS_CONF_JSON_INPUT_SCHEMA,
        )
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == UNIT_AWS_CONF_JSON_NULL_USER_POOL_ID)
    }
}
