package com.aws.amazonlocation.ui.main.map_style

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import com.aws.amazonlocation.ui.main.explore.MapStyleAdapter
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class MapStyleBottomSheetFragment(
    private val mViewModel: ExploreViewModel,
    private val mapInterface: MapStyleAdapter.MapInterface
) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetMapStyleBinding
    private var mMapStyleAdapter: MapStyleAdapter? = null

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

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
        mBinding = BottomSheetMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        mBinding.apply {
            rvMapStyle.apply {
                mViewModel.setMapListData(context)
                val mapName =
                    mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
                        ?: getString(R.string.map_esri)
                val mapStyleName =
                    mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
                        ?: getString(R.string.map_light)
                mViewModel.mStyleList.forEach {
                    if (it.styleNameDisplay.equals(mapName)) {
                        it.isSelected = true
                        it.mapInnerData?.forEach { mapStyleInnerData ->
                            if (mapStyleInnerData.mapName.equals(mapStyleName)) {
                                mapStyleInnerData.isSelected = true
                            }
                        }
                    } else {
                        it.isSelected = false
                    }
                }
                this.layoutManager = LinearLayoutManager(requireContext())
                mMapStyleAdapter =
                    MapStyleAdapter(
                        mViewModel.mStyleList,
                        object : MapStyleAdapter.MapInterface {
                            override fun mapClick(position: Int) {
                                if (position != -1) {
                                    mapInterface.mapClick(position)
                                }
                            }

                            override fun mapStyleClick(position: Int, innerPosition: Int) {
                                if (position != -1 && innerPosition != -1) {
                                    mapInterface.mapStyleClick(position, innerPosition)
                                }
                            }
                        }
                    )
                this.adapter = mMapStyleAdapter
            }
            cardMapStyleClose.setOnClickListener {
                dismiss()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyAdapter() {
        mMapStyleAdapter?.notifyDataSetChanged()
    }
}
