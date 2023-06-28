package com.aws.amazonlocation.utils

import com.aws.amazonlocation.BuildConfig

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

const val KEY_BOARD_HEIGHT = 500
const val KEY_USER_DETAILS = "user_details"
const val KEY_LOCATION_PERMISSION = "location_permission"
const val SEARCH_MAX_RESULT = 15
const val SEARCH_MAX_SUGGESTION_RESULT = 5
const val KILOMETERS = "Kilometers"
const val MILES = "Miles"
const val KEY_POOL_ID = "POOL_ID"
const val KEY_USER_POOL_ID = "KEY_USER_POOL_ID"
const val KEY_USER_DOMAIN = "KEY_USER_DOMAIN"
const val KEY_USER_REGION = "KEY_USER_REGION"
const val KEY_USER_POOL_CLIENT_ID = "KEY_USER_POOL_CLIENT_ID"
const val WEB_SOCKET_URL = "web_socket_url"
const val KEY_RE_START_APP = "KEY_RE_START_APP"
const val KEY_RE_START_APP_WITH_AWS_DISCONNECT = "key_restart_app_with_aws_disconnect"
const val KEY_TAB_ENUM = "KEY_TAB_ENUM"
const val KEY_CLOUD_FORMATION_STATUS = "KEY_CLOUD_FORMATION_STATUS"
const val KEY_UNIT_SYSTEM = "KEY_UNIT_SYSTEM"
const val HTTPS = "https://"
const val KEY_URL = "KEY_URL"
const val KEY_MAP_NAME = "key_map_name"
const val KEY_MAP_STYLE_NAME = "key_map_style_name"
const val CLICK_DEBOUNCE = 1000L
const val CLICK_DEBOUNCE_ENABLE = 1200L
const val RESTART_DELAY = 800L
const val DELAY_500 = 500L
const val DELAY_300 = 300L
const val DELAY_1000 = 1000L
const val DELAY_LANGUAGE_3000 = 3000L
const val KEY_AVOID_TOLLS = "Avoid Tolls"
const val KEY_AVOID_FERRIES = "Avoid Ferries"
const val AWS_CLOUD_INFORMATION_FRAGMENT = "AwsCloudInformationFragment"
const val SETTING_FRAGMENT = "SettingFragment"
const val ABOUT_FRAGMENT = "AboutFragment"
const val VERSION_FRAGMENT = "VersionFragment"
const val IS_LOCATION_TRACKING_ENABLE = "is_location_tracking_enable"
const val IS_APP_FIRST_TIME_OPENED = "is_app_first_time_opened"

const val KEY_ID_TOKEN = "key_id_token"
const val KEY_ACCESS_TOKEN = "key_access_token"
const val KEY_REFRESH_TOKEN = "key_refresh_token"
const val KEY_PROVIDER = "key_provider"
const val IOT_POLICY = "AmazonLocationIotPolicy"

const val STRING_FORMAT = "%.2f, %.2f"

const val JSON_KEY_STYLE_SOURCES = "sources"
const val JSON_KEY_STYLE_ESRI = "esri"
const val JSON_KEY_STYLE_HERE = "omv"
const val JSON_KEY_STYLE_RASTER = "raster-tiles"
const val JSON_KEY_STYLE_MINZOOM = "minzoom"
const val JSON_KEY_STYLE_MAXZOOM = "maxzoom"
const val TRAVEL_MODE_BICYCLE = "Bicycle"
const val TRAVEL_MODE_MOTORCYCLE = "Motorcycle"

const val HERE = "Here"
const val DESCRIPTION_TAG_ESRI = "ESRI_"
const val DESCRIPTION_TAG_HERE = "HERE_"

val SE_REGION_LIST = arrayListOf("ap-southeast-1")

const val latNorth = 31.952162238024968
const val lonEast = 146.25
const val latSouth = -21.943045533438166
const val lonWest = 90.0

const val LANGUAGE_CODE_GERMAN = "de"
const val LANGUAGE_CODE_SPANISH = "es"
const val LANGUAGE_CODE_ENGLISH = "en"
const val LANGUAGE_CODE_FRENCH = "fr"
const val LANGUAGE_CODE_ITALIAN = "it"
const val LANGUAGE_CODE_BR_PT = "pt-BR"
const val LANGUAGE_CODE_CH_CN = "zh-CN"
const val LANGUAGE_CODE_CH_TW = "zh-TW"
const val LANGUAGE_CODE_JAPANESE = "ja"
const val LANGUAGE_CODE_KOREAN = "ko"
const val LANGUAGE_CODE_ARABIC = "ar"
const val LANGUAGE_CODE_HEBREW = "iw"
const val LANGUAGE_CODE_HINDI = "hi"

