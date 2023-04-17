package com.aws.amazonlocation

import com.aws.amazonlocation.mock.END_DATE_NULL
import com.aws.amazonlocation.mock.START_DATE_NULL
import java.text.SimpleDateFormat
import java.util.*

fun getDateRange(startDate: String, endDate: String): Pair<Date, Date> {
    val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
    val dateStart = sdf.parse(startDate) ?: throw Exception(START_DATE_NULL)
    val dateEnd = sdf.parse(endDate) ?: throw Exception(END_DATE_NULL)

    return Pair(dateStart, dateEnd)
}
