package com.aws.amazonlocation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.aws.amazonlocation.mock.END_DATE_NULL
import com.aws.amazonlocation.mock.START_DATE_NULL
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*

fun getDateRange(startDate: String, endDate: String): Pair<Date, Date> {
    val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
    val dateStart = sdf.parse(startDate) ?: throw Exception(START_DATE_NULL)
    val dateEnd = sdf.parse(endDate) ?: throw Exception(END_DATE_NULL)

    return Pair(dateStart, dateEnd)
}

@Throws(Exception::class)
fun setConnectivity(enabled: Boolean) {
    val connectivityManager = RuntimeEnvironment.getApplication().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = ShadowNetworkCapabilities.newInstance()
    if (enabled) {
        shadowOf(networkCapabilities).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    shadowOf(connectivityManager).setNetworkCapabilities(connectivityManager.activeNetwork, networkCapabilities)
}
