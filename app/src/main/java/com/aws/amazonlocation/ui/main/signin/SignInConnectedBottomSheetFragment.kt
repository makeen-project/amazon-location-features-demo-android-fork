package com.aws.amazonlocation.ui.main.signin

import android.app.Dialog
import android.os.Bundle
import android.view.*
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetSigninConnectedBinding
import com.aws.amazonlocation.domain.`interface`.SignInConnectInterface
import com.aws.amazonlocation.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class SignInConnectedBottomSheetFragment(private var mSignInConnectInterface: SignInConnectInterface) :
    BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetSigninConnectedBinding

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    private var mPoolId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                behaviour.isDraggable = false
                setupFullHeight(layout)
            }
        }
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = BottomSheetSigninConnectedBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mSignInConnectInterface.continueToExplore(dialog)
            }
            true
        }
        mPoolId = mPreferenceManager.getValue(KEY_POOL_ID, "")
        mPreferenceManager.setValue(KEY_RE_START_APP, false)
        mPreferenceManager.setValue(KEY_TAB_ENUM, "")
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            btnSignIn.setOnClickListener {
                mSignInConnectInterface.signIn(dialog)
            }

            btnContinueToExplore.setOnClickListener {
                mSignInConnectInterface.continueToExplore(dialog)
            }

            cardSignInConnectedClose.setOnClickListener {
                mSignInConnectInterface.continueToExplore(dialog)
            }
        }
    }
}
