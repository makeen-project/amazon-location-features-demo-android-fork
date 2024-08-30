package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.location.model.TravelMode
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExploreVMCalculateDistance : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var locationSearchImp: LocationSearchImp

    private lateinit var locationSearchUseCase: LocationSearchUseCase

    private lateinit var mExploreVM: ExploreViewModel

    override fun setUp() {
        super.setUp()

        locationSearchImp = LocationSearchImp(mRemoteDataSourceImpl)
        locationSearchUseCase = LocationSearchUseCase(locationSearchImp)
        mExploreVM = ExploreViewModel(locationSearchUseCase)
        mExploreVM.mCarData = null
        mExploreVM.mWalkingData = null
        mExploreVM.mTruckData = null
        mExploreVM.mBicycleData = null
        mExploreVM.mMotorcycleData = null
    }

    @Test
    fun calculateDistanceSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val mode = it.arguments[6] as String
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface

            when (mode) {
                TravelMode.Walking.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
                TravelMode.Car.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
                TravelMode.Truck.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
            }
        }

        mExploreVM.mCalculateDistance.test {
            val start = DISTANCE_COORDINATE_FROM
            val end = DISTANCE_COORDINATE_TO
            mExploreVM.mStartLatLng = null
            mExploreVM.mDestinationLatLng = null
            mExploreVM.calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
            mExploreVM.calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, true)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateDistanceError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val distanceType = it.arguments[6] as String?
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.distanceFailed(DataSourceException.Error(distanceType!!))
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateDistance(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TravelMode.Car.value)

            mExploreVM.calculateDistance(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, true)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TravelMode.Walking.value)

            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == TravelMode.Truck.value)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateDistanceInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
            callback.internetConnectionError(NO_INTERNET_ERROR)
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateDistance(null, null, null, null, AVOID_FERRIES, AVOID_TOLLS, false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as? HandleResult.Error)?.exception?.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
