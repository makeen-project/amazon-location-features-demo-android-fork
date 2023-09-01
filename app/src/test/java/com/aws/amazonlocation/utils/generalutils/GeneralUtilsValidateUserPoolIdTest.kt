package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.utils.validateUserPoolId
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsValidateUserPoolIdTest : BaseTest() {

    @Test
    fun validateUserPoolIdSuccess() {
        val result = validateUserPoolId(BuildConfig.USER_POOL_ID)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
    }
}