val regionMapList: MutableMap<String, String> = mutableMapOf(
    Pair("US East (Ohio) us-east-2", "us-east-2"),
    Pair("US East (N. Virginia) us-east-1", "us-east-1"),
    Pair("US West (Oregon) us-west-2", "us-west-2"),
    Pair("Asia Pacific (Singapore) ap-southeast-1", "ap-southeast-1"),
    Pair("Asia Pacific (Sydney) ap-southeast-2", "ap-southeast-2"),
    Pair("Asia Pacific (Tokyo) ap-northeast-1", "ap-northeast-1"),
    Pair("Canada (Central) ca-central-1", "ca-central-1"),
    Pair("Europe (Frankfurt) eu-central-1", "eu-central-1"),
    Pair("Europe (Ireland) eu-west-1", "eu-west-1"),
    Pair("Europe (London) eu-west-2", "eu-west-2"),
    Pair("Europe (Stockholm) eu-north-1", "eu-north-1"),
    Pair("South America (São Paulo) sa-east-1", "sa-east-1")
)

/**
 *  * Validate Latitude and Longitude from string.
 *
 * eg 1> "Abcd" > Check LAT_LNG_REG_EXP > return false
 * eg 2> "49.281174, -123.116823" > Check LAT_LNG_REG_EXP > return true
 */
const val LAT_LNG_REG_EXP = "([+-]?\\d+\\.?\\d+)\\s*,\\s*([+-]?\\d+\\.?\\d+)"

const val GEOFENCE_NAME_REG_EXP = "^[-._\\p{L}\\p{N}]+\$"
const val userPoolIdPattern = "[\\w-]+_[0-9a-zA-Z]+"
const val userPoolClientId = "[\\w+]+"

const val LOCATION_AWS_PREFIX = "location.aws.com.demo."
const val LOCATION_MAPS_PREFIX = "maps."
const val LOCATION_TRACKERS_PREFIX = "trackers."
const val LOCATION_GEOFENCE_S_PREFIX = "geofences."
const val LOCATION_ROUTES_PREFIX = "routes."
const val LOCATION_PLACES_PREFIX = "places."

const val ESRI_PLACE_INDEX = LOCATION_AWS_PREFIX + LOCATION_PLACES_PREFIX + "Esri.PlaceIndex"
const val HERE_PLACE_INDEX = LOCATION_AWS_PREFIX + LOCATION_PLACES_PREFIX + "HERE.PlaceIndex"
const val GRAB_PLACE_INDEX = LOCATION_AWS_PREFIX + LOCATION_PLACES_PREFIX + "Grab.PlaceIndex"
const val ESRI_ROUTE_CALCULATOR = LOCATION_AWS_PREFIX + LOCATION_ROUTES_PREFIX + "Esri.RouteCalculator"
const val HERE_ROUTE_CALCULATOR = LOCATION_AWS_PREFIX + LOCATION_ROUTES_PREFIX + "HERE.RouteCalculator"
const val GRAB_ROUTE_CALCULATOR = LOCATION_AWS_PREFIX + LOCATION_ROUTES_PREFIX + "Grab.RouteCalculator"

object Credentials {
    const val CLOUD_FORMATION_REMOVE_URL = BuildConfig.CLOUD_FORMATION_URL
    const val CLOUD_INFORMATION_LEARN_MORE = BuildConfig.BASE_DOMAIN + BuildConfig.CLOUD_FORMATION_READ_MORE_URL
}

object Distance {
    const val DISTANCE_IN_METER_30 = 30
    const val DISTANCE_IN_METER_20 = 20
    const val DISTANCE_IN_METER_10 = 10
}

object MapCameraZoom {
    const val NAVIGATION_CAMERA_ZOOM = 14.0
    const val DEFAULT_CAMERA_ZOOM = 14.0
    const val TRACKING_CAMERA_ZOOM = 14.0
    const val MAX_ZOOM = 22.0
}

object Durations {
    const val DEFAULT_INTERVAL_IN_MILLISECONDS = 500L
    const val CAMERA_DURATION_1500 = 1500
    const val CAMERA_DURATION_1000 = 1000
    const val CAMERA_BOTTOM_PADDING = 800
    const val CAMERA_TOP_RIGHT_LEFT_PADDING = 225
    const val CAMERA_RIGHT_PADDING = 180
    const val DEFAULT_RADIUS = 80
    const val DELAY_FOR_GEOFENCE = 500L
}

