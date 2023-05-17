package com.aws.amazonlocation.ui.main.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentVersionBinding
import com.aws.amazonlocation.ui.base.BaseFragment

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class VersionFragment : BaseFragment() {

    private lateinit var mBinding: FragmentVersionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentVersionBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        backPress()
    }

    private fun init() {
        mBinding.apply {
            tvAppVersion.text = buildString {
                append(getString(R.string.label_app_version))
                append(" ")
                append(BuildConfig.VERSION_NAME)
            }
            ivBack?.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
