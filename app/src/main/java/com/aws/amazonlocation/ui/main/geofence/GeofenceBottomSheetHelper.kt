package com.aws.amazonlocation.ui.main.geofence

import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.domain.`interface`.SignInConnectInterface
import com.aws.amazonlocation.domain.`interface`.SignInRequiredInterface
import com.aws.amazonlocation.ui.main.signin.CloudFormationBottomSheetFragment
import com.aws.amazonlocation.ui.main.signin.SignInConnectedBottomSheetFragment
import com.aws.amazonlocation.ui.main.signin.SignInRequiredBottomSheetFragment

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceBottomSheetHelper(private var mActivity: FragmentActivity) {
    private var mCloudFormationBottomSheetFragment: CloudFormationBottomSheetFragment? = null
    fun signInRequiredBottomSheet(mSignInRequiredInterface: SignInRequiredInterface) {
        val mSignInRequiredBottomSheetFragment =
            SignInRequiredBottomSheetFragment(mSignInRequiredInterface)
        mActivity.supportFragmentManager.let { mSignInRequiredBottomSheetFragment.show(it, "") }
    }

    fun cloudFormationBottomSheet(
        tabEnum: TabEnum,
        mCloudFormationInterface: CloudFormationInterface
    ) {
        mCloudFormationBottomSheetFragment =
            CloudFormationBottomSheetFragment(tabEnum, mCloudFormationInterface)
        mCloudFormationBottomSheetFragment?.let { cloudFormationBottomSheet ->
            mActivity.supportFragmentManager.let {
                cloudFormationBottomSheet.show(
                    it,
                    CloudFormationBottomSheetFragment::class.java.name
                )
            }
        }
    }

    fun isCloudFormationBottomSheetVisible(): Boolean {
        mCloudFormationBottomSheetFragment?.let { cloudFormationBottomSheet ->
            return cloudFormationBottomSheet.isVisible
        }
        return false
    }

    fun cloudFormationBottomSheetHideKeyboard() {
        mCloudFormationBottomSheetFragment?.hideKeyBoard()
    }

    fun signInConnectedBottomSheet(mSignInConnectInterface: SignInConnectInterface) {
        val mSignInConnectedBottomSheetFragment =
            SignInConnectedBottomSheetFragment(mSignInConnectInterface)
        mActivity.supportFragmentManager.let { mSignInConnectedBottomSheetFragment.show(it, "") }
    }
}
