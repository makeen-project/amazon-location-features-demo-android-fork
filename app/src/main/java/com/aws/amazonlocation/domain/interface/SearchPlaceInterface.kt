package com.aws.amazonlocation.domain.`interface`

import com.aws.amazonlocation.data.response.SearchSuggestionResponse

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
interface SearchPlaceInterface {

    fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {}

    fun success(searchResponse: SearchSuggestionResponse) {}

    fun error(searchResponse: SearchSuggestionResponse) {}

    fun internetConnectionError(error: String) {}
}
