package com.aws.amazonlocation.utils.generalutils

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.mockedInternetAvailability
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class GeneralUtilsIsInternetAvailableTest : BaseTest() {

    private val context: Context = RuntimeEnvironment.getApplication().baseContext

    @Before
    override fun setUp() {
        mockedInternetAvailability = null
    }

    @Test
    fun isInternetAvailableSuccess() {
        setConnectivity(true)
        var result = context.isInternetAvailable()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, result)
        setConnectivity(false)
        result = context.isInternetAvailable()
        Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_DATA, !result)
    }
}
