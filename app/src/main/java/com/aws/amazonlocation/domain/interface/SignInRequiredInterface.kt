package com.aws.amazonlocation.domain.`interface`

import android.app.Dialog

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SignInRequiredInterface {
    fun signInClick(dialog: Dialog?)
    fun mayBeLaterClick(dialog: Dialog?)
}
