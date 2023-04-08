package com.aws.amazonlocation.data.response

import com.amazonaws.services.geo.model.GetDevicePositionHistoryResult

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class LocationHistoryResponse(var errorMessage: String?, var response: GetDevicePositionHistoryResult?)
