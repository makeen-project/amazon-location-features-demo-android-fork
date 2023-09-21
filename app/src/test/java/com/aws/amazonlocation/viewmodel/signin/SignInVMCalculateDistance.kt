package com.aws.amazonlocation.viewmodel.signin

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.mock.*
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.utils.PreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SignInVMCalculateDistance : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var locationSearchImp: AuthImp

    private lateinit var locationSearchUseCase: AuthUseCase

    private lateinit var mSignInVM: SignInViewModel

    override fun setUp() {
        super.setUp()
        val mPreferenceManager = PreferenceManager(context)
        locationSearchImp = AuthImp(mRemoteDataSourceImpl)
        locationSearchUseCase = AuthUseCase(locationSearchImp)
        mSignInVM = SignInViewModel(locationSearchUseCase, mPreferenceManager)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun calculateDistanceSuccess() = runTest {
//        Mockito.`when`(mRemoteDataSourceImpl.calculateRoute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any())).thenAnswer {
//            val mode = it.arguments[6] as String
//            val callback: DistanceInterface = it.arguments[7] as DistanceInterface
//
//            when (mode) {
//                TravelMode.Walking.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
//                TravelMode.Car.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
//                TravelMode.Truck.value -> callback.distanceSuccess(Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
//            }
//        }
//
//        mSignInVM.mSignInResponse.test {
//            val start = DISTANCE_COORDINATE_FROM
//            val end = DISTANCE_COORDINATE_TO
//            mSignInVM.signInWithAmazon(activity = )
//            var result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
//            result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
//            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_CAR)
//            mSignInVM.calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude, AVOID_FERRIES, AVOID_TOLLS, true)
//            result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
//            result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
//            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_WALKING)
//            result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_LOADING, result is HandleResult.Loading)
//            result = awaitItem()
//            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
//            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, (result as? HandleResult.Success)?.response?.calculateRouteResult == Responses.RESPONSE_CALCULATE_DISTANCE_TRUCK)
//        }
    }
}
