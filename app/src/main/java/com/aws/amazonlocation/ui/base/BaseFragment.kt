package com.aws.amazonlocation.ui.base

import android.content.Context
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aws.amazonlocation.R
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.BottomSheetHelper
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
abstract class BaseFragment : Fragment() {

    var mBaseActivity: BaseActivity? = null

    @Inject
    lateinit var mMapHelper: MapHelper

    @Inject
    lateinit var mBottomSheetHelper: BottomSheetHelper

    @Inject
    lateinit var mAWSLocationHelper: AWSLocationHelper

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as BaseActivity?
    }

    fun showError(error: String) {
        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.nav_host_fragment),
            error,
            Snackbar.LENGTH_SHORT
        )
        val textView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 10
        snackBar.show()
    }
}
