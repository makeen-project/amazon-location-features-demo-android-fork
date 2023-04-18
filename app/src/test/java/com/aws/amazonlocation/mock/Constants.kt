package com.aws.amazonlocation.mock

import com.mapbox.mapboxsdk.geometry.LatLng

val DEFAULT_LOCATION = LatLng(49.281174, -123.116823)
const val DEVICE_ID = "662f86eddc909886"

const val SEARCH_TEXT_RIO_TINTO = "rio tinto"
val GATE_WAY_OF_INDIA_LAT_LNG = LatLng(18.921880000000044, 72.83468000000005) // Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_FROM = LatLng(18.92216, 72.83373) // Near Gateway of India, Colaba, Mumbai, Maharashtra, 400001, IND
val DISTANCE_COORDINATE_TO = LatLng(18.92169000000007, 72.83312000000006) // The Taj Palace Hotel, Mumbai, Mumbai, Mahārāshtra, IND
const val AVOID_FERRIES = false
const val AVOID_TOLLS = true

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
