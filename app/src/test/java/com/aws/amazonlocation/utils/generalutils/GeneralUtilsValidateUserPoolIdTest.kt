package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.validateUserPoolId // ktlint-disable no-wildcard-imports
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateUserPoolIdTest : BaseTest() {

    @Test
    fun validateUserPoolIdSuccess() {
        var result = validateUserPoolId(BuildConfig.USER_POOL_ID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = validateUserPoolId(null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateUserPoolId(USER_POOL_ID_INVALID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
