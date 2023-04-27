package com.aws.amazonlocation.utils.generalutils

import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.utils.amazonLocationPlace
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsAmazonLocationPlaceTest : BaseTest() {

    @Test
    fun amazonLocationPlaceSuccess() {
        val result = amazonLocationPlace(PLACE_TO_AMAZON_LOCATION_INPUT)
        val expected = PLACE_TO_AMAZON_LOCATION_OUTPUT
        Assert.assertTrue(
            TEST_FAILED_DUE_TO_INCORRECT_DATA,
            result.coordinates.latitude == expected.coordinates.latitude &&
                result.coordinates.longitude == expected.coordinates.longitude &&
                result.label == expected.label &&
                result.addressNumber == expected.addressNumber &&
                result.street == expected.street &&
                result.country == expected.country &&
                result.region == expected.region &&
                result.subRegion == expected.subRegion &&
                result.municipality == expected.municipality &&
                result.neighborhood == expected.neighborhood &&
                result.postalCode == expected.postalCode,
        )
    }
}
