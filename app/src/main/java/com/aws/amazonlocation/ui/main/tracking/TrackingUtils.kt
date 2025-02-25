package com.aws.amazonlocation.ui.main.tracking

import android.app.Activity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetTrackingBinding
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.simulation.SimulationBottomSheetFragment
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW_1
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.providers.LocationProvider
import com.aws.amazonlocation.utils.show
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

class TrackingUtils(
    val mPreferenceManager: PreferenceManager? = null,
    val activity: Activity?,
    val mLocationProvider: LocationProvider
) {
    var isChangeDataProviderClicked: Boolean = false
    private var mBottomSheetTrackingBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var mBindingTracking: BottomSheetTrackingBinding? = null
    private var mFragmentActivity: FragmentActivity? = null

    fun isTrackingExpandedOrHalfExpand(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun isTrackingSheetCollapsed(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
    }

    fun isTrackingSheetHidden(): Boolean {
        return mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_HIDDEN || mBottomSheetTrackingBehavior?.state == BottomSheetBehavior.STATE_SETTLING
    }

    fun collapseTracking() {
        mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mBottomSheetTrackingBehavior?.isDraggable = true
    }

    fun showTrackingBottomSheet() {
        mBottomSheetTrackingBehavior?.isHideable = false
        mBottomSheetTrackingBehavior?.isDraggable = true
        mBottomSheetTrackingBehavior?.isFitToContents = false
        mBindingTracking?.clEnableTracking?.context?.let {
            if ((activity as MainActivity).isTablet) {
                mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_150
                )
            } else {
                mBottomSheetTrackingBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_110
                )
            }
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        mBindingTracking?.apply {
            clPersistentBottomSheet.show()
            clEnableTracking.show()
        }
    }

    fun initTrackingView(
        fragmentActivity: FragmentActivity?,
        bottomSheetGeofenceList: BottomSheetTrackingBinding
    ) {
        this.mFragmentActivity = fragmentActivity
        this.mBindingTracking = bottomSheetGeofenceList
        initTrackingBottomSheet()
    }

    private fun initTrackingBottomSheet() {
        mBindingTracking?.apply {
            mBottomSheetTrackingBehavior = BottomSheetBehavior.from(root)
            mBottomSheetTrackingBehavior?.isHideable = true
            mBottomSheetTrackingBehavior?.isDraggable = true
            mBottomSheetTrackingBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetTrackingBehavior?.isFitToContents = false
            mBottomSheetTrackingBehavior?.halfExpandedRatio = 0.58f
            btnTryTracker.setOnClickListener {
                openSimulationWelcome()
            }
            if ((activity as MainActivity).isTablet) {
                val languageCode = getLanguageCode()
                val isRtl =
                    languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW || languageCode == LANGUAGE_CODE_HEBREW_1
                if (isRtl) {
                    clPersistentBottomSheet.layoutDirection = View.LAYOUT_DIRECTION_RTL
                }
            }
            mBottomSheetTrackingBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                if (!clEnableTracking.isVisible) {
                                    imgAmazonLogoTrackingSheet?.alpha = 1f
                                    ivAmazonInfoTrackingSheet?.alpha = 1f
                                }
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoTrackingSheet?.alpha = 0f
                                ivAmazonInfoTrackingSheet?.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoTrackingSheet?.alpha = 1f
                                ivAmazonInfoTrackingSheet?.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {}
                            BottomSheetBehavior.STATE_SETTLING -> {}
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
        }
    }

    private fun openSimulationWelcome() {
        val simulationBottomSheetFragment = SimulationBottomSheetFragment()
        (activity as MainActivity).supportFragmentManager.let {
            simulationBottomSheetFragment.show(
                it,
                WelcomeBottomSheetFragment::javaClass.name
            )
        }
    }

    fun hideTrackingBottomSheet() {
        mBottomSheetTrackingBehavior.let {
            it?.isHideable = true
            it?.state = BottomSheetBehavior.STATE_HIDDEN
            it?.isFitToContents = false
        }
    }
}
