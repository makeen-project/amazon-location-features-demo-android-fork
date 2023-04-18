package com.aws.amazonlocation.viewmodel.geofence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.GeofenceImp
import com.aws.amazonlocation.data.response.AddGeofenceResponse
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import com.aws.amazonlocation.ui.main.geofence.GeofenceViewModel
import com.aws.amazonlocation.utils.GeofenceCons
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeofenceVMGeofenceAddTest : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var geofenceImp: GeofenceImp

    private lateinit var geofenceUseCase: GeofenceUseCase

    private lateinit var mGeofenceViewModel: GeofenceViewModel

    override fun setUp() {
        super.setUp()

        geofenceImp = GeofenceImp(mRemoteDataSourceImpl)
        geofenceUseCase = GeofenceUseCase(geofenceImp)
        mGeofenceViewModel = GeofenceViewModel(geofenceUseCase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun geofenceAddSuccess() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.addGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[4] as GeofenceAPIInterface
            callback.addGeofence(
                AddGeofenceResponse(
                    isGeofenceDataAdded = true,
                    errorMessage = null
                )
            )
        }

        mGeofenceViewModel.mAddGeofence.test {
            mGeofenceViewModel.addGeofence(
                "11",
                GeofenceCons.GEOFENCE_COLLECTION,
                80.toDouble(),
                LatLng(49.281174, -123.116823)
            )
            val result = awaitItem()
            assert(result is HandleResult.Success)
            assert((result as HandleResult.Success).response.isGeofenceDataAdded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun geofenceAddFail() = runTest {
        Mockito.`when`(
            mRemoteDataSourceImpl.addGeofence(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenAnswer {
            val callback: GeofenceAPIInterface = it.arguments[4] as GeofenceAPIInterface
            callback.addGeofence(
                AddGeofenceResponse(
                    isGeofenceDataAdded = false,
                    errorMessage = "No data found"
                )
            )
        }

        mGeofenceViewModel.mAddGeofence.test {
            mGeofenceViewModel.addGeofence(
                "11",
                GeofenceCons.GEOFENCE_COLLECTION,
                80.toDouble(),
                LatLng(49.281174, -123.116823)
            )
            val result = awaitItem()
            assert(result is HandleResult.Error)
            (result as HandleResult.Error).exception.messageResource?.equals("No data found")
                ?.let { assert(it) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
