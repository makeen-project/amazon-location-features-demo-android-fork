package com.aws.amazonlocation.utils

import android.content.Context
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.regions.Regions
import com.amazonaws.services.pinpoint.model.ChannelType
import com.aws.amazonlocation.data.enum.AuthEnum
import javax.inject.Inject

class AnalyticsHelper(
    private val context: Context,
    private val mAWSLocationHelper: AWSLocationHelper,
    private val mPreferenceManager: PreferenceManager
) {

    private var pinpointManager: PinpointManager? = null

    fun initAnalytics() {
        val provider = mAWSLocationHelper.getAnalyticsCredentialProvider()
        val pinpointConfig = PinpointConfiguration(
            context,
            ANALYTICS_APP_ID,
            Regions.US_EAST_1,
            ChannelType.GCM,
            provider
        )
        pinpointManager = PinpointManager(pinpointConfig)
    }

    fun startSession() {
        pinpointManager?.sessionClient?.startSession()
    }

    fun stopSession() {
        pinpointManager?.sessionClient?.stopSession()
        pinpointManager?.analyticsClient?.submitEvents()
    }

    fun recordEvent(eventName: String, properties: List<Pair<String, String>>) {
        val mAuthStatus = mPreferenceManager.getValue(
            KEY_CLOUD_FORMATION_STATUS,
            AuthEnum.DEFAULT.name
        )
        var connectedStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_NOT_CONNECTED
        var authStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_UNAUTHENTICATED
        if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
            connectedStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_CONNECTED
            authStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_AUTHENTICATED
        } else if (mAuthStatus == AuthEnum.AWS_CONNECTED.name) {
            connectedStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_CONNECTED
            authStatus = AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS_AUTHENTICATED
        }
        val event = pinpointManager?.analyticsClient?.createEvent(eventName)
        event?.addAttribute(AnalyticsAttribute.USER_AWS_ACCOUNT_CONNECTION_STATUS, connectedStatus)
        event?.addAttribute(AnalyticsAttribute.USER_AUTHENTICATION_STATUS, authStatus)
        properties.forEach { (propertyName, propertyValue) ->
            event?.addAttribute(propertyName, propertyValue)
        }
        pinpointManager?.analyticsClient?.recordEvent(event)
        pinpointManager?.analyticsClient?.submitEvents()
    }
}
