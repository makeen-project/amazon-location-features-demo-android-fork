package com.aws.amazonlocation.domain.repository

import android.app.Activity
import android.content.Context
import com.aws.amazonlocation.domain.`interface`.SignInInterface

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface AuthRepository {

    fun signInWithAmazon(activity: Activity, signInInterface: SignInInterface)

    fun signOutWithAmazon(
        context: Context,
        isDisconnectFromAWSRequired: Boolean,
        signInInterface: SignInInterface
    )
}