object GeofenceCons {
    const val GEOFENCE_COLLECTION = LOCATION_AWS_PREFIX + LOCATION_GEOFENCE_S_PREFIX + "GeofenceCollection"
    const val CIRCLE_CENTER_SOURCE_ID = "CIRCLE_CENTER_SOURCE_ID"
    const val CIRCLE_CENTER_ICON_ID = "CIRCLE_CENTER_ICON_ID"
    const val CIRCLE_CENTER_LAYER_ID = "CIRCLE_CENTER_LAYER_ID"
    const val TURF_CALCULATION_FILL_LAYER_GEO_JSON_SOURCE_ID =
        "TURF_CALCULATION_FILL_LAYER_GEOJSON_SOURCE_ID"
    const val TURF_CALCULATION_FILL_LAYER_ID = "TURF_CALCULATION_FILL_LAYER_ID"

    const val TURF_CALCULATION_LINE_LAYER_GEO_JSON_SOURCE_ID =
        "TURF_CALCULATION_LINE_LAYER_GEOJSON_SOURCE_ID"
    const val TURF_CALCULATION_LINE_LAYER_ID = "TURF_CALCULATION_LINE_LAYER_ID"
    const val RADIUS_SEEKBAR_MAX = 10000
    const val RADIUS_SEEKBAR_DIFFERENCE = 1

    const val CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID = "CIRCLE_DRAGGABLE_VISIBLE_SOURCE_ID"
    const val CIRCLE_DRAGGABLE_VISIBLE_ICON_ID = "CIRCLE_DRAGGABLE_VISIBLE_ICON_ID"
    const val CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID = "CIRCLE_DRAGGABLE_VISIBLE_LAYER_ID"

    const val CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID = "CIRCLE_DRAGGABLE_INVISIBLE_ICON_ID"

    const val CIRCLE_DRAGGABLE_BEARING = 90.0

    const val GEOFENCE_MIN_RADIUS = 10
}

object TrackerCons {
    const val TRACKER_COLLECTION = LOCATION_AWS_PREFIX + LOCATION_TRACKERS_PREFIX + "Tracker"
}

object MapNames {
    const val ESRI_LIGHT = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.Light"
    const val ESRI_STREET_MAP = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.Streets"
    const val ESRI_NAVIGATION = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.Navigation"
    const val ESRI_DARK_GRAY_CANVAS = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.DarkGrayCanvas"
    const val ESRI_LIGHT_GRAY_CANVAS = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.LightGrayCanvas"
    const val ESRI_IMAGERY = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Esri.Imagery"
    const val HERE_CONTRAST = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "HERE.Contrast"
    const val HERE_EXPLORE = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "HERE.Explore"
    const val HERE_EXPLORE_TRUCK = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "HERE.ExploreTruck"
    const val HERE_HYBRID = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "HERE.Hybrid"
    const val HERE_IMAGERY = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "HERE.Imagery"
    const val GRAB_LIGHT = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Grab.StandardLight"
    const val GRAB_DARK = LOCATION_AWS_PREFIX + LOCATION_MAPS_PREFIX + "Grab.StandardDark"
}

object MapStyles {
    const val VECTOR_ESRI_TOPOGRAPHIC = "VectorEsriTopographic"
    const val VECTOR_ESRI_STREETS = "VectorEsriStreets"
    const val VECTOR_ESRI_NAVIGATION = "VectorEsriNavigation"
    const val VECTOR_ESRI_DARK_GRAY_CANVAS = "VectorEsriDarkGrayCanvas"
    const val VECTOR_ESRI_LIGHT_GRAY_CANVAS = "VectorEsriLightGrayCanvas"
    const val RASTER_ESRI_IMAGERY = "RasterEsriImagery"
    const val VECTOR_HERE_CONTRAST = "VectorHereContrast"
    const val VECTOR_HERE_EXPLORE = "VectorHereExplore"
    const val VECTOR_HERE_EXPLORE_TRUCK = "VectorHereExploreTruck"
    const val HYBRID_HERE_EXPLORE_SATELLITE = "HybridHereExploreSatellite"
    const val RASTER_HERE_EXPLORE_SATELLITE = "RasterHereExploreSatellite"
    const val GRAB_LIGHT = "VectorGrabStandardLight"
    const val GRAB_DARK = "VectorGrabStandardDark"
}

object DateFormat {
    const val MMM_DD_YYYY = "MMM dd, yyyy"
    const val HH_MM_AA = "HH:mm aa"
}
