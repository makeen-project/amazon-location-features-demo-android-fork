package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.DeleteLocationHistoryResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface LocationDeleteHistoryInterface {
    fun success(historyResponse: DeleteLocationHistoryResponse) {}
}
