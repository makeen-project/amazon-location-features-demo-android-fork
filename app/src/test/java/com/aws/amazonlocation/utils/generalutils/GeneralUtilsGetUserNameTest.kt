package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.getUserName
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsGetUserNameTest : BaseTest() {

    @Test
    fun getUserNameSuccess() {
        val result = getUserName(Responses.RESPONSE_SIGN_IN)
        val expected = Responses.RESPONSE_SIGN_IN.name?.first()?.uppercase()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == expected)
    }
}
