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
        var regionData = ""
        var invalidRegion = REGION_INVALID
        BuildConfig.IDENTITY_POOL_ID.let { identityPId ->
            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    regionData = region
                }
            }
        }
        if (regionData == REGION_INVALID) {
            invalidRegion = REGION_INVALID_1
        }

        var result =
            validateIdentityPoolId(BuildConfig.IDENTITY_POOL_ID, regionData)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        result = validateIdentityPoolId(IDENTITY_POOL_ID_INVALID, regionData)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateIdentityPoolId(BuildConfig.IDENTITY_POOL_ID, invalidRegion)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
        result = validateIdentityPoolId(IDENTITY_POOL_ID_INVALID, invalidRegion)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
