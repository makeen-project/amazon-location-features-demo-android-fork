package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.RESULT_TIME
import com.aws.amazonlocation.mock.TEST_FAILED_DUE_TO_INCORRECT_DATA
import com.aws.amazonlocation.mock.UTC_TIME
import com.aws.amazonlocation.utils.convertToLocalTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsConvertToLocalTimeTest : BaseTest() {

    @Test
    fun convertToLocalTimeTest() {
        val result = convertToLocalTime(UTC_TIME)
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result == RESULT_TIME)
    }
}
