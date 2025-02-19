package com.aws.amazonlocation.viewmodel.explore

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import aws.sdk.kotlin.services.georoutes.model.RouteTravelMode
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.LocationSearchImp
import com.aws.amazonlocation.domain.`interface`.DistanceInterface
import com.aws.amazonlocation.domain.usecase.LocationSearchUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.explore.AvoidanceOption
import com.aws.amazonlocation.ui.main.explore.DepartOption
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
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
        mExploreVM.mScooterData = null
        mExploreVM.mCarCalculateDistanceResponse = null
        mExploreVM.mWalkCalculateDistanceResponse = null
        mExploreVM.mTruckCalculateDistanceResponse = null
        mExploreVM.mScooterCalculateDistanceResponse = null
        mExploreVM.mIsAvoidTolls = false
        mExploreVM.mIsAvoidFerries = false
        mExploreVM.mIsAvoidDirtRoads = false
        mExploreVM.mIsAvoidUTurns = false
        mExploreVM.mIsAvoidTunnels = false
        mExploreVM.mIsRouteOptionsOpened  = false
        mExploreVM.mIsDepartOptionsOpened  = false
        mExploreVM.mRouteFinish  = false
        mExploreVM.mIsSwapClicked  = false
        mExploreVM.mIsDirectionDataSet  = false
        mExploreVM.mIsDirectionDataSetNew  = false
        mExploreVM.mIsDirectionSheetHalfExpanded  = false
        mExploreVM.mIsLocationAlreadyEnabled  = false
        mExploreVM.mIsCurrentLocationClicked  = false
        mExploreVM.mIsTrackingLocationClicked  = false
        mExploreVM.isCalculateDriveApiError  = false
        mExploreVM.isCalculateWalkApiError  = false
        mExploreVM.isCalculateTruckApiError  = false
        mExploreVM.isCalculateScooterApiError  = false
        mExploreVM.isLocationUpdatedNeeded  = false
        mExploreVM.isZooming  = false
        mExploreVM.isDataSearchForDestination  = false
        mExploreVM.isLiveLocationClick  = false
        mExploreVM.mLastClickTime  = 0

    }

    @Test
    fun calculateDistanceSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(),  any(),anyOrNull())).thenAnswer {
            val mode = it.arguments[6] as String
            val callback: DistanceInterface = it.arguments[8] as DistanceInterface

            when (mode) {
                RouteTravelMode.Car.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
                RouteTravelMode.Truck.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
                RouteTravelMode.Scooter.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_SCOOTER)
                RouteTravelMode.Pedestrian.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
            }
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.mStartLatLng = null
            mExploreVM.mDestinationLatLng = null
            val start = DISTANCE_COORDINATE_FROM
            val end = DISTANCE_COORDINATE_TO
            mExploreVM.calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude, arrayListOf<AvoidanceOption>().apply {
                add(AvoidanceOption.FERRIES)
                add(AvoidanceOption.TOLL_ROADS)
            },  DepartOption.LEAVE_NOW.name,"",false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, mExploreVM.mStartLatLng == start)
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, mExploreVM.mDestinationLatLng == end)
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
            mExploreVM.calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude, arrayListOf<AvoidanceOption>().apply {
                if (mExploreVM.mIsAvoidTolls) add(AvoidanceOption.TOLL_ROADS)
                if (mExploreVM.mIsAvoidFerries)  add(AvoidanceOption.FERRIES)
                if (mExploreVM.mIsAvoidDirtRoads)  add(AvoidanceOption.DIRT_ROADS)
                if (mExploreVM.mIsAvoidUTurns)  add(AvoidanceOption.U_TURNS)
                if (mExploreVM.mIsAvoidTunnels)  add(AvoidanceOption.TUNNELS)
            }, DepartOption.LEAVE_NOW.name,"",true)
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
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_SCOOTER)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateDistanceError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(),  any(), anyOrNull(), anyOrNull())).thenAnswer {
            val distanceType = it.arguments[6] as String?
            val callback: DistanceInterface = it.arguments[8] as DistanceInterface
            callback.distanceFailed(DataSourceException.Error(distanceType!!))
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateDistance(null, null, null, null, arrayListOf<AvoidanceOption>().apply {
                add(AvoidanceOption.FERRIES)
                add(AvoidanceOption.TOLL_ROADS)
            }, DepartOption.LEAVE_NOW.name,"",false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Car.value)

            mExploreVM.calculateDistance(null, null, null, null, arrayListOf<AvoidanceOption>().apply {
                add(AvoidanceOption.FERRIES)
                add(AvoidanceOption.TOLL_ROADS)
                add(AvoidanceOption.U_TURNS)
                add(AvoidanceOption.DIRT_ROADS)
                add(AvoidanceOption.TUNNELS)
            }, DepartOption.LEAVE_NOW.name,"",true)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Pedestrian.value)

            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as? HandleResult.Error)?.exception?.messageResource == RouteTravelMode.Truck.value)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun calculateDistanceInternetError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any(),  any(), anyOrNull(), anyOrNull())).thenAnswer {
            val callback: DistanceInterface = it.arguments[8] as DistanceInterface
            callback.internetConnectionError(NO_INTERNET_ERROR)
        }

        mExploreVM.mCalculateDistance.test {
            mExploreVM.calculateDistance(null, null, null, null, arrayListOf<AvoidanceOption>().apply {
                add(AvoidanceOption.FERRIES)
                add(AvoidanceOption.TOLL_ROADS)
                add(AvoidanceOption.U_TURNS)
                add(AvoidanceOption.DIRT_ROADS)
                add(AvoidanceOption.TUNNELS)
            }, DepartOption.LEAVE_NOW.name,"",false)
            var result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
            result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR, (result as? HandleResult.Error)?.exception?.messageResource == NO_INTERNET_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
