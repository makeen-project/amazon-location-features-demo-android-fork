package com.aws.amazonlocation.mock

import com.amazonaws.services.geo.model.Place
import com.amazonaws.services.geo.model.PlaceGeometry
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.models.Coordinates
import com.mapbox.mapboxsdk.geometry.LatLng

const val DELAY_1000 = 1000L
const val DELAY_300 = 300L

val DEFAULT_LOCATION = LatLng(49.281174, -123.116823)
const val DEVICE_ID = "662f86eddc909886"

const val SEARCH_TEXT_RIO_TINTO = "rio tinto"
val GATE_WAY_OF_INDIA_LAT_LNG = LatLng(18.921880000000044, 72.83468000000005) // Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_FROM = LatLng(18.92216, 72.83373) // Near Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_TO = LatLng(18.92169000000007, 72.83312000000006) // The Taj Palace Hotel, Mumbai, Mumbai, Mahārāshtra, IND
const val AVOID_FERRIES = false
const val AVOID_TOLLS = true

const val VALID_LAT = 18.921880000000044
const val VALID_LNG = 72.83468000000005
const val INVALID_LAT = 91.0
const val INVALID_LNG = 181.0

const val LAT_LNG_VALID_STRING = "$VALID_LAT, $VALID_LNG"
const val LAT_LNG_VALID_LAT_STRING = "$VALID_LAT, $INVALID_LNG"
const val LAT_LNG_VALID_LNG_STRING = "$INVALID_LAT, $VALID_LNG"
const val LAT_LNG_INVALID_STRING = "$INVALID_LAT, $INVALID_LNG"

const val ESRI = "Esri"
const val HERE = "HERE"

const val SIGN_IN_SUCCESS = "Sign in Successfully"
const val SIGN_OUT_SUCCESS = "Sign out successfully"
const val NO_BROWSERS_INSTALLED = "No browsers installed."

const val TRACKING_HISTORY_START_DATE = "Thu Apr 14 00:00:00 GMT+05:30 2022"
const val TRACKING_HISTORY_END_DATE = "Fri Apr 14 23:59:59 GMT+05:30 2023"
const val TRACKING_HISTORY_TODAY_START_DATE = "Fri Apr 14 00:00:00 GMT+05:30 2023"
const val TRACKING_HISTORY_TODAY_END_DATE = "Fri Apr 14 23:59:59 GMT+05:30 2023"

const val START_DATE_NULL = "Start date is null"
const val END_DATE_NULL = "End date is null"

const val MOCK_ERROR = "Mock error"
const val API_ERROR = "Please try again later"
const val NO_INTERNET_ERROR = "Check your internet connection and try again"

const val TEST_FAILED_DUE_TO_STATE_NOT_LOADING = "Test failed due to state not loading"
const val TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS = "Test failed due to state not success"
const val TEST_FAILED_DUE_TO_STATE_NOT_ERROR = "Test failed due to state not error"
const val TEST_FAILED_DUE_TO_INCORRECT_DATA = "Test failed due to incorrect data"
const val TEST_FAILED_DUE_TO_DATA_NOT_EMPTY = "Test failed due to data not empty"
const val TEST_FAILED_DUE_TO_INCORRECT_DATA_SIZE = "Test failed due to incorrect data size"
const val TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE = "Test failed due to incorrect error message"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_INTERNET_ERROR = "Test failed due to incorrect no internet error"
const val TEST_FAILED_DUE_TO_LOGIN_NOT_SUCCESS = "Test failed due to login not success"
const val TEST_FAILED_DUE_TO_SIGN_OUT_NOT_SUCCESS = "Test failed due to sign out not success"

const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_PROVIDERS_LOADED = "Test failed due to incorrect no of providers loaded"
const val TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_ESRI = "Test failed due to incorrect style name for Esri"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_ESRI = "Test failed due to incorrect no of styles loaded for Esri"
const val TEST_FAILED_DUE_TO_INCORRECT_STYLE_NAME_FOR_HERE = "Test failed due to incorrect style name for HERE"
const val TEST_FAILED_DUE_TO_INCORRECT_NO_OF_STYLES_LOADED_FOR_HERE = "Test failed due to incorrect no of styles loaded for HERE"

const val TEST_FAILED_CLIENT_NOT_INITIALIZE = "Test failed- client not initialize"
const val TEST_FAILED_COGNITO_CREDENTIALS_PROVIDER_NOT_INITIALIZE = "Test failed- cognitoCredentialsProvider not initialize"

const val TEST_FAILED_DUE_TO_INVALID_LATITUDE = "Test failed due to invalid latitude"
const val TEST_FAILED_DUE_TO_INVALID_LONGITUDE = "Test failed due to invalid longitude"

val PLACE_TO_AMAZON_LOCATION_INPUT = Place()
    .withCountry("PRT")
    .withGeometry(
        PlaceGeometry()
            .withPoint(
                arrayListOf(-8.556649999999934, 41.17271000000005),
            ),
    )
    .withInterpolated(false)
    .withLabel("Rio Tinto, Gondomar, Porto, PRT")
    .withMunicipality("Gondomar")
    .withNeighborhood("Rio Tinto")
    .withRegion("Porto")
    .withSubRegion("Gondomar")

val PLACE_TO_AMAZON_LOCATION_OUTPUT = AmazonLocationPlace(
    coordinates = Coordinates(
        41.17271000000005,
        -8.556649999999934,
    ),
    label = "Rio Tinto, Gondomar, Porto, PRT",
    addressNumber = null,
    street = null,
    country = "PRT",
    region = "Porto",
    subRegion = "Gondomar",
    municipality = "Gondomar",
    neighborhood = "Rio Tinto",
    postalCode = null,
)

val GET_REGION_SUBREGION = "Gondomar"
val GET_REGION_REGION = "Porto"
val GET_REGION_COUNTRY = "PRT"

val GET_REGION_EMPTY = ""
val GET_REGION_SUBREGION_COUNTRY = "Gondomar, PRT"
val GET_REGION_REGION_COUNTRY = "Porto, PRT"

const val IDENTITY_POOL_ID_INVALID = "us-east-100:30f73277-588f-4587-a55a-256cc2e8a205"
const val REGION_INVALID = "us-east-2"
const val USER_POOL_ID_INVALID = "invalid-pool-id"
const val USER_POOL_CLIENT_ID_INVALID = "invalid-client-id"

const val PREF_MANAGER_KEY_STRING = "string"
const val PREF_MANAGER_KEY_INT = "int"
const val PREF_MANAGER_KEY_DOUBLE = "double"
const val PREF_MANAGER_KEY_LONG = "long"
const val PREF_MANAGER_KEY_STRING_SET = "string_set"
const val PREF_MANAGER_KEY_BOOL = "bool"

const val PREF_MANAGER_VALUE_STRING = "string"
const val PREF_MANAGER_VALUE_INT = 1
const val PREF_MANAGER_VALUE_DOUBLE = 1.0
const val PREF_MANAGER_VALUE_LONG = 1L
val PREF_MANAGER_VALUE_STRING_SET = setOf("string1", "string2")
const val PREF_MANAGER_VALUE_BOOL = true
