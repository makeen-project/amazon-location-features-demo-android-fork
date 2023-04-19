package com.aws.amazonlocation.utils

import android.content.Context
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.aws.amazonlocation.mock.TEST_FAILED_CLIENT_NOT_INITIALIZE
import com.aws.amazonlocation.mock.TEST_FAILED_COGNITO_CREDENTIALS_PROVIDER_NOT_INITIALIZE
import com.aws.amazonlocation.setConnectivity
import com.mapbox.mapboxsdk.geometry.LatLng
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

/**
 * Created by Abhin.
 */
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
    private var calculateRouteResult: CalculateRouteResult? = null

    @Before
    fun setUp() {
        mClient = awsLocationHelper.initCognitoCachingCredentialsProvider()
        mockedInternetAvailability = true
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

    @Test
    fun testFSearchPlaceIndexForTextTest() {
        val searchSuggestionResponse = awsLocationHelper.searchPlaceIndexForText(
            23.013019,
            72.521230,
            "Shyamal"
        )
        Assert.assertTrue("Test failed- No data", searchSuggestionResponse.error == null)
    }

    @Test
    fun testGSearchPlaceIndexForLatLngTest() {
        val searchSuggestionResponse = awsLocationHelper.searchPlaceIndexForText(
            23.013019,
            72.521230,
            "23.013019,72.521230"
        )
        Assert.assertTrue("Test failed- No data", searchSuggestionResponse.error == null)
    }

    @Test
    fun testHSearchPlaceIndexForTextFailTest() {
        val searchSuggestionResponse = awsLocationHelper.searchPlaceIndexForText(
            230130.19,
            725212.30,
            "f9f9f9f9f99f9f9f9f9f99f"
        )
        Assert.assertTrue("Test failed- No data", searchSuggestionResponse.error != null)
    }

    @Test
    fun testMAddGeofenceTest() {
        val addGeofenceResponse = awsLocationHelper.addGeofence(
            "11",
            GeofenceCons.GEOFENCE_COLLECTION,
            80.toDouble(),
            LatLng(49.281174, -123.116823)
        )
        Assert.assertTrue(
            "Test failed- No data",
            addGeofenceResponse.isGeofenceDataAdded
        )
    }

    @Test
    fun testNAddGeofenceFailTest() {
        val addGeofenceResponse = awsLocationHelper.addGeofence(
            "",
            GeofenceCons.GEOFENCE_COLLECTION,
            0.toDouble(),
            LatLng(49.2811, -12.3116823)
        )
        Assert.assertTrue(
            "Test failed- No data",
            !addGeofenceResponse.isGeofenceDataAdded
        )
    }

    @After
    fun tearDown() {
        mClient = null
    }
}
