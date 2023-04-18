package com.aws.amazonlocation.viewmodel.signin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.data.common.HandleResult
import com.aws.amazonlocation.data.datasource.RemoteDataSourceImpl
import com.aws.amazonlocation.data.repository.AuthImp
import com.aws.amazonlocation.domain.`interface`.SignInInterface
import com.aws.amazonlocation.domain.usecase.AuthUseCase
import com.aws.amazonlocation.mock.* // ktlint-disable no-wildcard-imports
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
class SignInVMSignOutWithAmazon : BaseTest() {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mMockContext = RuntimeEnvironment.getApplication().applicationContext

    @Mock
    private lateinit var mRemoteDataSourceImpl: RemoteDataSourceImpl

    private lateinit var authImp: AuthImp

    private lateinit var authUseCase: AuthUseCase

    private var mPreferenceManager: PreferenceManager = PreferenceManager(mMockContext)

    private lateinit var mSignInViewModel: SignInViewModel

    @Before
    override fun setUp() {
        super.setUp()

        authImp = AuthImp(mRemoteDataSourceImpl)
        authUseCase = AuthUseCase(authImp)
        mSignInViewModel = SignInViewModel(
            authUseCase,
            mPreferenceManager,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signOutWithAmazonSuccess() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signOutWithAmazon(any(), any(), any())).thenAnswer {
            val isDisconnectFromAWSRequired: Boolean = it.arguments[1] as Boolean
            val callback: SignInInterface = it.arguments[2] as SignInInterface
            callback.signOutSuccess(SIGN_OUT_SUCCESS, isDisconnectFromAWSRequired)
        }

        mSignInViewModel.mSignOutResponse.test {
            mSignInViewModel.signOutWithAmazon(mMockContext, true)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_SUCCESS, result is HandleResult.Success)
            Assert.assertTrue(TEST_FAILED_DUE_TO_SIGN_OUT_NOT_SUCCESS, (result as HandleResult.Success).response.message == SIGN_OUT_SUCCESS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun signOutWithAmazonError() = runTest {
        Mockito.`when`(mRemoteDataSourceImpl.signOutWithAmazon(any(), any(), any())).thenAnswer {
            val callback: SignInInterface = it.arguments[2] as SignInInterface
            callback.signOutFailed(MOCK_ERROR)
        }

        mSignInViewModel.mSignOutResponse.test {
            mSignInViewModel.signOutWithAmazon(mMockContext, true)
            val result = awaitItem()
            Assert.assertTrue(TEST_FAILED_DUE_TO_STATE_NOT_ERROR, result is HandleResult.Error)
            Assert.assertTrue(TEST_FAILED_DUE_TO_INCORRECT_ERROR_MESSAGE, (result as HandleResult.Error).exception.messageResource == MOCK_ERROR)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
