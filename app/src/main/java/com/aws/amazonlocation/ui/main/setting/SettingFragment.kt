package com.aws.amazonlocation.ui.main.setting

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.databinding.FragmentSettingBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.utils.DisconnectAWSInterface
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_PROVIDER
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_RE_START_APP_WITH_AWS_DISCONNECT
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.SignOutInterface
import com.aws.amazonlocation.utils.disconnectFromAWSDialog
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.signOutDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SettingFragment : BaseFragment(), SignOutInterface {

    private lateinit var mBinding: FragmentSettingBinding
    private val mSignInViewModel: SignInViewModel by viewModels()
    private var mAuthStatus: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getDataProvider()
        getMapStyle()
        initObserver()
        clickListener()
    }

    private fun init() {
        mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        mBinding.apply {
            if (!mAuthStatus.isNullOrEmpty()) {
                when (mAuthStatus) {
                    AuthEnum.SIGNED_IN.name -> {
                        ivDisconnect.setImageDrawable(ContextCompat.getDrawable(ivDisconnect.context, R.drawable.icon_log_out))
                        tvDisconnect.text = getText(R.string.label_sign_out)
                        clDisconnect.show()
                    }
                    AuthEnum.AWS_CONNECTED.name -> {
                        ivDisconnect.setImageDrawable(ContextCompat.getDrawable(ivDisconnect.context, R.drawable.ic_plug))
                        tvDisconnect.text = getString(R.string.label_disconnect)
                        clDisconnect.show()
                    }
                    else -> {
                        clDisconnect.hide()
                    }
                }
            } else {
                clDisconnect.hide()
            }
        }
    }

    private fun getDataProvider() {
        val dataProvider =
            mPreferenceManager.getValue(KEY_MAP_NAME, resources.getString(R.string.esri))
        mBinding.tvDataProviderName.text = dataProvider
    }

    private fun getMapStyle() {
        val mapStyleNameDisplay =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                ?: getString(R.string.map_light)
        mBinding.tvMapStyleName.text = mapStyleNameDisplay
    }

    private fun initObserver() {
        lifecycleScope.launchWhenStarted {
            mSignInViewModel.mSignOutResponse.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    mBaseActivity?.clearUserInFo()
                    mBaseActivity?.mPreferenceManager?.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.AWS_CONNECTED.name
                    )
                    init()
                    mPreferenceManager.removeValue(KEY_ID_TOKEN)
                    mPreferenceManager.removeValue(KEY_PROVIDER)
                    showError(it.message.toString())
                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                    activity?.restartApplication()
                }.onError { it ->
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }
    }

    private fun clickListener() {
        mBinding.apply {
            clDataProvider.setOnClickListener {
                findNavController().navigate(R.id.data_provider_fragment)
            }
            clMapStyle.setOnClickListener {
                findNavController().navigate(R.id.map_style_fragment)
            }
            clRouteOption.setOnClickListener {
                findNavController().navigate(R.id.route_option_fragment)
            }
            clAwsCloudformation.setOnClickListener {
                findNavController().navigate(R.id.aws_cloud_information_fragment)
            }
            clDisconnect.setOnClickListener {
                if (tvDisconnect.text.toString().trim() == getText(R.string.label_sign_out)) {
                    requireContext().signOutDialog(this@SettingFragment)
                } else {
                    requireContext().disconnectFromAWSDialog(
                        object : DisconnectAWSInterface {
                            override fun disconnectAWS(dialog: DialogInterface) {
                                lifecycleScope.launch {
                                    mPreferenceManager.setValue(KEY_RE_START_APP, true)
                                    mPreferenceManager.setValue(KEY_RE_START_APP_WITH_AWS_DISCONNECT, true)
                                    mPreferenceManager.setDefaultConfig()
                                    mPreferenceManager.removeValue(KEY_ID_TOKEN)
                                    mPreferenceManager.removeValue(KEY_PROVIDER)
                                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                                    activity?.restartApplication()
                                }
                                dialog.dismiss()
                            }

                            override fun logoutAndDisconnectAWS(dialog: DialogInterface) {
                            }
                        },
                        AWSMobileClient.getInstance().isSignedIn
                    )
                }
            }
        }
    }

    override fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean) {
        mSignInViewModel.signOutWithAmazon(requireContext(), isDisconnectFromAWSRequired)
    }
}