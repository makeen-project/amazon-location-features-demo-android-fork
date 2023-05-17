package com.aws.amazonlocation.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException
import com.amazonaws.services.geo.model.InternalServerException
import com.amazonaws.services.geo.model.ResourceNotFoundException
import com.amazonaws.services.geo.model.ThrottlingException
import com.amazonaws.services.geo.model.ValidationException
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.enum.AuthEnum
import com.aws.amazonlocation.data.response.LoginResponse
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.ui.main.geofence.GeofenceBottomSheetHelper
import com.aws.amazonlocation.ui.main.geofence.GeofenceUtils
import com.aws.amazonlocation.ui.main.tracking.TrackingUtils
import com.aws.amazonlocation.utils.AWSLocationHelper
import com.aws.amazonlocation.utils.AmplifyHelper
import com.aws.amazonlocation.utils.BottomSheetHelper
import com.aws.amazonlocation.utils.KEY_CLOUD_FORMATION_STATUS
import com.aws.amazonlocation.utils.KEY_LOCATION_PERMISSION
import com.aws.amazonlocation.utils.KEY_USER_DETAILS
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.restartApplication
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    var mIsUserLoggedIn: Boolean = false
    private var mAlertDialog: AlertDialog? = null

    @Inject
    lateinit var mPreferenceManager: PreferenceManager

    lateinit var mGeofenceBottomSheetHelper: GeofenceBottomSheetHelper

    var mGeofenceUtils: GeofenceUtils? = null

    var mTrackingUtils: TrackingUtils? = null
    var isTablet: Boolean = false

    @Inject
    lateinit var mBottomSheetHelper: BottomSheetHelper

    @Inject
    lateinit var mAWSLocationHelper: AWSLocationHelper
    private var subTitle = ""

    @Inject
    lateinit var amplifyHelper: AmplifyHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            try {
                amplifyHelper.initAmplify()
            } catch (_: Exception) {}
        }

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        mGeofenceBottomSheetHelper = GeofenceBottomSheetHelper(this@BaseActivity)
        mGeofenceUtils = GeofenceUtils()

        val preference = PreferenceManager(this)
        mAWSLocationHelper.initAWSMobileClient(this@BaseActivity)
        mTrackingUtils = TrackingUtils(preference, this@BaseActivity, mAWSLocationHelper)
        locationPermissionDialog()
    }

    fun showError(error: String) {
        val snackBar = Snackbar.make(
            findViewById(R.id.nav_host_fragment),
            error,
            Snackbar.LENGTH_SHORT
        )
        val textView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 10
        snackBar.show()
    }

    private fun locationPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        dialogBuilder.setMessage(resources.getString(R.string.location_permission_is_required))
            ?.setCancelable(true)
            ?.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                startActivity(
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
            }
        mAlertDialog = dialogBuilder.create()
        mAlertDialog?.setTitle(resources.getString(R.string.permission))
    }

    fun showLocationPermissionDialogBox() {
        mAlertDialog?.isShowing?.let {
            if (!it) {
                mAlertDialog?.show()
            }
        }
    }

    fun getUserInfo(): LoginResponse? {
        return if (!mPreferenceManager.getValue(KEY_USER_DETAILS, "").isNullOrEmpty()) {
            val type = object : TypeToken<LoginResponse>() {}.type
            Gson().fromJson(mPreferenceManager.getValue(KEY_USER_DETAILS, ""), type)
        } else {
            null
        }
    }

    fun getLocationPermissionCount(): Int {
        return mPreferenceManager.getIntValue(KEY_LOCATION_PERMISSION, 0)
    }

    fun updateLocationPermission(isLocation: Boolean = false) {
        if (getLocationPermissionCount() <= 1) {
            mPreferenceManager.setValue(
                KEY_LOCATION_PERMISSION,
                if (isLocation) 2 else getLocationPermissionCount().plus(1)
            )
        }
    }

    fun resetLocationPermission() {
        mPreferenceManager.setValue(KEY_LOCATION_PERMISSION, 0)
    }

    fun clearUserInFo() {
        mIsUserLoggedIn = false
        mPreferenceManager.removeValue(KEY_USER_DETAILS)
    }

    fun bottomNavigationVisibility(isVisibility: Boolean) {
        if (this is MainActivity) {
            this.manageBottomNavigationVisibility(isVisibility)
        }
    }

    fun handleException(exception: Exception, message: String? = null) {
        subTitle = ""
        when (exception) {
            is ResourceNotFoundException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is com.amazonaws.services.cognitoidentity.model.ResourceNotFoundException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is com.amazonaws.services.cognitoidentityprovider.model.ResourceNotFoundException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is aws.sdk.kotlin.services.location.model.ResourceNotFoundException,
            is aws.sdk.kotlin.services.cognitoidentity.model.ResourceNotFoundException,
            is aws.sdk.kotlin.services.cognitoidentityprovider.model.ResourceNotFoundException -> {
                setTitle()
            }
            is NotAuthorizedException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is InternalServerException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is ValidationException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is ThrottlingException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
            is AmazonServiceException -> {
                if (exception.statusCode == 400 || exception.statusCode == 403 || exception.statusCode == 404) {
                    setTitle()
                }
            }
        }
        runOnUiThread {
            if (subTitle.isEmpty()) {
                if (message != null) {
                    if (message.isNotEmpty()) {
                        showError(message)
                    }
                } else {
                    exception.message?.let {
                        showError(it)
                    }
                }
            } else {
                showErrorDialog(subTitle)
            }
        }
    }

    private fun setTitle() {
        subTitle = getString(R.string.reconnect_with_aws_account)
    }

    private fun showErrorDialog(subTitle: String) {
        runOnUiThread {
            showError(subTitle)
            restartAppWithClearData()
        }
    }

    fun restartAppWithClearData() {
        lifecycleScope.launch {
            when (
                mPreferenceManager.getValue(
                    KEY_CLOUD_FORMATION_STATUS,
                    ""
                )
            ) {
                AuthEnum.SIGNED_IN.name -> {
                    AWSMobileClient.getInstance().signOut()
                }
            }
            mPreferenceManager.setDefaultConfig()
            delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
            restartApplication()
        }
    }
}
