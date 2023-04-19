package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.validateUserPoolClientId
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateUserPoolClientIdTest : BaseTest() {

    @Test
    fun validateUserPoolClientIdSuccess() {
        var result = validateUserPoolClientId(BuildConfig.USER_POOL_CLIENT_ID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = validateUserPoolClientId(null)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateUserPoolClientId(USER_POOL_CLIENT_ID_INVALID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
