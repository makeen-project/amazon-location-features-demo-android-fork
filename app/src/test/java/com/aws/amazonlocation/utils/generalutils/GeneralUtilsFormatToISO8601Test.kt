package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.utils.formatToISO8601
import java.util.Calendar
import java.util.TimeZone
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsFormatToISO8601Test : BaseTest() {

    @Test
    fun formatToISO8601Test() {
        val testDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 7, 14, 30, 0)
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }.time

        val expectedOutput = "2025-01-07T14:30:00+05:30"
        val formattedDate = formatToISO8601(testDate)
        assertEquals(expectedOutput, formattedDate)
    }
}
