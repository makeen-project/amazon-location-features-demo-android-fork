package com.aws.amazonlocation.data.response

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class NavigationResponse(

    var duration: String? = null,
    var distance: Double? = null,
    var startLat: Double? = null,
    var startLng: Double? = null,
    var endLat: Double? = null,
    var endLng: Double? = null,
    var destinationAddress: String? = null,
    var navigationList: ArrayList<NavigationData> = ArrayList()
)

class NavigationData(
    var duration: Double? = null,
    var distance: Double? = null,
    var startLat: Double? = null,
    var startLng: Double? = null,
    var endLat: Double? = null,
    var endLng: Double? = null,
    var destinationAddress: String? = null,
    var region: String? = null,
    var subRegion: String? = null,
    var country: String? = null
) {
    fun getRegions(): String {
        val address = destinationAddress?.split(",")?.toTypedArray()?.get(0)
            ?: destinationAddress
        var mRegion = ""
        mRegion += if (!subRegion.isNullOrEmpty() && !region.isNullOrEmpty()) {
            "$address, $subRegion, $region"
        } else if (!region.isNullOrEmpty() && !country.isNullOrEmpty()) {
            "$address, $region, $country"
        } else if (!country.isNullOrEmpty()) {
            country
        } else {
            ""
        }
        return mRegion
    }
}
