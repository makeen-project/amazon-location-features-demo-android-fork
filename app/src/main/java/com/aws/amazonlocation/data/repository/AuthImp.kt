package com.aws.amazonlocation.data.repository

import android.app.Activity
import android.content.Context
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.repository.AuthRepository

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AuthImp(private val mRemoteDataSourceImpl: RemoteDataSourceImpl) : AuthRepository {

    override fun signInWithAmazon(activity: Activity, signInInterface: SignInInterface) {
        mRemoteDataSourceImpl.signInWithAmazon(activity, signInInterface)
    }

    override fun signOutWithAmazon(
        context: Context,
        isDisconnectFromAWSRequired: Boolean,
        signInInterface: SignInInterface
    ) {
        mRemoteDataSourceImpl.signOutWithAmazon(
            context,
            isDisconnectFromAWSRequired,
            signInInterface
        )
    }
}
