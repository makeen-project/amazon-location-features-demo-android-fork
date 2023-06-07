package com.aws.amazonlocation.ui.main.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SearchSuggestionData
import com.aws.amazonlocation.databinding.ItemSearchPlacesBinding
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.Units
import com.aws.amazonlocation.utils.getRegion
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SearchPlacesAdapter(
    private val mSearchPlaceList: ArrayList<SearchSuggestionData>,
    private val preferenceManager: PreferenceManager?,
    var mSearchPlaceInterface: SearchPlaceInterface,
) :
    RecyclerView.Adapter<SearchPlacesAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: ItemSearchPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: SearchSuggestionData) {
            binding.apply {
                ivSearchLocation.setImageResource(R.drawable.ic_map_pin)

                if (data.isPlaceIndexForPosition) {
                    tvPlaceName.text = data.text
                    groupDistance.hide()
                    tvRegion.hide()
                } else {
                    tvPlaceName.text = data.amazonLocationPlace?.label?.split(",")?.toTypedArray()?.get(0) ?: data.amazonLocationPlace?.label
                }

                if (data.distance != null && preferenceManager != null) {
                    data.distance?.let {
                        tvDistance.text = Units.getMetrics(
                            it
                        )
                    }
                    groupDistance.show()
                } else {
                    groupDistance.hide()
                }

                tvRegion.text =
                    getRegion(data.amazonLocationPlace?.region, data.amazonLocationPlace?.subRegion, data.amazonLocationPlace?.country)

                binding.clMain.setOnClickListener {
                    mSearchPlaceInterface.placeClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            ItemSearchPlacesBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(mSearchPlaceList[position])
    }

    override fun getItemCount() = mSearchPlaceList.size

    interface SearchPlaceInterface {
        fun placeClick(position: Int)
    }
}
