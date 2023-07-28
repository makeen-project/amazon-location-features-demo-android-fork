package com.aws.amazonlocation.utils

import android.content.Context
import android.util.Log
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import java.io.IOException

object GeoJsonReader {

    private const val TAG = "GeoJsonReader"

    fun readGeoJsonFile(context: Context, fileName: String): List<Geometry> {
        val geometries = mutableListOf<Geometry>()

        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)

            val featureCollection = FeatureCollection.fromJson(json)
            val features = featureCollection.features()
            if (features != null) {
                for (feature in features) {
                    val geometry = feature.geometry()
                    if (geometry != null) {
                        geometries.add(geometry)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading GeoJSON file: ${e.message}")
        }

        return geometries
    }
}
