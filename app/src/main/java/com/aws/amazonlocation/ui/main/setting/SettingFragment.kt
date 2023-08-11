package com.aws.amazonlocation.ui.main.setting

import android.content.DialogInterface
import android.content.res.Configuration
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
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.data_provider.DataProviderFragment
import com.aws.amazonlocation.ui.main.map_style.MapStyleFragment
import com.aws.amazonlocation.ui.main.route_option.RouteOptionFragment
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.ui.main.unit_system.UnitSystemFragment
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.DisconnectAWSInterface
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_ID_TOKEN
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.KEY_PROVIDER
import com.aws.amazonlocation.utils.KEY_RE_START_APP
import com.aws.amazonlocation.utils.KEY_RE_START_APP_WITH_AWS_DISCONNECT
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM
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

    private lateinit var aWSCloudInformationFragment: AWSCloudInformationFragment
    private lateinit var mBinding: FragmentSettingBinding
    private val mSignInViewModel: SignInViewModel by viewModels()
    private var mAuthStatus: String? = null
    private var isTablet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.clSettings?.layoutParams?.width = width
        mBinding.clSettings?.requestLayout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        init()
        getUnitSystem()
        getDataProvider()
        getMapStyle()
        initObserver()
        clickListener()
        if (isTablet) {
            addReplaceFragment(
                R.id.frame_container,
                UnitSystemFragment(),
                addFragment = true,
                addToBackStack = false
            )
            mBinding.apply {
                clUnitSystem.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                ivUnitSystem.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
            }
            (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.UNITS)
        }
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

    private fun getUnitSystem() {
        val unitSystem =
            mPreferenceManager.getValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
        mBinding.tvUnitSystemName.text = unitSystem
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
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_OUT_SUCCESSFUL, propertiesAws)
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
                    val propertiesAws = listOf(
                        Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                    )
                    (activity as MainActivity).analyticsHelper?.recordEvent(EventType.SIGN_OUT_FAILED, propertiesAws)
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }
    }

    private fun clickListener() {
        mBinding.apply {
            clUnitSystem.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        UnitSystemFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clUnitSystem.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivUnitSystem.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.unit_system_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.UNITS)
            }
            clDataProvider.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        DataProviderFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clDataProvider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivDataProvider.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.data_provider_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.DATA_PROVIDER)
            }
            clMapStyle.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        MapStyleFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clMapStyle.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivMapStyle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.map_style_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.MAP_STYLE)
            }
            clRouteOption.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        RouteOptionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clRouteOption.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivRouteOption.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.route_option_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.DEFAULT_ROUTE_OPTIONS)
            }
            clAwsCloudformation.setOnClickListener {
                if (isTablet) {
                    aWSCloudInformationFragment = AWSCloudInformationFragment()
                    addReplaceFragment(
                        R.id.frame_container,
                        aWSCloudInformationFragment,
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clAwsCloudformation.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_view))
                        ivAwsCloudFormation.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary_green))
                    }
                } else {
                    findNavController().navigate(R.id.aws_cloud_information_fragment)
                }
                (activity as MainActivity).exitScreen()
                (activity as MainActivity).setSelectedScreen(AnalyticsAttributeValue.CONNECT_YOUR_AWS_ACCOUNT)
                val propertiesAws = listOf(
                    Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
                )
                (activity as MainActivity).analyticsHelper?.recordEvent(EventType.AWS_ACCOUNT_CONNECTION_STARTED, propertiesAws)
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

    fun hideKeyBoard() {
        if (isTablet) {
            if (this::aWSCloudInformationFragment.isInitialized) {
                if (aWSCloudInformationFragment.isVisible) {
                    aWSCloudInformationFragment.hideKeyBoard()
                }
            }
        }
    }

    private fun FragmentSettingBinding.setDefaultSelection() {
        clUnitSystem.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        ivUnitSystem.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clAwsCloudformation.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        ivAwsCloudFormation.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clRouteOption.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivRouteOption.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
        clMapStyle.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivMapStyle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_img_tint))
        clDataProvider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        ivDataProvider.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_img_tint
            )
        )
    }

    override fun logout(dialog: DialogInterface, isDisconnectFromAWSRequired: Boolean) {
        mSignInViewModel.signOutWithAmazon(requireContext(), isDisconnectFromAWSRequired)
    }
}
