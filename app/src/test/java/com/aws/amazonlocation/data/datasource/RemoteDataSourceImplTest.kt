package com.aws.amazonlocation.data.datasource

import android.content.Context
import aws.sdk.kotlin.services.location.model.TravelMode
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.mock.DEFAULT_LOCATION
import com.aws.amazonlocation.mock.DELAY_5000
import com.aws.amazonlocation.mock.FAKE_LAT_LNG
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.mock.SEARCH_TEXT_TINTO
import com.aws.amazonlocation.mock.TEST_DATA_9
import com.aws.amazonlocation.mock.TEST_DATA_LAT_2
import com.aws.amazonlocation.mock.TEST_DATA_LAT_3
import com.aws.amazonlocation.mock.TEST_DATA_LAT_4
import com.aws.amazonlocation.mock.TEST_DATA_LNG_2
import com.aws.amazonlocation.mock.TEST_DATA_LNG_3
import com.aws.amazonlocation.mock.TEST_DATA_LNG_4
import com.aws.amazonlocation.mock.TEST_FAILED_INTERNET_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_RECEIVED_ERROR
import com.aws.amazonlocation.mock.TEST_FAILED_RESPONSE_ERROR
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.GeofenceCons
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.mockedInternetAvailability
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.InjectMocks
import org.mockito.Mockito.* // ktlint-disable no-wildcard-imports
import org.mockito.Spy
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RemoteDataSourceImplTest : BaseTest() {

    @Spy
    private var context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Spy
    private var preferenceManager: PreferenceManager = PreferenceManager(context)

    @Spy
    private var mapHelper: MapHelper = MapHelper(context)

    @Spy
    private var awsLocationHelper: AWSLocationHelper =
        AWSLocationHelper(mapHelper, preferenceManager)

    @InjectMocks
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private var mainActivity: MainActivity = mock(MainActivity::class.java)

    override fun setUp() {
        super.setUp()
        doNothing().`when`(mainActivity).handleException(anyOrNull(), anyOrNull())
        awsLocationHelper.initAWSMobileClient(mainActivity)
        setConnectivity(true)
    }

    @Test
    fun searchPlaceSuggestions() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        val loc = mapHelper.getLiveLocation()

        var response: SearchSuggestionResponse? = null

        mRemoteDataSourceImpl.searchPlaceSuggestions(
            loc?.latitude,
            loc?.longitude,
            SEARCH_TEXT_TINTO,
            object : SearchPlaceInterface {
                override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                    response = suggestionResponse
                    latch.countDown()
                }

                override fun error(searchResponse: SearchSuggestionResponse) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response?.error != null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testACalculateWalkingRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            TEST_DATA_LAT_3,
            TEST_DATA_LNG_3,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Walking.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response == null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testBCalculateDriveRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            TEST_DATA_LAT_3,
            TEST_DATA_LNG_3,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Car.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response == null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testCCalculateTruckRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            TEST_DATA_LAT_3,
            TEST_DATA_LNG_3,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response == null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testDCalculateTruckRouteError() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.calculateRoute(
            23013.019,
            TEST_DATA_LNG_2,
            TEST_DATA_LAT_3,
            TEST_DATA_LNG_3,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.assertTrue(
                        TEST_FAILED_RECEIVED_ERROR,
                        exception.messageResource.toString() == TravelMode.Truck.value
                    )
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )
    }

    @Test
    fun testECalculateTruckRouteInternetError() {
        mockedInternetAvailability = false
        setConnectivity(false)
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.calculateRoute(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            TEST_DATA_LAT_3,
            TEST_DATA_LNG_3,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.assertTrue(
                        TEST_FAILED_RECEIVED_ERROR,
                        exception.messageResource.toString() == TravelMode.Truck.value
                    )
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, exception == "")
                    latch.countDown()
                }
            }
        )
    }

    @Test
    fun testFSearchNavigationPlaceIndexForPosition() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: NavigationData? = null

        val param = Responses.RESPONSE_CALCULATE_DISTANCE_CAR
        val startPosition = param.legs.first().startPosition
        val step = param.legs.first().steps.first()
        mRemoteDataSourceImpl.searchNavigationPlaceIndexForPosition(
            startPosition[0],
            startPosition[1],
            step,
            object : NavigationDataInterface {
                override fun getNavigationList(navigationData: NavigationData) {
                    response = navigationData
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response == null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testGSearchNavigationPlaceIndexForPositionInternetError() {
        mockedInternetAvailability = false
        setConnectivity(false)
        val latch = CountDownLatch(1)

        val param = Responses.RESPONSE_CALCULATE_DISTANCE_CAR
        val startPosition = param.legs.first().startPosition
        val step = param.legs.first().steps.first()
        mRemoteDataSourceImpl.searchNavigationPlaceIndexForPosition(
            startPosition[0],
            startPosition[1],
            step,
            object : NavigationDataInterface {
                override fun getNavigationList(navigationData: NavigationData) {
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, error == "")
                    latch.countDown()
                }
            }
        )
    }

    @Test
    fun testHSearchPlaceIndexForPosition() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: SearchPlaceIndexForPositionResult? = null
        mRemoteDataSourceImpl.searPlaceIndexForPosition(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    response = searchPlaceIndexForPositionResult
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }

        if (response == null) {
            Assert.fail(TEST_FAILED_RECEIVED_ERROR)
        }
    }

    @Test
    fun testISearchPlaceIndexForPositionInternetError() {
        mockedInternetAvailability = false
        setConnectivity(false)
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.searPlaceIndexForPosition(
            TEST_DATA_LAT_2,
            TEST_DATA_LNG_2,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, error == "")
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.fail(TEST_FAILED_RECEIVED_ERROR)
                    latch.countDown()
                }
            }
        )
    }

    @Test
    fun testJSearchPlaceIndexForPositionError() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.searPlaceIndexForPosition(
            TEST_DATA_LAT_4,
            TEST_DATA_LNG_4,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail(TEST_FAILED_INTERNET_ERROR)
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, error.isNotEmpty())
                    latch.countDown()
                }
            }
        )
    }

    @Test
    fun testKAddGeofence() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)
        var addGeofenceResponse: AddGeofenceResponse? = null
        mRemoteDataSourceImpl.addGeofence(
            TEST_DATA_9,
            GeofenceCons.GEOFENCE_COLLECTION,
            80.toDouble(),
            DEFAULT_LOCATION,
            object : GeofenceAPIInterface {
                override fun addGeofence(response: AddGeofenceResponse) {
                    addGeofenceResponse = response
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, response.isGeofenceDataAdded)
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (addGeofenceResponse == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }
    }

    @Test
    fun testLAddGeofenceFail() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)
        var addGeofenceResponse: AddGeofenceResponse? = null
        mRemoteDataSourceImpl.addGeofence(
            "",
            GeofenceCons.GEOFENCE_COLLECTION,
            0.toDouble(),
            FAKE_LAT_LNG,
            object : GeofenceAPIInterface {
                override fun addGeofence(response: AddGeofenceResponse) {
                    addGeofenceResponse = response
                    Assert.assertTrue(TEST_FAILED_RECEIVED_ERROR, !response.isGeofenceDataAdded)
                }
            }
        )

        latch.await(DELAY_5000, TimeUnit.MILLISECONDS)

        if (addGeofenceResponse == null) {
            Assert.fail(TEST_FAILED_RESPONSE_ERROR)
        }
    }
}
