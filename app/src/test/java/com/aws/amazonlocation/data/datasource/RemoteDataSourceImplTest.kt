package com.aws.amazonlocation.data.datasource

import android.content.Context
import aws.sdk.kotlin.services.location.model.TravelMode
import com.amazonaws.services.geo.model.CalculateRouteResult
import com.amazonaws.services.geo.model.SearchPlaceIndexForPositionResult
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.response.NavigationData
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.`interface`.NavigationDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchDataInterface
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.mock.Responses
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.mockedInternetAvailability
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
            "Tinto",
            object : SearchPlaceInterface {
                override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                    response = suggestionResponse
                    latch.countDown()
                }

                override fun error(searchResponse: SearchSuggestionResponse) {
                    Assert.fail("Received error")
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response?.error != null) {
            Assert.fail("Received error")
        }
    }

    @Test
    fun testACalculateWalkingRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            23.013019,
            72.521230,
            23.013533,
            72.525653,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Walking.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail("Received error")
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response == null) {
            Assert.fail("Received error")
        }
    }

    @Test
    fun testBCalculateDriveRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            23.013019,
            72.521230,
            23.013533,
            72.525653,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Car.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail("Received error")
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response == null) {
            Assert.fail("Received error")
        }
    }

    @Test
    fun testCCalculateTruckRoute() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        var response: CalculateRouteResult? = null

        mRemoteDataSourceImpl.calculateRoute(
            23.013019,
            72.521230,
            23.013533,
            72.525653,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    response = success
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.fail("Received error")
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response == null) {
            Assert.fail("Received error")
        }
    }

    @Test
    fun testDCalculateTruckRouteError() {
        mockedInternetAvailability = true
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.calculateRoute(
            23013.019,
            72.521230,
            23.013533,
            72.525653,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.assertTrue(
                        "Received error",
                        exception.messageResource.toString() == TravelMode.Truck.value
                    )
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.fail("Internet connection error")
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
            23.013019,
            72.521230,
            23.013533,
            72.525653,
            isAvoidFerries = true,
            isAvoidTolls = true,
            distanceType = TravelMode.Truck.value,
            object : DistanceInterface {
                override fun distanceSuccess(success: CalculateRouteResult) {
                    latch.countDown()
                }

                override fun distanceFailed(exception: DataSourceException) {
                    Assert.assertTrue(
                        "Received error",
                        exception.messageResource.toString() == TravelMode.Truck.value
                    )
                    latch.countDown()
                }

                override fun internetConnectionError(exception: String) {
                    Assert.assertTrue("Received error", exception.isNotEmpty())
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
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response == null) {
            Assert.fail("Received error")
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
                    Assert.assertTrue("Received error", error.isNotEmpty())
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
            23.013019,
            72.521230,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    response = searchPlaceIndexForPositionResult
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.fail("Received error")
                    latch.countDown()
                }
            }
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response == null) {
            Assert.fail("Received error")
        }
    }

    @Test
    fun testISearchPlaceIndexForPositionInternetError() {
        mockedInternetAvailability = false
        setConnectivity(false)
        val latch = CountDownLatch(1)

        mRemoteDataSourceImpl.searPlaceIndexForPosition(
            23.013019,
            72.521230,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.assertTrue("Received error", error.isNotEmpty())
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.fail("Received error")
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
            230130.19,
            725212.30,
            object : SearchDataInterface {
                override fun getAddressData(searchPlaceIndexForPositionResult: SearchPlaceIndexForPositionResult) {
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail("Internet error")
                    latch.countDown()
                }

                override fun error(error: String) {
                    Assert.assertTrue("Received error", error.isNotEmpty())
                    latch.countDown()
                }
            }
        )
    }
}
