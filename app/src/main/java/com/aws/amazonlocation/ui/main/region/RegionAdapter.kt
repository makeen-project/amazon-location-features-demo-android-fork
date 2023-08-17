package com.aws.amazonlocation.ui.main.region

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.RegionResponse
import com.aws.amazonlocation.databinding.ItemRegionBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class RegionAdapter(
    private val mRegionList: ArrayList<RegionResponse>,
    private val isRtl: Boolean,
    var mSearchPlaceInterface: RegionInterface
) :
    RecyclerView.Adapter<RegionAdapter.RegionVH>() {

    inner class RegionVH(private val binding: ItemRegionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RegionResponse) {
            binding.apply {
                rbRegion.text = data.name
                rbRegion.isChecked = data.isSelected
                rbRegion.isSelected = data.isSelected
                if (data.isSelected) {
                    setRadioButtonIcon(rbRegion, R.drawable.icon_checkmark)
                } else {
                    setRadioButtonIcon(rbRegion, R.drawable.ic_radio_button_unchecked)
                }
                rbRegion.setOnClickListener {
                    if (!data.isSelected) {
                        mSearchPlaceInterface.click(adapterPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionAdapter.RegionVH {
        return RegionVH(
            ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RegionVH, position: Int) {
        holder.bind(mRegionList[position])
    }

    override fun getItemCount() = mRegionList.size

    interface RegionInterface {
        fun click(position: Int)
    }
    private fun setRadioButtonIcon(
        rb: AppCompatRadioButton,
        iconCheckmark: Int
    ) {
        rb.isChecked = true
        if (isRtl) {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                iconCheckmark,
                0,
                0,
                0
            )
        } else {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                iconCheckmark,
                0
            )
        }
    }
}
