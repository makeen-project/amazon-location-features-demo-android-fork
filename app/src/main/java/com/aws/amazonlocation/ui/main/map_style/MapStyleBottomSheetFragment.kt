package com.aws.amazonlocation.ui.main.map_style

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.BottomSheetMapStyleBinding
import com.aws.amazonlocation.ui.main.explore.ExploreViewModel
import com.aws.amazonlocation.ui.main.explore.MapStyleAdapter
import com.aws.amazonlocation.ui.main.explore.SortingAdapter
import com.aws.amazonlocation.utils.CLICK_DEBOUNCE
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.hideSoftKeyboard
import com.aws.amazonlocation.utils.hideViews
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.showViews
import com.aws.amazonlocation.utils.textChanges
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
class MapStyleBottomSheetFragment(
    private val mViewModel: ExploreViewModel,
    private val mapInterface: MapStyleAdapter.MapInterface
) : BottomSheetDialogFragment() {

    private lateinit var mBinding: BottomSheetMapStyleBinding
    private var mMapStyleAdapter: MapStyleAdapter? = null
    private var mProviderAdapter: SortingAdapter? = null
    private var mAttributeAdapter: SortingAdapter? = null
    private var mTypeAdapter: SortingAdapter? = null

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

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        mBinding.apply {
            rvMapStyle.apply {
                mViewModel.setMapListData(context, isGrabMapEnable(mPreferenceManager))
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
                layoutNoDataFound.tvNoMatchingFound.text = getString(R.string.label_style_search_error_title)
                layoutNoDataFound.tvMakeSureSpelledCorrect.text = getString(R.string.label_style_search_error_des)
                setMapTileSelection(mapName)
                this.layoutManager = LinearLayoutManager(requireContext())
                mMapStyleAdapter =
                    MapStyleAdapter(
                        mViewModel.mStyleList,
                        object : MapStyleAdapter.MapInterface {
                            override fun mapStyleClick(position: Int, innerPosition: Int) {
                                if (position != -1 && innerPosition != -1) {
                                    mapInterface.mapStyleClick(position, innerPosition)
                                }
                            }
                        }
                    )
                this.adapter = mMapStyleAdapter
            }
            rvProvider.layoutManager = LinearLayoutManager(requireContext())
            mProviderAdapter =
                SortingAdapter(
                    mViewModel.providerOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.providerOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvProvider.adapter = mProviderAdapter

            rvAttribute.layoutManager = LinearLayoutManager(requireContext())
            mAttributeAdapter =
                SortingAdapter(
                    mViewModel.attributeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.attributeOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvAttribute.adapter = mAttributeAdapter

            rvType.layoutManager = LinearLayoutManager(requireContext())
            mTypeAdapter =
                SortingAdapter(
                    mViewModel.typeOptions,
                    object : SortingAdapter.MapInterface {
                        override fun mapClick(position: Int, isSelected: Boolean) {
                            if (position != -1) {
                                mViewModel.typeOptions[position].isSelected = isSelected
                            }
                        }
                    }
                )
            rvType.adapter = mTypeAdapter
            ivMapStyleClose.setOnClickListener {
                mapStyleShowList()
                dismiss()
            }

            etSearchMap.textChanges().debounce(CLICK_DEBOUNCE).onEach { text ->
                mapStyleShowList()
                if (!text.isNullOrEmpty()) {
                    tilSearch.isEndIconVisible = true
                    val filterList = mViewModel.filterAndSortItems(
                        requireContext(),
                        text.toString(),
                        null,
                        null,
                        null
                    )
                    if (filterList.isNotEmpty()) {
                        mViewModel.mStyleList.clear()
                        mViewModel.mStyleList.addAll(filterList)
                        activity?.runOnUiThread {
                            mMapStyleAdapter?.notifyDataSetChanged()
                        }
                        rvMapStyle.show()
                        layoutNoDataFound.root.hide()
                    } else {
                        layoutNoDataFound.root.show()
                        rvMapStyle.hide()
                    }
                }
            }.launchIn(lifecycleScope)
            val params = cardSearchFilter.layoutParams
            tilSearch.setEndIconOnClickListener {
                setDefaultMapStyleList()
                etSearchMap.setText("")
                tilSearch.clearFocus()
                etSearchMap.clearFocus()
                params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
                cardSearchFilter.layoutParams = params
                tilSearch.hide()
                viewLine.show()
                scrollMapStyle.show()
                rvMapStyle.show()
                layoutNoDataFound.root.hide()
                activity?.hideSoftKeyboard(etSearchMap)
            }
            tilSearch.isEndIconVisible = false
            ivSearch.setOnClickListener {
                viewLine.hide()
                tilSearch.show()
                params?.width = ViewGroup.LayoutParams.MATCH_PARENT
                cardSearchFilter.layoutParams = params
                etSearchMap.clearFocus()
                scrollMapStyle.hide()
                tilSearch.isEndIconVisible = true
            }

            tvClearSelection.setOnClickListener {
                mViewModel.providerOptions.forEachIndexed { index, _ ->
                    mViewModel.providerOptions[index].isSelected = false
                }
                mViewModel.attributeOptions.forEachIndexed { index, _ ->
                    mViewModel.attributeOptions[index].isSelected = false
                }
                mViewModel.typeOptions.forEachIndexed { index, _ ->
                    mViewModel.typeOptions[index].isSelected = false
                }
                mTypeAdapter?.notifyDataSetChanged()
                mAttributeAdapter?.notifyDataSetChanged()
                mProviderAdapter?.notifyDataSetChanged()
                imgFilterSelected.hide()
                imgFilter.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_img_tint
                    )
                )
                setDefaultMapStyleList()
                mapStyleShowList()
            }
            btnApplyFilter.setOnClickListener {
                val providerNames = arrayListOf<String>()
                val attributeNames = arrayListOf<String>()
                val typeNames = arrayListOf<String>()
                mViewModel.providerOptions.filter { it.isSelected }
                    .forEach { data -> providerNames.add(data.name) }
                mViewModel.attributeOptions.filter { it.isSelected }
                    .forEach { data -> attributeNames.add(data.name) }
                mViewModel.typeOptions.filter { it.isSelected }
                    .forEach { data -> typeNames.add(data.name) }
                val filterList = mViewModel.filterAndSortItems(
                    requireContext(),
                    null,
                    providerNames.ifEmpty { null },
                    attributeNames.ifEmpty { null },
                    typeNames.ifEmpty { null }
                )
                if (providerNames.isNotEmpty() || attributeNames.isNotEmpty() || typeNames.isNotEmpty()) {
                    imgFilterSelected.show()
                    imgFilter.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_primary_green
                        )
                    )
                } else {
                    imgFilterSelected.hide()
                    imgFilter.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_img_tint
                        )
                    )
                }
                if (filterList.isNotEmpty()) {
                    mViewModel.mStyleList.clear()
                    mViewModel.mStyleList.addAll(filterList)
                    activity?.runOnUiThread {
                        mMapStyleAdapter?.notifyDataSetChanged()
                    }
                    mapStyleShowList()
                }
            }
            imgFilter.setOnClickListener {
                if (nsvFilter.isVisible) {
                    mapStyleShowList()
                } else {
                    mapStyleShowSorting()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun BottomSheetMapStyleBinding.setDefaultMapStyleList() {
        mViewModel.mStyleList.clear()
        mViewModel.mStyleList.addAll(mViewModel.mStyleListForFilter)
        activity?.runOnUiThread {
            etSearchMap.setText("")
            mMapStyleAdapter?.notifyDataSetChanged()
        }
    }
    private fun BottomSheetMapStyleBinding.mapStyleShowSorting() {
        showViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter
        )
        rvMapStyle.hide()
    }

    private fun BottomSheetMapStyleBinding.mapStyleShowList() {
        hideViews(
            nsvFilter,
            viewDivider,
            tvClearSelection,
            btnApplyFilter
        )
        rvMapStyle.show()
    }

    fun refreshMapTile(mapName: String){
        mBinding.apply {
            setMapTileSelection(mapName)
        }
    }
    private fun BottomSheetMapStyleBinding.setMapTileSelection(
        mapName: String
    ) {
        when (mapName) {
            resources.getString(R.string.esri) -> {
                cardEsri.strokeColor =
                    ContextCompat.getColor(requireContext(), R.color.color_primary_green)
                cardHere.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
                cardGrabMap.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
            }
            resources.getString(R.string.here) -> {
                cardHere.strokeColor =
                    ContextCompat.getColor(requireContext(), R.color.color_primary_green)
                cardEsri.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
                cardGrabMap.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
            }
            resources.getString(R.string.grab) -> {
                cardGrabMap.strokeColor =
                    ContextCompat.getColor(requireContext(), R.color.color_primary_green)
                cardEsri.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
                cardHere.strokeColor = ContextCompat.getColor(requireContext(), R.color.white)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun notifyAdapter() {
        mMapStyleAdapter?.notifyDataSetChanged()
    }
}
