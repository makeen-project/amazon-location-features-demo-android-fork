package com.aws.amazonlocation.data.datasource

import android.content.Context
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.response.SearchSuggestionResponse
import com.aws.amazonlocation.domain.`interface`.SearchPlaceInterface
import com.aws.amazonlocation.setConnectivity
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.MapHelper
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.mockedInternetAvailability
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito.* // ktlint-disable no-wildcard-imports
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.anyOrNull
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class RemoteDataSourceImplTest : BaseTest() {
class RemoteDataSourceImplTest: BaseTest() {

    @Spy private var context: Context = RuntimeEnvironment.getApplication().applicationContext

    @Spy private var preferenceManager: PreferenceManager = PreferenceManager(context)

    @Spy private var mapHelper: MapHelper = MapHelper(context)

    @Spy private var awsLocationHelper: AWSLocationHelper = AWSLocationHelper(mapHelper, preferenceManager)

    @InjectMocks
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private var mainActivity: MainActivity = mock(MainActivity::class.java)

    override fun setUp() {
        super.setUp()

        doNothing().`when`(mainActivity).handleException(anyOrNull(), anyOrNull())
        awsLocationHelper.initAWSMobileClient(mainActivity)
        mockedInternetAvailability = true

        setConnectivity(true)
    }

    @Test
    fun searchPlaceSuggestions() {

        val latch = CountDownLatch(1)

        val loc = mapHelper.getLiveLocation()

        var response: SearchSuggestionResponse? = null

        mRemoteDataSourceImpl.searchPlaceSuggestions(
            loc?.latitude,
            loc?.longitude,
            "Tinto",
            object : SearchPlaceInterface {
                override fun getSearchPlaceSuggestionResponse(suggestionResponse: SearchSuggestionResponse?) {
                    response = suggestionResponse
                    latch.countDown()
                }

                override fun error(searchResponse: SearchSuggestionResponse) {
                    Assert.fail("Received error")
                    latch.countDown()
                }

                override fun internetConnectionError(error: String) {
                    Assert.fail("Internet connection error")
                    latch.countDown()
                }
            },
        )

        latch.await(5000, TimeUnit.MILLISECONDS)

        if (response == null) {
            Assert.fail("Response is null")
        }

        if (response?.error != null) {
            Assert.fail("Received error")
        }
    }
}
