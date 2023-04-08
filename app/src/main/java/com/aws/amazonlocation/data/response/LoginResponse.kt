package com.aws.amazonlocation.data.response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
data class LoginResponse(
    var name: String? = null,
    var email: String? = null,
    var success: String? = null,
    var provider: String? = null,
    var idToken: String? = null
)
