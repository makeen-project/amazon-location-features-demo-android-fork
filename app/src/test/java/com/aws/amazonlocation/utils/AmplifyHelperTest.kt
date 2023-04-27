package com.aws.amazonlocation.utils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.mock.DELAY_5000
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.ui.main.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AmplifyHelperTest : BaseTest() {

    private val context = RuntimeEnvironment.getApplication().applicationContext

    private val preferenceManager = PreferenceManager(context)

    private var mainActivity: MainActivity = Mockito.mock(MainActivity::class.java)

    @Spy
    private var amplifyHelper: AmplifyHelper =
        AmplifyHelper(context, preferenceManager)

    override fun setUp() {
        super.setUp()
        Mockito.doNothing().`when`(mainActivity).handleException(anyOrNull(), anyOrNull())
        val mIdentityPoolId = BuildConfig.IDENTITY_POOL_ID
        val mUserDomain = BuildConfig.USER_DOMAIN
        val mUserPoolClientId = BuildConfig.USER_POOL_CLIENT_ID
        val mUserPoolId = BuildConfig.USER_POOL_ID
        val mWebSocketUrl = BuildConfig.WEB_SOCKET_URL
        mIdentityPoolId.let { identityPId ->
            preferenceManager.setValue(
                KEY_POOL_ID,
                identityPId
            )

            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    preferenceManager.setValue(
                        KEY_USER_REGION,
                        region
                    )
                }
            }
        }
        mUserDomain.let { uDomain ->
            uDomain.split(HTTPS)[1].let { domain ->
                preferenceManager.setValue(
                    KEY_USER_DOMAIN,
                    domain
                )
            }
        }

        mUserPoolClientId.let { uPoolClientId ->
            preferenceManager.setValue(
                KEY_USER_POOL_CLIENT_ID,
                uPoolClientId
            )
        }
        mUserPoolId.let { uPoolId ->
            preferenceManager.setValue(
                KEY_USER_POOL_ID,
                uPoolId
            )
        }
        mWebSocketUrl.let { webSocketUrl ->
            preferenceManager.setValue(
                WEB_SOCKET_URL,
                webSocketUrl
            )
        }
        setConnectivity(true)
    }

    @Test
    fun initAmplifyTest() {
        amplifyHelper.initAmplify()
        Thread.sleep(DELAY_5000)
    }
}
