package com.aws.amazonlocation.ui.main.more

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentAboutBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.about.VersionFragment
import com.aws.amazonlocation.ui.main.attribution.AttributionFragment
import com.aws.amazonlocation.ui.main.terms_condition.TermsAndConditionFragment
import com.aws.amazonlocation.ui.main.web_view.WebViewActivity
import com.aws.amazonlocation.utils.KEY_URL

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class AboutFragment : BaseFragment() {

    private lateinit var mBinding: FragmentAboutBinding
    private var isTablet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAboutBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val width = resources.getDimensionPixelSize(R.dimen.screen_size)
        mBinding.clAbout?.layoutParams?.width = width
        mBinding.clAbout?.requestLayout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isTablet = requireContext().resources.getBoolean(R.bool.is_tablet)
        clickListener()
        if (isTablet) {
            addReplaceFragment(
                R.id.frame_container,
                AttributionFragment(),
                addFragment = true,
                addToBackStack = false
            )
            mBinding.apply {
                clAttribution.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_view
                    )
                )
            }
        }
    }

    private fun clickListener() {
        mBinding.apply {
            clAttribution.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        AttributionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clAttribution.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_view
                            )
                        )
                    }
                } else {
                    findNavController().navigate(R.id.attribution_fragment)
                }
            }
            clVersion.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        VersionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clVersion.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_view
                            )
                        )
                    }
                } else {
                    findNavController().navigate(R.id.version_fragment)
                }
            }
            clTermsConditions.setOnClickListener {
                if (isTablet) {
                    addReplaceFragment(
                        R.id.frame_container,
                        TermsAndConditionFragment(),
                        addFragment = false,
                        addToBackStack = false
                    )
                    mBinding.apply {
                        setDefaultSelection()
                        clTermsConditions.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_view
                            )
                        )
                    }
                } else {
                    findNavController().navigate(R.id.terms_conditions_fragment)
                }
            }
            clHelp.setOnClickListener {
                startActivity(
                    Intent(
                        context,
                        WebViewActivity::class.java
                    ).putExtra(
                        KEY_URL,
                        BuildConfig.BASE_DOMAIN + BuildConfig.CLOUD_FORMATION_READ_MORE_URL
                    )
                )
            }
        }
    }

    private fun setDefaultSelection() {
        mBinding.apply {
            clAttribution.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            clTermsConditions.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            clVersion.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
}
