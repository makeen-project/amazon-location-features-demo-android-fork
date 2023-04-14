package com.aws.amazonlocation.ui.main.signin

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.databinding.BottomSheetCloudFormationBinding
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.HTTPS
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_POOL_ID
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_TAB_ENUM
import com.aws.amazonlocation.utils.KEY_URL
import com.aws.amazonlocation.utils.KEY_USER_DOMAIN
import com.aws.amazonlocation.utils.KEY_USER_POOL_CLIENT_ID
import com.aws.amazonlocation.utils.KEY_USER_POOL_ID
import com.aws.amazonlocation.utils.KEY_USER_REGION
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.WEB_SOCKET_URL
import com.aws.amazonlocation.utils.changeClickHereColor
import com.aws.amazonlocation.utils.changeLearnMoreColor
import com.aws.amazonlocation.utils.changeTermsAndConditionColor
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.validateIdentityPoolId
import com.aws.amazonlocation.utils.validateUserPoolClientId
import com.aws.amazonlocation.utils.validateUserPoolId
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class CloudFormationBottomSheetFragment(
    private val mTabEnum: TabEnum,
    private val mCloudFormationInterface: CloudFormationInterface
) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetCloudFormationBinding

    private var mUserPoolClientId: String? = null
    private var mUserPoolId: String? = null
    private var mWebSocketUrl: String? = null

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    private var mIdentityPoolId: String? = null
    private var mUserDomain: String? = null
    private var regionData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                behaviour.isDraggable = false
                setupFullHeight(layout)
            }
        }
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
        mBinding = BottomSheetCloudFormationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mCloudFormationInterface.dialogDismiss(dialog)
            }
            true
        }
        init()
    }

    private fun init() {
        changeTermsAndConditionColor(mBinding.tvTermsCondition)
        changeClickHereColor(mBinding.tvTitle11, clickHere)
        changeLearnMoreColor(
            mBinding.tvTvTitle22,
            object : CloudFormationInterface {
                override fun clickHere(url: String) {
                    startActivity(
                        Intent(
                            context,
                            WebViewActivity::class.java
                        ).putExtra(KEY_URL, url)
                    )
                }
            }
        )
        clickListener()
        cloudFormationValidation()
    }

    private fun clickListener() {
        mBinding.apply {
            edtIdentityPoolId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserDomain.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserPoolClientId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtUserPoolId.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }
            edtWebSocketUrl.doOnTextChanged { _, _, _, _ ->
                cloudFormationValidation()
            }

            cardSignInConnectedClose.setOnClickListener {
                mPreferenceManager.setValue(KEY_TAB_ENUM, "")
                mPreferenceManager.setValue(KEY_CLOUD_FORMATION_STATUS, "")
                mCloudFormationInterface.dialogDismiss(dialog)
            }

            btnConnect.setOnClickListener {
                mIdentityPoolId = edtIdentityPoolId.text.toString().trim()
                mUserDomain = edtUserDomain.text.toString().trim()
                mUserPoolClientId = edtUserPoolClientId.text.toString().trim()
                mUserPoolId = edtUserPoolId.text.toString().trim()
                mWebSocketUrl = edtWebSocketUrl.text.toString().trim()
                mIdentityPoolId?.let { identityPId ->
                    identityPId.split(":").let { splitStringList ->
                        splitStringList[0].let { region ->
                            regionData = region
                        }
                    }
                }
                regionData?.let { _ -> validateAWSAccountData() }
            }

            nsCloudFormation.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
                    val topDetector = nsCloudFormation.scrollY
                    if (topDetector <= 0) {
                        tvAwsCloudFormationRequired.alpha = 0f
                        viewScroll.alpha = 0f
                    } else {
                        tvAwsCloudFormationRequired.alpha = 1f
                        viewScroll.alpha = 1f
                    }
                }
            )
        }
    }

    private fun storeDataAndRestartApp() {
        lifecycleScope.launch {
            mPreferenceManager.setValue(
                KEY_CLOUD_FORMATION_STATUS,
                AuthEnum.AWS_CONNECTED.name
            )
            storeDataInPreference()
            if (!isRunningTest) {
                delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                activity?.restartApplication()
            }
            dismiss()
        }
    }

    private fun validateAWSAccountData() {
        if (!validateIdentityPoolId(mIdentityPoolId, regionData)) {
            showError(getString(R.string.label_enter_identity_pool_id))
        } else if (!URLUtil.isValidUrl(mUserDomain)) {
            showError(getString(R.string.label_enter_domain))
        } else if (!validateUserPoolClientId(mUserPoolClientId)) {
            showError(getString(R.string.label_enter_user_pool_client_id))
        } else if (!validateUserPoolId(mUserPoolId)) {
            showError(getString(R.string.label_enter_user_pool_id))
        } else if (mWebSocketUrl.isNullOrEmpty()) {
            showError(getString(R.string.label_enter_web_socket_url))
        } else {
            storeDataAndRestartApp()
        }
    }

    fun showError(error: String) {
        val snackBar = dialog?.window?.let {
            Snackbar.make(
                it.decorView,
                error,
                Snackbar.LENGTH_SHORT
            )
        }
        val textView = snackBar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView?.maxLines = 10
        snackBar?.show()
    }

    private fun cloudFormationValidation() {
        mBinding.apply {
            if (edtIdentityPoolId.text.toString().isNotEmpty() && edtUserDomain.text.toString()
                .isNotEmpty() && edtUserPoolClientId.text.toString()
                    .isNotEmpty() && edtUserPoolId.text.toString()
                    .isNotEmpty() && edtWebSocketUrl.text.toString().isNotEmpty()
            ) {
                btnConnect.alpha = 1f
                btnConnect.isEnabled = true
            } else {
                btnConnect.alpha = 0.4f
                btnConnect.isEnabled = false
            }
        }
    }

    private fun storeDataInPreference() {
        mPreferenceManager.setValue(KEY_TAB_ENUM, mTabEnum.name)

        mPreferenceManager.setValue(
            KEY_RE_START_APP,
            true
        )

        mIdentityPoolId?.let { identityPId ->
            mPreferenceManager.setValue(
                KEY_POOL_ID,
                identityPId
            )

            identityPId.split(":").let { splitStringList ->
                splitStringList[0].let { region ->
                    mPreferenceManager.setValue(
                        KEY_USER_REGION,
                        region
                    )
                }
            }
        }
        mUserDomain?.let { uDomain ->
            uDomain.split(HTTPS)[1].let { domain ->
                mPreferenceManager.setValue(
                    KEY_USER_DOMAIN,
                    domain
                )
            }
        }

        mUserPoolClientId?.let { uPoolClientId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_CLIENT_ID,
                uPoolClientId
            )
        }
        mUserPoolId?.let { uPoolId ->
            mPreferenceManager.setValue(
                KEY_USER_POOL_ID,
                uPoolId
            )
        }
        mWebSocketUrl?.let { webSocketUrl ->
            mPreferenceManager.setValue(
                WEB_SOCKET_URL,
                webSocketUrl
            )
        }
    }

    private var clickHere = object : CloudFormationInterface {
        override fun clickHere(url: String) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }
}