package com.aws.amazonlocation.utils.units

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.regionDisplayName
import com.aws.amazonlocation.utils.regionList
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitsDefaultIdentityPoolIdTest : BaseTest() {

    @Test
    fun toDefaultIdentityPoolIdSuccess() {
        var result = Units.getDefaultIdentityPoolId(regionDisplayName[0], "")
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID)
        result = Units.getDefaultIdentityPoolId(regionDisplayName[0], regionList[2])
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == BuildConfig.DEFAULT_IDENTITY_POOL_ID_AP)
    }
}
