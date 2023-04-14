package com.aws.amazonlocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
object Units {

    fun getMetrics(distance: Double): String {
        return if (distance <= 1000) {
            "${DecimalFormat("##.##").format((distance.toInt()))} m"
        } else {
            "${DecimalFormat("##.#").format((distance / 1000))} km"
        }
    }

    fun kiloMeterToMeter(kiloMeter: Double): Double {
        return kiloMeter * 1000
    }

    fun getTime(second: Double): String {
        val mSeconds = second.toInt().toLong()
        TimeUnit.SECONDS.toDays(mSeconds).toInt()
        val mHours: Long =
            TimeUnit.SECONDS.toHours(mSeconds)
        val mMinute: Long =
            TimeUnit.SECONDS.toMinutes(mSeconds) - TimeUnit.SECONDS.toHours(mSeconds) * 60
        val mSecondNew: Long =
            TimeUnit.SECONDS.toSeconds(mSeconds) - TimeUnit.SECONDS.toMinutes(mSeconds) * 60

        var mTime = if (mMinute == 0L && mHours == 0L) {
            buildString {
                append(mSecondNew)
                append(" sec")
            }
        } else {
            buildString {
                append(mMinute)
                append(" min")
            }
        }

        if (mHours != 0L) {
            mTime = buildString {
                append(mHours)
                append(" hr ")
                append(mTime)
            }
        }
        return mTime
    }

    fun getDefaultAwsConfigJson(
        poolID: String?,
        region: String?
    ): String {
        return "{\n" +
            "    \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
            "    \"Version\": \"0.1.0\",\n" +
            "    \"IdentityManager\": {\n" +
            "        \"Default\": {}\n" +
            "    },\n" +
            "    \"CredentialsProvider\": {\n" +
            "        \"CognitoIdentity\": {\n" +
            "            \"Default\": {\n" +
            "                \"PoolId\": \"$poolID\",\n" +
            "                \"Region\": \"$region\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
    }

    fun getAwsConfigJson(
        poolID: String?,
        userPoolId: String? = null,
        appClientId: String?,
        domain: String?,
        region: String?,
        schema: String
    ): String {
        return "{\n" +
            "    \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
            "    \"Version\": \"0.1.0\",\n" +
            "    \"IdentityManager\": {\n" +
            "        \"Default\": {}\n" +
            "    },\n" +
            "    \"CredentialsProvider\": {\n" +
            "        \"CognitoIdentity\": {\n" +
            "            \"Default\": {\n" +
            "                \"PoolId\": \"$poolID\",\n" +
            "                \"Region\": \"$region\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"CognitoUserPool\": {\n" +
            "        \"Default\": {\n" +
            "            \"PoolId\": \"$userPoolId\",\n" +
            "            \"AppClientId\": \"$appClientId\",\n" +
            "            \"Region\": \"$region\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"Auth\": {\n" +
            "        \"Default\": {\n" +
            "            \"OAuth\": {\n" +
            "              \"WebDomain\": \"$domain\",\n" +
            "              \"AppClientId\": \"$appClientId\",\n" +
            "              \"SignInRedirectURI\": \"$schema://signin/\",\n" +
            "              \"SignOutRedirectURI\": \"$schema://signout/\",\n" +
            "              \"Scopes\": [\n" +
            "                \"email\",\n" +
            "                \"openid\",\n" +
            "                \"profile\"\n" +
            "              ]\n" +
            "            }\n" +
            "          }\n" +
            "    }\n" +
            "}"
    }

    fun getAmplifyConfigJson(
        poolID: String?,
        region: String?
    ): String {
        return "{\n" +
            "    \"UserAgent\": \"aws-amplify-cli/2.0\",\n" +
            "    \"Version\": \"1.0\",\n" +
            "    \"auth\": {\n" +
            "        \"plugins\": {\n" +
            "            \"awsCognitoAuthPlugin\": {\n" +
            "                \"UserAgent\": \"aws-amplify-cli/0.1.0\",\n" +
            "                \"Version\": \"0.1.0\",\n" +
            "                \"IdentityManager\": {\n" +
            "                    \"Default\": {}\n" +
            "                },\n" +
            "                \"CredentialsProvider\": {\n" +
            "                    \"CognitoIdentity\": {\n" +
            "                        \"Default\": {\n" +
            "                            \"PoolId\": \"$poolID\",\n" +
            "                            \"Region\": \"$region\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"geo\": {\n" +
            "        \"plugins\": {\n" +
            "            \"awsLocationGeoPlugin\": {\n" +
            "                \"region\": \"$region\",\n" +
            "                \"maps\": {\n" +
            "                    \"items\": {\n" +
            "                        \"${MapNames.ESRI_LIGHT}\": {\n" +
            "                            \"style\": \"${MapStyles.VECTOR_ESRI_TOPOGRAPHIC}\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    \"default\": \"${MapNames.ESRI_LIGHT}\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun isGPSEnabled(mContext: Context): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
