package com.aws.amazonlocation.utils

import android.content.Context
import com.amazonaws.auth.CognitoCredentialsProvider
import com.aws.amazonlocation.mock.TEST_FAILED_CLIENT_NOT_INITIALIZE
import com.aws.amazonlocation.mock.TEST_FAILED_COGNITO_CREDENTIALS_PROVIDER_NOT_INITIALIZE
import com.aws.amazonlocation.setConnectivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
internal class AWSLocationHelperTest {

    @Spy
    private var context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Spy
    private var preferenceManager: PreferenceManager = PreferenceManager(context)

    @Spy
    private var mapHelper: MapHelper = MapHelper(context)

    @Spy
    private var awsLocationHelper: AWSLocationHelper =
        AWSLocationHelper(mapHelper, preferenceManager)
    private var mClient: CognitoCredentialsProvider? = null

    @Before
    fun setUp() {
        mClient = awsLocationHelper.initCognitoCachingCredentialsProvider()
        setConnectivity(true)
    }

    @Test
    fun testAInitCognitoCachingCredentialsProviderTest() {
        Assert.assertTrue(TEST_FAILED_CLIENT_NOT_INITIALIZE, mClient != null)
    }

    @Test
    fun testBGetCognitoCachingCredentialsProviderTest() {
        val cognitoCredentialsProvider = awsLocationHelper.getCognitoCachingCredentialsProvider()
        Assert.assertTrue(TEST_FAILED_COGNITO_CREDENTIALS_PROVIDER_NOT_INITIALIZE, cognitoCredentialsProvider != null)
    }

    @After
    fun tearDown() {
        mClient = null
    }
}
