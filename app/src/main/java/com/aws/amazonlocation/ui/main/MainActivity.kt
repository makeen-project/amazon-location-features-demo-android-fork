package com.aws.amazonlocation.ui.main

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.Tokens
import com.amazonaws.mobileconnectors.cognitoauth.AuthClient
import com.amazonaws.regions.Region
import com.amazonaws.services.iot.AWSIotClient
import com.amazonaws.services.iot.model.AttachPolicyRequest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.common.onError
import com.aws.amazonlocation.data.common.onLoading
import com.aws.amazonlocation.data.common.onSuccess
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.enum.TabEnum
import com.aws.amazonlocation.data.enum.TrackingEnum
import com.aws.amazonlocation.databinding.ActivityMainBinding
import com.aws.amazonlocation.domain.`interface`.CloudFormationInterface
import com.aws.amazonlocation.domain.`interface`.SignInConnectInterface
import com.aws.amazonlocation.domain.`interface`.SignInRequiredInterface
import com.aws.amazonlocation.ui.base.BaseActivity
import com.aws.amazonlocation.ui.main.explore.ExploreFragment
import com.aws.amazonlocation.ui.main.signin.SignInViewModel
import com.aws.amazonlocation.ui.main.welcome.WelcomeBottomSheetFragment
import com.aws.amazonlocation.utils.*
import com.aws.amazonlocation.utils.Durations.DELAY_FOR_GEOFENCE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class MainActivity : BaseActivity() {

    private var isAppNotFirstOpened: Boolean = false
    private var reStartApp: Boolean = false
    private lateinit var mNavHostFragment: NavHostFragment
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private var mAuthStatus: String? = null
    private val mSignInViewModel: SignInViewModel by viewModels()
    private var mBottomSheetDialog: Dialog? = null
    private var alertDialog: Dialog? = null
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkMap()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        makeTransparentStatusBar()
        reStartApp = mPreferenceManager.getBooleanValue(KEY_RE_START_APP, false)
        val mTab = mPreferenceManager.getValue(KEY_TAB_ENUM, "")
        mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
        initObserver()
        mNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mNavController = mNavHostFragment.navController
        mBinding.bottomNavigationMain.setupWithNavController(mNavController)
        if (mBottomSheetDialog == null) {
            setBottomBar()
        }

        isAppNotFirstOpened = mPreferenceManager.getValue(IS_APP_FIRST_TIME_OPENED, false)

        val inflater = mNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        if (!isAppNotFirstOpened) {
            val welcomeSheet = WelcomeBottomSheetFragment()
            this.supportFragmentManager.let {
                welcomeSheet.show(it, WelcomeBottomSheetFragment::javaClass.name)
            }
            mBinding.bottomNavigationMain.hide()
        } else {
            graph.startDestination = R.id.explore_fragment
            mNavHostFragment.navController.graph = graph
            if (mBottomSheetDialog == null) {
                setBottomBar()
            }
        }
        if (reStartApp) {
            if (!mTab.isNullOrEmpty()) {
                when (mTab) {
                    TabEnum.TAB_EXPLORE.name -> {
                        mAuthStatus = mPreferenceManager.getValue(
                            KEY_CLOUD_FORMATION_STATUS,
                            AuthEnum.DEFAULT.name
                        )
                        when (mAuthStatus) {
                            AuthEnum.AWS_CONNECTED.name -> {
                                if (reStartApp) {
                                    reStartApp = false
                                    mPreferenceManager.removeValue(KEY_RE_START_APP)
                                    mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                        mSignInConnectInterface
                                    )
                                }
                            }
                        }
                    }
                    TabEnum.TAB_TRACKING.name -> {
                        mBinding.bottomNavigationMain.selectedItemId =
                            R.id.menu_tracking
                    }
                    TabEnum.TAB_GEOFENCE.name -> {
                        mBinding.bottomNavigationMain.selectedItemId =
                            R.id.menu_geofence
                    }
                }
            }
        }
    }

    fun geofenceClick() {
        mBinding.bottomNavigationMain.selectedItemId = R.id.menu_geofence
    }

    private fun initObserver() {
        lifecycleScope.launchWhenStarted {
            mSignInViewModel.mSignInResponse.collect { handleResult ->
                handleResult.onLoading {
                }.onSuccess {
                    mIsUserLoggedIn = true
                    mPreferenceManager.setValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.SIGNED_IN.name
                    )
                    getTokenAndAttachPolicy(it)
                }.onError { it ->
                    setBottomBar()
                    hideProgress()
                    it.messageResource?.let {
                        showError(it.toString())
                    }
                }
            }
        }
    }

    fun getTokenAndAttachPolicy(it: String) {
        AWSMobileClient.getInstance().getTokens(object : Callback<Tokens> {
            override fun onResult(result: Tokens?) {
                result?.accessToken?.tokenString?.let { accessToken ->
                    mPreferenceManager.setValue(
                        KEY_ACCESS_TOKEN,
                        accessToken
                    )
                }
                result?.refreshToken?.tokenString?.let { refreshToken ->
                    mPreferenceManager.setValue(
                        KEY_REFRESH_TOKEN,
                        refreshToken
                    )
                }

                val mCognitoCredentialsProvider: CognitoCredentialsProvider? =
                    mAWSLocationHelper.initCognitoCachingCredentialsProvider()

                val identityId: String? =
                    mCognitoCredentialsProvider?.identityId

                // Initialize the AWSIotMqttManager with the configuration
                val attachPolicyReq =
                    AttachPolicyRequest().withPolicyName(IOT_POLICY)
                        .withTarget(identityId)
                val mIotAndroidClient =
                    AWSIotClient(mCognitoCredentialsProvider)
                var region = mPreferenceManager.getValue(KEY_USER_REGION, "")

                if (region.isNullOrEmpty()) {
                    region = BuildConfig.DEFAULT_REGION
                }
                mIotAndroidClient.setRegion(Region.getRegion(region))
                mIotAndroidClient.attachPolicy(attachPolicyReq)
                showError(it)
                hideProgressAndShowData()
            }

            override fun onError(e: Exception?) {
                hideProgressAndShowData()
            }
        })
    }

    private fun hideProgressAndShowData() {
        hideProgress()
        runOnUiThread {
            if (mBinding.bottomNavigationMain.selectedItemId == R.id.menu_geofence) {
                mGeofenceUtils?.showGeofenceListBottomSheet(this@MainActivity)
                mBottomSheetHelper.hideSearchBottomSheet(true)
            } else if (mBinding.bottomNavigationMain.selectedItemId == R.id.menu_tracking) {
                if (checkMap()) {
                    mBottomSheetHelper.hideSearchBottomSheet(true)
                    showTracking()
                }
            }
            setBottomBar()
        }
        mBottomSheetDialog?.dismiss()
        mBottomSheetDialog = null
    }

    private fun setBottomBar() {
        mBinding.bottomNavigationMain.setOnItemSelectedListener { item ->
            mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
            when (item.itemId) {
                R.id.menu_explore -> {
                    setExplorer()
                }
                R.id.menu_tracking -> {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment !is ExploreFragment) {
                        mNavController.navigate(R.id.explore_fragment)
                    }

                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    mAuthStatus = mPreferenceManager.getValue(
                        KEY_CLOUD_FORMATION_STATUS,
                        AuthEnum.DEFAULT.name
                    )
                    when (mAuthStatus) {
                        AuthEnum.DEFAULT.name -> {
                            mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
                                TabEnum.TAB_TRACKING,
                                mCloudFormationInterface
                            )
                        }
                        AuthEnum.AWS_CONNECTED.name -> {
                            if (reStartApp) {
                                reStartApp = false
                                mPreferenceManager.removeValue(KEY_RE_START_APP)
                                mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                    mSignInConnectInterface
                                )
                            } else {
                                mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                    mSignInRequiredInterface
                                )
                            }
                        }
                        AuthEnum.SIGNED_IN.name -> {
                            if (mBottomSheetHelper.isDirectionSearchSheetVisible()) {
                                mBottomSheetHelper.hideDirectionSearchBottomSheet(fragment as ExploreFragment)
                            }
                            if (mTrackingUtils?.isTrackingSheetHidden() == true) {
                                if (checkMap()) {
                                    showTracking()
                                }
                            }
                        }
                        else -> mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
                            TabEnum.TAB_TRACKING,
                            mCloudFormationInterface
                        )
                    }
                }
                R.id.menu_geofence -> {
                    val fragment = mNavHostFragment.childFragmentManager.fragments[0]
                    if (fragment !is ExploreFragment) {
                        mNavController.navigate(R.id.explore_fragment)
                    }
                    mTrackingUtils?.hideTrackingBottomSheet()
                    mAuthStatus = mPreferenceManager.getValue(KEY_CLOUD_FORMATION_STATUS, "")
                    if (!mAuthStatus.isNullOrEmpty()) {
                        when (mAuthStatus) {
                            AuthEnum.DEFAULT.name -> {
                                mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
                                    TabEnum.TAB_GEOFENCE,
                                    mCloudFormationInterface
                                )
                            }
                            AuthEnum.AWS_CONNECTED.name -> {
                                if (reStartApp) {
                                    reStartApp = false
                                    mPreferenceManager.removeValue(KEY_RE_START_APP)
                                    mGeofenceBottomSheetHelper.signInConnectedBottomSheet(
                                        mSignInConnectInterface
                                    )
                                } else {
                                    mGeofenceBottomSheetHelper.signInRequiredBottomSheet(
                                        mSignInRequiredInterface
                                    )
                                }
                            }
                            AuthEnum.SIGNED_IN.name -> {
                                showGeofence()
                            }
                            else -> mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
                                TabEnum.TAB_GEOFENCE,
                                mCloudFormationInterface
                            )
                        }
                    } else {
                        mGeofenceBottomSheetHelper.cloudFormationBottomSheet(
                            TabEnum.TAB_GEOFENCE,
                            mCloudFormationInterface
                        )
                    }
                }
                R.id.menu_settings -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.setting_fragment)
                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    mTrackingUtils?.hideTrackingBottomSheet()
                }
                R.id.menu_more -> {
                    mBottomSheetHelper.hideSearchBottomSheet(false)
                    mNavController.navigate(R.id.about_fragment)
                    mGeofenceUtils?.hideAllGeofenceBottomSheet()
                    mTrackingUtils?.hideTrackingBottomSheet()
                }
            }
            true
        }
    }

    fun setExplorer() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment !is ExploreFragment) {
            mNavController.navigate(R.id.explore_fragment)
        } else {
            fragment.showDirectionAndCurrentLocationIcon()
        }
        mBottomSheetHelper.hideSearchBottomSheet(false)
        mBottomSheetHelper.hideMapStyleSheet()
        mGeofenceUtils?.hideAllGeofenceBottomSheet()
        mTrackingUtils?.hideTrackingBottomSheet()
    }

    fun showBottomBar() {
        mBinding.bottomNavigationMain.show()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthClient.CUSTOM_TABS_ACTIVITY_CODE &&
            resultCode == RESULT_CANCELED
        ) {
            hideProgress()
        }
    }

    private fun showGeofence() {
        val fragment = mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.hideDirectionAndCurrentLocationIcon()
        }
        mBottomSheetHelper.hideSearchBottomSheet(true)
        lifecycleScope.launch {
            if (fragment !is ExploreFragment) {
                delay(DELAY_FOR_GEOFENCE) // Need delay for showing bottomsheet after fragment load
            }
            mGeofenceUtils?.showGeofenceListBottomSheet(this@MainActivity)
        }
    }

    private fun showTracking() {
        val fragment =
            mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.showDirectionAndCurrentLocationIcon()
        }
        mBottomSheetHelper.hideSearchBottomSheet(true)
        mBottomSheetHelper.hideMapStyleSheet()
        showTrackingBottomSheet()
    }

    private fun checkMap(): Boolean {
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))
        if (mapName == getString(R.string.map_esri)) {
            enableTrackingDialog()
            return false
        }
        return true
    }

    private fun showTrackingBottomSheet() {
        if (mPreferenceManager.getValue(
                IS_LOCATION_TRACKING_ENABLE,
                false
            )
        ) {
            mTrackingUtils?.showTrackingBottomSheet(TrackingEnum.TRACKING_HISTORY)
        } else {
            mTrackingUtils?.showTrackingBottomSheet(TrackingEnum.ENABLE_TRACKING)
        }
    }

    fun manageBottomNavigationVisibility(isVisibility: Boolean = true) {
        mBinding.apply {
            if (isVisibility) {
                bottomNavigationMain.animate().translationY(0f).start()
                bottomNavigationMain.show()
            } else {
                bottomNavigationMain.animate().translationY(100f).start()
                bottomNavigationMain.hide()
            }
        }
    }

    private val mSignInConnectInterface = object :
        SignInConnectInterface {
        override fun signIn(dialog: Dialog?) {
            mBottomSheetDialog = dialog
            mSignInViewModel.signInWithAmazon(this@MainActivity)
        }

        override fun continueToExplore(dialog: Dialog?) {
            moveToExploreScreen()
            dialog?.dismiss()
        }
    }

    private val mSignInRequiredInterface = object : SignInRequiredInterface {
        override fun signInClick(dialog: Dialog?) {
            mBottomSheetDialog = dialog
            mSignInViewModel.signInWithAmazon(this@MainActivity)
        }

        override fun mayBeLaterClick(dialog: Dialog?) {
            moveToExploreScreen()
            dialog?.dismiss()
        }
    }

    fun moveToExploreScreen() {
        mBinding.bottomNavigationMain.menu.findItem(R.id.menu_explore).isChecked = true
    }

    private val mCloudFormationInterface = object : CloudFormationInterface {
        override fun dialogDismiss(dialog: Dialog?) {
            moveToExploreScreen()
            dialog?.dismiss()
        }
    }

    override fun onBackPressed() {
        if (mNavController.currentDestination?.label == AWS_CLOUD_INFORMATION_FRAGMENT) {
            mNavController.popBackStack()
        }else if (mNavController.currentDestination?.label == VERSION_FRAGMENT) {
            mNavController.popBackStack()
        } else if (mNavController.currentDestination?.label == ABOUT_FRAGMENT) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment !is ExploreFragment) {
                mNavController.navigate(R.id.explore_fragment)
            }
            moveToExploreScreen()
        } else if (mNavController.currentDestination?.label == SETTING_FRAGMENT) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment !is ExploreFragment) {
                mNavController.navigate(R.id.explore_fragment)
            }
            moveToExploreScreen()
        } else if (mBottomSheetHelper.isAttributeExpandedOrHalfExpand()) {
            mBottomSheetHelper.hideAttributeSheet()
        } else if (mBottomSheetHelper.isSearchBottomSheetExpandedOrHalfExpand()) {
            mBottomSheetHelper.collapseSearchBottomSheet()
        } else if (mBottomSheetHelper.isMapStyleExpandedOrHalfExpand()) {
            mBottomSheetHelper.hideMapStyleSheet()
        } else if (mBottomSheetHelper.isNavigationBottomSheetHalfExpand()) {
            val fragment = mNavHostFragment.childFragmentManager.fragments[0]
            if (fragment is ExploreFragment) {
                fragment.navigationExit()
            }
        } else if (mBottomSheetHelper.isNavigationBottomSheetFullyExpand()) {
            mBottomSheetHelper.collapseNavigatingSheet()
        } else if (mBottomSheetHelper.isDirectionSearchExpandedOrHalfExpand()) {
            mBottomSheetHelper.collapseDirectionSearch()
        } else if (mGeofenceUtils?.isGeofenceListExpandedOrHalfExpand() == true) {
            mGeofenceUtils?.collapseGeofenceList()
        } else if (mGeofenceUtils?.isAddGeofenceExpandedOrHalfExpand() == true) {
            mGeofenceUtils?.collapseAddGeofence()
        } else if (mTrackingUtils?.isTrackingExpandedOrHalfExpand() == true) {
            mTrackingUtils?.collapseTracking()
        } else {
            super.onBackPressed()
        }
    }

    fun showProgress() {
        mBinding.apply {
            runOnUiThread {
                alertDialog = Dialog(this@MainActivity)
                alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alertDialog?.setContentView(R.layout.dialog_progress)
                alertDialog?.setCancelable(false)
                alertDialog?.setCanceledOnTouchOutside(false)
                alertDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(this@MainActivity, android.R.color.transparent)
                    )
                )
                alertDialog?.show()
            }
        }
    }

    fun hideProgress() {
        runOnUiThread {
            if (alertDialog?.isShowing == true) {
                alertDialog?.hide()
            }
        }
    }

    private fun enableTrackingDialog() {
        enableTracker(
            object : EnableTrackerInterface {
                override fun continueToTracker(dialog: DialogInterface) {
                    showTracking()
                    mBinding.bottomNavigationMain.menu.findItem(R.id.menu_tracking).isChecked = true
                }

                override fun cancel() {
                    mBinding.bottomNavigationMain.menu.findItem(R.id.menu_explore).isChecked = true
                    setExplorer()
                }

                override fun viewTermsAndCondition(dialog: DialogInterface) {
                    resultLauncher.launch(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.BASE_DOMAIN+BuildConfig.AWS_TERMS_URL)
                        )
                    )
                }
            }
        )
    }

    fun setWelcomeToExplorer() {
        mPreferenceManager.setValue(IS_APP_FIRST_TIME_OPENED, true)
        isAppNotFirstOpened = true
        val fragment =
            mNavHostFragment.childFragmentManager.fragments[0]
        if (fragment is ExploreFragment) {
            fragment.checkPermissionFromWelcome()
        }
    }

    fun isAppNotFirstOpened(): Boolean {
        return isAppNotFirstOpened
    }
}
