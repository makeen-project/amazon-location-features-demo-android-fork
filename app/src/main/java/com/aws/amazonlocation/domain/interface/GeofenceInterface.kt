package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.enum.GeofenceBottomSheetEnum

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface GeofenceInterface {
    fun hideShowBottomNavigationBar(isHide: Boolean = false, type: GeofenceBottomSheetEnum)
}
