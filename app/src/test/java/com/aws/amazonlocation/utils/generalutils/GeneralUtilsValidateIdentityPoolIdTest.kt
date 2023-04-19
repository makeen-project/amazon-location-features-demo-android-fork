package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.validateIdentityPoolId
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateIdentityPoolIdTest : BaseTest() {

    @Test
    fun validateIdentityPoolIdSuccess() {
        var result = validateIdentityPoolId(BuildConfig.IDENTITY_POOL_ID, BuildConfig.DEFAULT_REGION)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = validateIdentityPoolId(IDENTITY_POOL_ID_INVALID, BuildConfig.DEFAULT_REGION)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateIdentityPoolId(BuildConfig.IDENTITY_POOL_ID, REGION_INVALID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateIdentityPoolId(IDENTITY_POOL_ID_INVALID, REGION_INVALID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
