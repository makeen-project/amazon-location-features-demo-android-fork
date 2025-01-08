package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.utils.formatToDisplayDate
import java.util.Calendar
import java.util.TimeZone
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsFormatToDisplayDateTest : BaseTest() {

    @Test
    fun formatToDisplayDateTest() {
        val testDate = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 7, 14, 30, 0)
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }.time

        val expectedOutput = "07/01 14:30"
        val formattedDate = formatToDisplayDate(testDate)
        assertEquals(expectedOutput, formattedDate)
    }
}
