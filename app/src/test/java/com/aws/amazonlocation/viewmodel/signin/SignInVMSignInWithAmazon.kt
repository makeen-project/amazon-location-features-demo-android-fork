package com.aws.amazonlocation.viewmodel.signin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.utils.* // ktlint-disable no-wildcard-imports
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SignInVMSignInWithAmazon : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mMockContext = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var authImp: AuthImp

    private lateinit var authUseCase: AuthUseCase

    private var mPreferenceManager: PreferenceManager = PreferenceManager(mMockContext)

    private lateinit var mSignInViewModel: SignInViewModel

    @Mock
    private lateinit var activity: MainActivity

    @Before
    override fun setUp() {
        super.setUp()

        authImp = AuthImp(mRemoteDataSourceImpl)
        authUseCase = AuthUseCase(authImp)
        mSignInViewModel = SignInViewModel(
            authUseCase,
            mPreferenceManager,
        )

        mPreferenceManager.setValue(
            KEY_RE_START_APP,
            true,
        )

        mPreferenceManager.setValue(
            KEY_POOL_ID,
            BuildConfig.USER_POOL_ID,
        )
        mPreferenceManager.setValue(
            KEY_USER_REGION,
            BuildConfig.DEFAULT_REGION,
        )
        mPreferenceManager.setValue(
            KEY_USER_DOMAIN,
            BuildConfig.USER_DOMAIN,
        )
        mPreferenceManager.setValue(
            KEY_USER_POOL_CLIENT_ID,
            BuildConfig.USER_POOL_CLIENT_ID,
        )
        mPreferenceManager.setValue(
            KEY_USER_POOL_ID,
            BuildConfig.USER_POOL_ID,
        )
        mPreferenceManager.setValue(
            WEB_SOCKET_URL,
            BuildConfig.WEB_SOCKET_URL,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signInWithAmazonSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signInWithAmazon(any(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[1] as SignInInterface
            callback.getUserDetails(Responses.RESPONSE_SIGN_IN)
        }

        mSignInViewModel.mSignInResponse.test {
            mSignInViewModel.signInWithAmazon(activity)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_LOGIN_NOT_SUCCESS, (result as HandleResult.Success).response == SIGN_IN_SUCCESS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signInWithAmazonError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signInWithAmazon(any(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[1] as SignInInterface
            callback.signInFailed(NO_BROWSERS_INSTALLED)
        }

        mSignInViewModel.mSignInResponse.test {
            mSignInViewModel.signInWithAmazon(activity)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as HandleResult.Error).exception.messageResource == NO_BROWSERS_INSTALLED)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
