package com.aws.amazonlocation.ui.main.simulation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.TrackingHistoryData
import com.aws.amazonlocation.databinding.RvTrackingItemBinding
import com.aws.amazonlocation.utils.DateFormat
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationTrackingListAdapter(
    private val notificationData: ArrayList<TrackingHistoryData>,
) :
    RecyclerView.Adapter<SimulationTrackingListAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: RvTrackingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackingHistoryData) {
            binding.apply {
                item.devicePositionData?.let {
                    tvLatLng.text = String.format("%7f , %7f", it.position[1], it.position[0])
                    val date = convertToCustomFormat(it.receivedTime)
                    tvTime.text = date
                }
                when (item.headerData) {
                    viewTopDotted.context.getString(R.string.label_position_start) -> {
                        viewTopDotted.hide()
                        viewBottomDotted.show()
                    }
                    viewTopDotted.context.getString(R.string.label_position_end) -> {
                        viewTopDotted.show()
                        viewBottomDotted.hide()
                    }
                    viewTopDotted.context.getString(R.string.label_position_data) -> {
                        viewTopDotted.show()
                        viewBottomDotted.show()
                    }
                    else -> {
                        viewTopDotted.hide()
                        viewBottomDotted.hide()
                    }
                }
            }
        }

        private fun convertToCustomFormat(receivedTime: Date): String? {
            val destFormat = SimpleDateFormat(DateFormat.HH_MM_AA, Locale.getDefault())
            return receivedTime.let { destFormat.format(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            RvTrackingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(notificationData[position])
    }

    override fun getItemCount() = notificationData.size
}
