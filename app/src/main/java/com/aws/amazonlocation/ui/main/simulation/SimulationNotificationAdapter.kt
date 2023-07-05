package com.aws.amazonlocation.ui.main.simulation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.databinding.RvItemNotificationBinding
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class SimulationNotificationAdapter(
    private val notificationData: ArrayList<NotificationData>,
    var notificationInterface: NotificationInterface
) :
    RecyclerView.Adapter<SimulationNotificationAdapter.SearchPlaceVH>() {

    inner class SearchPlaceVH(private val binding: RvItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationData) {
            binding.apply {
                tvBusName.text = data.name
                switchBusNotification.isChecked = data.isSelected
                if (adapterPosition == notificationData.size - 1) {
                    viewNotification.hide()
                } else {
                    viewNotification.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceVH {
        return SearchPlaceVH(
            RvItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchPlaceVH, position: Int) {
        holder.bind(notificationData[position])
    }

    override fun getItemCount() = notificationData.size

    interface NotificationInterface {
        fun click(position: Int, isSelected: Boolean)
    }
}
