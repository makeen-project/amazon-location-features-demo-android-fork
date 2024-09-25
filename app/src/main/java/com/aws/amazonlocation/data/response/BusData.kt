package com.aws.amazonlocation.data.response

import android.animation.ValueAnimator
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.style.sources.GeoJsonSource

data class BusData(
    var currentPosition: LatLng? = null,
    var geoJsonSource: GeoJsonSource? = null,
    var animator: ValueAnimator? = null
)
