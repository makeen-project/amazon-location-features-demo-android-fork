package com.aws.amazonlocation.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMoreBinding
import com.aws.amazonlocation.ui.base.BaseFragment

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MoreFragment : BaseFragment() {

    private lateinit var mBinding: FragmentMoreBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMoreBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListener()
    }

    private fun clickListener() {
        mBinding.apply {
            clAttribution.setOnClickListener {
                findNavController().navigate(R.id.attribution_fragment)
            }
            clAbout.setOnClickListener {
                findNavController().navigate(R.id.about_fragment)
            }
            clTermsConditions.setOnClickListener {
                findNavController().navigate(R.id.terms_conditions_fragment)
            }
        }
    }
}
