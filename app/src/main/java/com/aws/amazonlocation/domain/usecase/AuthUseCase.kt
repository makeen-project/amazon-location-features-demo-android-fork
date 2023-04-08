package com.aws.amazonlocation.domain.usecase

import android.app.Activity
import android.content.Context
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.repository.AuthRepository
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AuthUseCase @Inject constructor(private val mAuthRepository: AuthRepository) {

    fun signInWithAmazon(activity: Activity, signInInterface: SignInInterface) =
        mAuthRepository.signInWithAmazon(activity, signInInterface)

    fun signOutWithAmazon(
        context: Context,
        isDisconnectFromAWSRequired: Boolean,
        signInInterface: SignInInterface
    ) =
        mAuthRepository.signOutWithAmazon(context, isDisconnectFromAWSRequired, signInInterface)
}
