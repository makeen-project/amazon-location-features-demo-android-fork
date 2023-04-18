package com.aws.amazonlocation.utils

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken
import com.amazonaws.regions.Regions
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.utils.Units.getAmplifyConfigJson
import com.aws.amazonlocation.utils.Units.getAwsConfigJson
import com.aws.amazonlocation.utils.Units.getDefaultAwsConfigJson
import org.json.JSONObject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AmplifyHelper(
    private var mContext: Context,
    private var mPreferenceManager: PreferenceManager
) {

    fun initAmplify() {
        val mPoolId = mPreferenceManager.getValue(KEY_POOL_ID, "")
        val mUserPoolId = mPreferenceManager.getValue(KEY_USER_POOL_ID, "")

        val mAppClientId = mPreferenceManager.getValue(KEY_USER_POOL_CLIENT_ID, "")
        val mDomain = mPreferenceManager.getValue(KEY_USER_DOMAIN, "")
        val mRegion = mPreferenceManager.getValue(KEY_USER_REGION, "")

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSLocationGeoPlugin())
        } catch (_: Exception) {
        }
        val mAmplifyConfiguration: AmplifyConfiguration
        val mAwsConfiguration: AWSConfiguration

        if (mPoolId.isNullOrEmpty()) { // Guest mode
            mAmplifyConfiguration = AmplifyConfiguration.fromJson(
                JSONObject(
                    getAmplifyConfigJson(
                        BuildConfig.DEFAULT_IDENTITY_POOL_ID,
                        BuildConfig.DEFAULT_REGION
                    )
                )
            )
            mAwsConfiguration = AWSConfiguration(
                JSONObject(
                    getDefaultAwsConfigJson(
                        BuildConfig.DEFAULT_IDENTITY_POOL_ID,
                        BuildConfig.DEFAULT_REGION
                    )
                )
            )
        } else { // connected with user's AWS
            mAmplifyConfiguration = AmplifyConfiguration.fromJson(
                JSONObject(
                    getAmplifyConfigJson(
                        mPoolId,
                        mRegion
                    )
                )
            )
            val schema = mContext.getString(R.string.AMAZON_LOCATION_SCHEMA)
            mAwsConfiguration = AWSConfiguration(
                JSONObject(
                    getAwsConfigJson(
                        mPoolId,
                        mUserPoolId,
                        mAppClientId,
                        mDomain,
                        mRegion,
                        schema
                    )
                )
            )
        }

        checkCurrentUserSession(mUserPoolId, mAppClientId, mRegion)

        AWSMobileClient.getInstance()
            .initialize(
                mContext,
                mAwsConfiguration,
                object : Callback<UserStateDetails> {
                    override fun onResult(result: UserStateDetails?) {
                        if (result?.userState?.name == UserState.SIGNED_IN.name) {
                            val accessToken = mPreferenceManager.getValue(KEY_ACCESS_TOKEN, "")
                            val refreshToken = mPreferenceManager.getValue(KEY_REFRESH_TOKEN, "")
                            if (result.details.containsKey("token")) {
                                result.details["token"]?.let {
                                    mPreferenceManager.setValue(
                                        KEY_ID_TOKEN,
                                        it
                                    )
                                }
                            }

                            if (result.details.containsKey("provider")) {
                                result.details["provider"]?.let {
                                    mPreferenceManager.setValue(
                                        KEY_PROVIDER,
                                        it
                                    )
                                }
                            }
                            val idToken = mPreferenceManager.getValue(KEY_ID_TOKEN, "")

                            val cipSession = CognitoUserSession(
                                CognitoIdToken(idToken),
                                CognitoAccessToken(accessToken),
                                CognitoRefreshToken(refreshToken)
                            )

                            if (!cipSession.isValid) {
                                mPreferenceManager.removeValue(KEY_USER_DETAILS)
                                mPreferenceManager.setValue(
                                    KEY_CLOUD_FORMATION_STATUS,
                                    AuthEnum.AWS_CONNECTED.name
                                )
                            }
                        } else {
                            if (mPoolId.isNullOrEmpty()) {
                                mPreferenceManager.setValue(
                                    KEY_CLOUD_FORMATION_STATUS,
                                    AuthEnum.DEFAULT.name
                                )
                            } else {
                                mPreferenceManager.removeValue(KEY_USER_DETAILS)
                                mPreferenceManager.setValue(
                                    KEY_CLOUD_FORMATION_STATUS,
                                    AuthEnum.AWS_CONNECTED.name
                                )
                            }
                        }
                    }

                    override fun onError(e: Exception?) {
                        e?.printStackTrace()
                    }
                }
            )

        try {
            Amplify.configure(mAmplifyConfiguration, mContext)
        } catch (e: AmplifyException) {
            e.printStackTrace()
        }
    }

    private fun checkCurrentUserSession(
        mUserPoolId: String?,
        mAppClientId: String?,
        mRegion: String?
    ) {
        val mAuthStatus = mPreferenceManager.getValue(
            KEY_CLOUD_FORMATION_STATUS,
            AuthEnum.DEFAULT.name
        )

        if (mAuthStatus == AuthEnum.SIGNED_IN.name) {
            val userPool = CognitoUserPool(
                mContext,
                mUserPoolId,
                mAppClientId,
                null,
                Regions.fromName(mRegion)
            )

            userPool.currentUser.getSession(object : AuthenticationHandler {
                override fun onSuccess(
                    userSession: CognitoUserSession?,
                    newDevice: CognitoDevice?
                ) {
                    mPreferenceManager.setValue(
                        KEY_ACCESS_TOKEN,
                        userSession?.accessToken?.jwtToken!!
                    )
                    mPreferenceManager.setValue(
                        KEY_REFRESH_TOKEN,
                        userSession.refreshToken?.token!!
                    )

                    mPreferenceManager.setValue(
                        KEY_ID_TOKEN,
                        userSession.idToken.jwtToken
                    )
                }

                override fun getAuthenticationDetails(
                    authenticationContinuation: AuthenticationContinuation?,
                    userId: String?
                ) {
                }

                override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                }

                override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                }

                override fun onFailure(exception: java.lang.Exception) {
                }
            })
        }
    }
}
