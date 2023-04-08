package com.aws.amazonlocation.ui.main.map_style

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.utils.MapNames
import com.aws.amazonlocation.utils.MapNames.HERE_CONTRAST
import com.aws.amazonlocation.utils.MapNames.HERE_EXPLORE
import com.aws.amazonlocation.utils.MapNames.HERE_EXPLORE_TRUCK
import com.aws.amazonlocation.utils.MapNames.HERE_HYBRID
import com.aws.amazonlocation.utils.MapNames.HERE_IMAGERY
import com.aws.amazonlocation.utils.MapStyles
import com.aws.amazonlocation.utils.MapStyles.HYBRID_HERE_EXPLORE_SATELLITE
import com.aws.amazonlocation.utils.MapStyles.RASTER_HERE_EXPLORE_SATELLITE
import com.aws.amazonlocation.utils.MapStyles.VECTOR_HERE_CONTRAST
import com.aws.amazonlocation.utils.MapStyles.VECTOR_HERE_EXPLORE
import com.aws.amazonlocation.utils.MapStyles.VECTOR_HERE_EXPLORE_TRUCK
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapStyleViewModel @Inject constructor() : ViewModel() {

    val esriList = arrayListOf<MapStyleInnerData>()
    val hereList = arrayListOf<MapStyleInnerData>()

    fun setEsriMapListData(context: Context) {
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_light),
                isSelected = false,
                image = R.drawable.light,
                mMapName = MapNames.ESRI_LIGHT,
                mMapStyleName = MapStyles.VECTOR_ESRI_TOPOGRAPHIC,
            ),
        )
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_streets),
                isSelected = false,
                image = R.drawable.streets,
                mMapName = MapNames.ESRI_STREET_MAP,
                mMapStyleName = MapStyles.VECTOR_ESRI_STREETS,
            ),
        )
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_navigation),
                isSelected = false,
                image = R.drawable.navigation,
                mMapName = MapNames.ESRI_NAVIGATION,
                mMapStyleName = MapStyles.VECTOR_ESRI_NAVIGATION,
            ),
        )
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_dark_gray),
                isSelected = false,
                image = R.drawable.dark_gray,
                mMapName = MapNames.ESRI_DARK_GRAY_CANVAS,
                mMapStyleName = MapStyles.VECTOR_ESRI_DARK_GRAY_CANVAS,
            ),
        )
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_light_gray),
                isSelected = false,
                image = R.drawable.light_gray,
                mMapName = MapNames.ESRI_LIGHT_GRAY_CANVAS,
                mMapStyleName = MapStyles.VECTOR_ESRI_LIGHT_GRAY_CANVAS,
            ),
        )
        esriList.add(
            MapStyleInnerData(
                mapName = context.getString(R.string.map_imagery),
                isSelected = false,
                image = R.drawable.imagery,
                mMapName = MapNames.ESRI_IMAGERY,
                mMapStyleName = MapStyles.RASTER_ESRI_IMAGERY,
            ),
        )
    }

    fun setHereMapListData(context: Context) {
        hereList.clear()
        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_contrast),
                image = R.mipmap.ic_here_contrast,
                isSelected = false,
                mMapName = HERE_CONTRAST,
                mMapStyleName = VECTOR_HERE_CONTRAST,
            ),
        )
        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_explore),
                image = R.mipmap.ic_here_explore,
                isSelected = false,
                mMapName = HERE_EXPLORE,
                mMapStyleName = VECTOR_HERE_EXPLORE,
            ),
        )

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_explore_truck),
                image = R.mipmap.ic_here_explore_truck,
                isSelected = false,
                mMapName = HERE_EXPLORE_TRUCK,
                mMapStyleName = VECTOR_HERE_EXPLORE_TRUCK,
            ),
        )

        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_raster),
                image = R.mipmap.ic_here_imagery,
                isSelected = false,
                mMapName = HERE_IMAGERY,
                mMapStyleName = RASTER_HERE_EXPLORE_SATELLITE,
            ),
        )
        hereList.add(
            MapStyleInnerData(
                mapName = context.resources.getString(R.string.map_hybrid),
                image = R.mipmap.ic_here_hybrid,
                isSelected = false,
                mMapName = HERE_HYBRID,
                mMapStyleName = HYBRID_HERE_EXPLORE_SATELLITE
            )
        )
    }
}
