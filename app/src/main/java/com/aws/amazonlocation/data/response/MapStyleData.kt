package com.aws.amazonlocation.data.response // ktlint-disable filename

data class MapStyleData(
    var styleNameDisplay: String? = null,
    var isSelected: Boolean = false,
    var mapInnerData: ArrayList<MapStyleInnerData>? = null
)
