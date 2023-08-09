package com.aws.amazonlocation.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class LatencyChecker(private val client: OkHttpClient = defaultClient()) {

    companion object {
        fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    fun checkLatencyForUrls(urls: List<String>): Pair<String?, Long> {
        var fastestUrl: String? = null
        var minLatency: Long = Long.MAX_VALUE

        for (url in urls) {
            val request = Request.Builder().url(url).build()
            val start = System.currentTimeMillis()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val latency = System.currentTimeMillis() - start
                    if (latency < minLatency) {
                        minLatency = latency
                        fastestUrl = url
                    }
                }
            }
        }

        return fastestUrl to minLatency
    }
}
