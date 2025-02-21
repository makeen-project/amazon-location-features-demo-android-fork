package com.aws.amazonlocation.ui.main.simulation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SimulationHistoryData
import com.aws.amazonlocation.databinding.RvSimulationItemBinding
import com.aws.amazonlocation.utils.DateFormat
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationTrackingListAdapter(
    private val simulationHistoryData: ArrayList<SimulationHistoryData>
) :
    RecyclerView.Adapter<SimulationTrackingListAdapter.SimulationVH>() {

    inner class SimulationVH(private val binding: RvSimulationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimulationHistoryData) {
            binding.apply {
                item.devicePositionData?.let {
                    tvLatLng.text = String.format("%7f, %7f", it.latitude, it.longitude)
                    val date = convertToCustomFormat(it.receivedTime)
                    tvTime.text = date
                }
                if (item.isBusStopData) {
                    tvBusStop.show()
                    tvBusStop.text = String.format("Bus stop number %2d", item.busStopCount)
                    tvLatLng.setTextColor(
                        ContextCompat.getColor(
                            tvLatLng.context,
                            R.color.color_hint_text
                        )
                    )
                    val padding = tvLatLng.context.resources.getDimensionPixelSize(R.dimen.dp_4)
                    clNavigationItem.setPadding(
                        clNavigationItem.paddingLeft,
                        padding,
                        clNavigationItem.paddingRight,
                        padding
                    )
                } else {
                    tvBusStop.hide()
                    tvLatLng.setTextColor(
                        ContextCompat.getColor(
                            tvLatLng.context,
                            R.color.color_medium_black
                        )
                    )
                    val padding = tvLatLng.context.resources.getDimensionPixelSize(R.dimen.dp_14)
                    clNavigationItem.setPadding(
                        clNavigationItem.paddingLeft,
                        padding,
                        clNavigationItem.paddingRight,
                        padding
                    )
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimulationVH {
        return SimulationVH(
            RvSimulationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SimulationVH, position: Int) {
        holder.bind(simulationHistoryData[position])
    }

    override fun getItemCount() = simulationHistoryData.size
}
