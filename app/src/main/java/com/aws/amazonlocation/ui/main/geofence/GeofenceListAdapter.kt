package com.aws.amazonlocation.ui.main.geofence

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.databinding.ItemGeofenceListBinding

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceListAdapter(
    private var mGeofenceList: ArrayList<ListGeofenceResponseEntry>,
    private var mGeofenceDeleteInterface: GeofenceDeleteInterface
) :
    RecyclerView.Adapter<GeofenceListAdapter.GeofenceVH>() {

    class GeofenceVH(private var binding: ItemGeofenceListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: ListGeofenceResponseEntry,
            mGeofenceDeleteInterface: GeofenceDeleteInterface
        ) {
            binding.apply {
                tvGeofenceAddressType.text = data.geofenceId
                tvGeofenceMessage.text = data.status
                ivDeleteGeofence.setOnClickListener {
                    mGeofenceDeleteInterface.deleteGeofence(adapterPosition, data)
                }
                root.setOnClickListener {
                    mGeofenceDeleteInterface.editGeofence(adapterPosition, data)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofenceVH {
        return GeofenceVH(
            ItemGeofenceListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GeofenceVH, position: Int) {
        holder.bind(mGeofenceList[position], mGeofenceDeleteInterface)
    }

    override fun getItemCount(): Int {
        return mGeofenceList.size
    }

    interface GeofenceDeleteInterface {
        fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry)
        fun editGeofence(position: Int, data: ListGeofenceResponseEntry)
    }
}