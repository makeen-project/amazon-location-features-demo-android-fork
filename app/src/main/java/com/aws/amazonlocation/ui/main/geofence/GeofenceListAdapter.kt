package com.aws.amazonlocation.ui.main.geofence

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.geo.model.ListGeofenceResponseEntry
import com.aws.amazonlocation.databinding.ItemGeofenceListBinding
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.checkGeofenceInsideGrab
import com.aws.amazonlocation.utils.geofence_helper.GeofenceHelper
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.show
import com.mapbox.mapboxsdk.geometry.LatLng

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class GeofenceListAdapter(
    private var mPreferenceManager: PreferenceManager?,
    private var context: Context?,
    private var mGeofenceList: ArrayList<ListGeofenceResponseEntry>,
    private var mGeofenceDeleteInterface: GeofenceDeleteInterface
) : RecyclerView.Adapter<GeofenceListAdapter.GeofenceVH>() {
    private var mGeofenceHelper: GeofenceHelper? = null

    inner class GeofenceVH(private var binding: ItemGeofenceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: ListGeofenceResponseEntry,
            mGeofenceDeleteInterface: GeofenceDeleteInterface
        ) {
            binding.apply {
                mGeofenceHelper?.let {
                    if (checkGeofenceInsideGrab(
                            LatLng(
                                    data.geometry.circle.center[1],
                                    data.geometry.circle.center[0]
                                ),
                            mPreferenceManager,
                            context
                        )
                    ) {
                        clMainGeofence.alpha = 1.0F
                        ivGeofenceIcon.show()
                        ivGeofenceIconDisable.hide()
                    } else {
                        clMainGeofence.alpha = 0.3F
                        ivGeofenceIconDisable.show()
                        ivGeofenceIcon.invisible()
                    }
                }
                tvGeofenceAddressType.text = data.geofenceId
                tvGeofenceMessage.text = data.status
                ivDeleteGeofence.setOnClickListener {
                    mGeofenceHelper?.let {
                        if (checkGeofenceInsideGrab(
                                LatLng(
                                        data.geometry.circle.center[1],
                                        data.geometry.circle.center[0]
                                    ),
                                mPreferenceManager,
                                context
                            )
                        ) {
                            mGeofenceDeleteInterface.deleteGeofence(adapterPosition, data)
                        } else {
                            mGeofenceDeleteInterface.disableGeofenceClick()
                        }
                    }
                }
                root.setOnClickListener {
                    mGeofenceHelper?.let {
                        if (checkGeofenceInsideGrab(
                                LatLng(
                                        data.geometry.circle.center[1],
                                        data.geometry.circle.center[0]
                                    ),
                                mPreferenceManager,
                                context
                            )
                        ) {
                            mGeofenceDeleteInterface.editGeofence(adapterPosition, data)
                        } else {
                            mGeofenceDeleteInterface.disableGeofenceClick()
                        }
                    }
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

    fun setGeofenceHelper(geofenceHelper: GeofenceHelper?) {
        mGeofenceHelper = geofenceHelper
    }

    interface GeofenceDeleteInterface {
        fun deleteGeofence(position: Int, data: ListGeofenceResponseEntry)
        fun editGeofence(position: Int, data: ListGeofenceResponseEntry)
        fun disableGeofenceClick()
    }
}
