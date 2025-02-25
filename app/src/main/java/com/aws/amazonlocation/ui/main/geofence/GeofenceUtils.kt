package com.aws.amazonlocation.ui.main.geofence

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetGeofenceListBinding
import com.aws.amazonlocation.domain.`interface`.GeofenceInterface
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.simulation.SimulationBottomSheetFragment
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.show
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@SuppressLint("NotifyDataSetChanged")
class GeofenceUtils {

    var isChangeDataProviderClicked: Boolean = false
    private var mBottomSheetGeofenceListBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private var mBindingGeofenceList: BottomSheetGeofenceListBinding? = null
    private var mFragmentActivity: FragmentActivity? = null
    private var mActivity: Activity? = null
    private var mGeofenceInterface: GeofenceInterface? = null
    private var isTablet = false
    private var preferenceManager: PreferenceManager? = null

    fun setMapBox(
        activity: Activity,
        prefrenceManager: PreferenceManager
    ) {
        this.mActivity = activity
        this.preferenceManager = prefrenceManager
    }

    fun isGeofenceListExpandedOrHalfExpand(): Boolean {
        return mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_EXPANDED || mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun collapseGeofenceList() {
        mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun initGeofenceView(
        fragmentActivity: FragmentActivity?,
        bottomSheetGeofenceList: BottomSheetGeofenceListBinding,
        mGeofenceInterface: GeofenceInterface
    ) {
        this.mGeofenceInterface = mGeofenceInterface
        this.mFragmentActivity = fragmentActivity
        this.mBindingGeofenceList = bottomSheetGeofenceList
        initGeofenceListBottomSheet()
    }

    fun emptyGeofenceBottomSheetAddBtn() {
        mBindingGeofenceList?.clEmptyGeofenceList?.show()
    }

    private fun initGeofenceListBottomSheet() {
        mBindingGeofenceList?.apply {
            mBottomSheetGeofenceListBehavior = BottomSheetBehavior.from(root)
            mBottomSheetGeofenceListBehavior?.isHideable = true
            mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            mBottomSheetGeofenceListBehavior?.isFitToContents = false
            if (isTablet) {
                mBottomSheetGeofenceListBehavior?.halfExpandedRatio = 0.58f
            } else {
                mBottomSheetGeofenceListBehavior?.halfExpandedRatio = 0.5f
            }
            btnTryGeofence.setOnClickListener {
                openSimulationWelcome()
            }

            mBottomSheetGeofenceListBehavior?.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                imgAmazonLogoGeofenceList?.alpha = 1f
                                ivAmazonInfoGeofenceList?.alpha = 1f
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                imgAmazonLogoGeofenceList?.alpha = 0f
                                ivAmazonInfoGeofenceList?.alpha = 0f
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                imgAmazonLogoGeofenceList?.alpha = 1f
                                ivAmazonInfoGeofenceList?.alpha = 1f
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

    fun hideAllGeofenceBottomSheet() {
        hideGeofenceListBottomSheet()
    }

    fun showGeofenceBeforeLogin() {
        mBindingGeofenceList?.clEmptyGeofenceList?.show()
        mBottomSheetGeofenceListBehavior?.isHideable = false
        mBottomSheetGeofenceListBehavior?.isDraggable = true
        mBottomSheetGeofenceListBehavior?.isFitToContents = false
        mBindingGeofenceList?.clEmptyGeofenceList?.post {


            mBottomSheetGeofenceListBehavior?.halfExpandedRatio = 0.55f
        }
        mBindingGeofenceList?.clEmptyGeofenceList?.context?.let {
            if ((mActivity as MainActivity).isTablet) {
                mBottomSheetGeofenceListBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_150
                )
            } else {
                mBottomSheetGeofenceListBehavior?.peekHeight = it.resources.getDimensionPixelSize(
                    R.dimen.dp_110
                )
            }
            mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun hideGeofenceListBottomSheet() {
        mBottomSheetGeofenceListBehavior?.isHideable = true
        mBottomSheetGeofenceListBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun openSimulationWelcome() {
        val simulationBottomSheetFragment = SimulationBottomSheetFragment()
        (mActivity as MainActivity).supportFragmentManager.let {
            simulationBottomSheetFragment.show(it, WelcomeBottomSheetFragment::javaClass.name)
        }
    }

    fun isGeofenceSheetCollapsed(): Boolean {
        return mBottomSheetGeofenceListBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
    }
}
