package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.NavigationData

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface NavigationDataInterface {
    fun getNavigationList(navigationData: NavigationData)

    fun internetConnectionError(error: String) {}
}
