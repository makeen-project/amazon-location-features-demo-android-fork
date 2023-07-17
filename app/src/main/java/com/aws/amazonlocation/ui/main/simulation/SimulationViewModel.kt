package com.aws.amazonlocation.ui.main.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.data.common.DataSourceException
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.response.GeofenceData
import com.aws.amazonlocation.data.response.UpdateBatchLocationResponse
import com.aws.amazonlocation.domain.`interface`.BatchLocationUpdateInterface
import com.aws.amazonlocation.domain.`interface`.GeofenceAPIInterface
import com.aws.amazonlocation.domain.usecase.GeofenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@HiltViewModel
class SimulationViewModel @Inject constructor(
    private var mGetGeofenceUseCase: GeofenceUseCase
) :
    ViewModel() {

    private val _getGeofenceList =
        Channel<HandleResult<ArrayList<ListGeofenceResponseEntry>>>(Channel.BUFFERED)
    val mGetGeofenceList: Flow<HandleResult<ArrayList<ListGeofenceResponseEntry>>> =
        _getGeofenceList.receiveAsFlow()

    private val _getUpdateDevicePosition =
        Channel<HandleResult<UpdateBatchLocationResponse>>(Channel.BUFFERED)
    val mGetUpdateDevicePosition: Flow<HandleResult<UpdateBatchLocationResponse>> =
        _getUpdateDevicePosition.receiveAsFlow()

    fun getGeofenceList(collectionName: String) {
        _getGeofenceList.trySend(HandleResult.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.getGeofenceList(
                collectionName,
                object : GeofenceAPIInterface {
                    override fun getGeofenceList(geofenceData: GeofenceData) {
                        if (!geofenceData.message.isNullOrEmpty()) {
                            geofenceData.message?.let {
                                _getGeofenceList.trySend(
                                    HandleResult.Error(
                                        DataSourceException.Error(
                                            it
                                        )
                                    )
                                )
                            }
                        } else {
                            _getGeofenceList.trySend(HandleResult.Success(geofenceData.geofenceList))
                        }
                    }
                }
            )
        }
    }

    fun evaluateGeofence(
        trackerName: String,
        position1: List<Double>? = null,
        position2: List<Double>? = null,
        position3: List<Double>? = null,
        position4: List<Double>? = null,
        position5: List<Double>? = null,
        position6: List<Double>? = null,
        position7: List<Double>? = null,
        position8: List<Double>? = null,
        position9: List<Double>? = null,
        position10: List<Double>? = null,
        deviceId: String,
        date: Date
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            mGetGeofenceUseCase.evaluateGeofence(
                trackerName,
                position1,
                position2,
                position3,
                position4,
                position5,
                position6,
                position7,
                position8,
                position9,
                position10,
                deviceId,
                date,
                object : BatchLocationUpdateInterface {
                    override fun success(searchResponse: UpdateBatchLocationResponse) {
                        if (searchResponse.isLocationDataAdded) {
                            _getUpdateDevicePosition.trySend(HandleResult.Success(searchResponse))
                        }
                    }
                }
            )
        }
    }
}
