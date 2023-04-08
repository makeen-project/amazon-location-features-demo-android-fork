package com.aws.amazonlocation.ui.main.signin

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetSigninRequiredBinding
import com.aws.amazonlocation.domain.`interface`.SignInRequiredInterface
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class SignInRequiredBottomSheetFragment(private var mSignInRequiredInterface: SignInRequiredInterface) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetSigninRequiredBinding

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    private var mPoolId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = BottomSheetSigninRequiredBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPoolId = mPreferenceManager.getValue(KEY_POOL_ID, "")
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mSignInRequiredInterface.mayBeLaterClick(dialog)
            }
            true
        }
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            btnSignIn.setOnClickListener {
                mSignInRequiredInterface.signInClick(dialog)
            }

            tvMayBeLater.setOnClickListener {
                mSignInRequiredInterface.mayBeLaterClick(dialog)
            }
        }
    }
}
