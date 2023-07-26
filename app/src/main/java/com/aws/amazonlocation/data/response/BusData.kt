package com.aws.amazonlocation.data.response

import android.animation.ValueAnimator
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

data class BusData(
    var currentPosition: LatLng? = null,
    var geoJsonSource: GeoJsonSource? = null,
    var animator: ValueAnimator? = null
)
