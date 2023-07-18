package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.databinding.ItemSortingBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SortingAdapter(
    private val mMapStyleData: ArrayList<FilterOption>,
    var mapInterface: MapInterface
) :
    RecyclerView.Adapter<SortingAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemSortingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FilterOption) {
            binding.apply {
                tvSortingName.text = data.name
                cbSorting.isChecked = data.isSelected
                cbSorting.setOnCheckedChangeListener { _, isChecked ->
                    mapInterface.mapClick(position = adapterPosition, isChecked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemSortingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mMapStyleData[position])
    }

    override fun getItemCount() = mMapStyleData.size

    interface MapInterface {
        fun mapClick(position: Int, isSelected: Boolean)
    }
}
