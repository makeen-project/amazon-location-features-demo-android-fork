package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.databinding.ItemMapStyleBinding
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MapStyleAdapter(
    private val mMapStyleData: ArrayList<MapStyleData>,
    var mapInterface: MapInterface
) :
    RecyclerView.Adapter<MapStyleAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemMapStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MapStyleData) {
            binding.apply {
                tvStyleName.text = data.styleNameDisplay
                if (data.isSelected) {
                    if (!data.mapInnerData.isNullOrEmpty()) {
                        viewDivider.show()
                        rvMapName.show()
                    }
                    ivChecked.setImageDrawable(
                        ContextCompat.getDrawable(
                            ivChecked.context,
                            R.drawable.ic_check_circle
                        )
                    )
                } else {
                    viewDivider.hide()
                    rvMapName.hide()
                    ivChecked.setImageDrawable(
                        ContextCompat.getDrawable(
                            ivChecked.context,
                            R.drawable.ic_radio_button_unchecked
                        )
                    )
                }
                cardMapStyle.setOnClickListener {
                    mapInterface.mapClick(position = adapterPosition)
                }
                data.mapInnerData?.let {
                    rvMapName.apply {
                        this.layoutManager = GridLayoutManager(this.context, 3)
                        val mMapStyleAdapter = MapStyleInnerAdapter(
                            it,
                            object : MapStyleInnerAdapter.MapInterface {
                                override fun mapClick(position: Int) {
                                    mapInterface.mapStyleClick(position = adapterPosition, position)
                                }
                            }
                        )
                        this.adapter = mMapStyleAdapter
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemMapStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mMapStyleData[position])
    }

    override fun getItemCount() = mMapStyleData.size

    interface MapInterface {
        fun mapClick(position: Int)
        fun mapStyleClick(position: Int, innerPosition: Int)
    }
}
