package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.LoginResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SignInInterface {

    fun signInFailed(exception: String?) {}

    fun signOutSuccess(success: String, isDisconnectFromAWSRequired: Boolean) {}

    fun signOutFailed(error: String) {}

    fun getUserDetails(mLoginResponse: LoginResponse) {}
}
