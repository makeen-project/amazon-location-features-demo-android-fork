package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.validateIdentityPoolId
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateIdentityPoolIdTest : BaseTest() {

    @Test
    fun validateUserPoolIdSuccess() {
        val result = validateIdentityPoolId(BuildConfig.IDENTITY_POOL_ID, BuildConfig.DEFAULT_REGION)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
