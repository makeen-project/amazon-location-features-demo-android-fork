package com.aws.amazonlocation.ui.main.data_provider // ktlint-disable package-name

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentDataProviderBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AnalyticsAttribute
import com.aws.amazonlocation.utils.AnalyticsAttributeValue
import com.aws.amazonlocation.utils.EventType
import com.aws.amazonlocation.utils.KEY_GRAB_DONT_ASK
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.MapStyleRestartInterface
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.restartAppMapStyleDialog
import com.aws.amazonlocation.utils.restartApplication
import com.aws.amazonlocation.utils.show
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DataProviderFragment : BaseFragment() {

    private lateinit var mBinding: FragmentDataProviderBinding
    private var isGrabMapEnable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDataProviderBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isGrabMapEnable = isGrabMapEnable(mPreferenceManager)
        backPress()
        clickListener()
    }

    private fun clickListener() {
        val dataProvider =
            mPreferenceManager.getValue(KEY_MAP_NAME, resources.getString(R.string.esri))
        mBinding.apply {
            if (isGrabMapEnable) {
                llGrab.show()
            } else {
                llGrab.hide()
            }
        }
        mBinding.apply {
            when (dataProvider) {
                resources.getString(R.string.esri) -> {
                    changeDataProvider(isEsri = true, isGrab = false)
                }
                resources.getString(R.string.here) -> {
                    changeDataProvider(isEsri = false, isGrab = false)
                }
                resources.getString(R.string.grab) -> {
                    changeDataProvider(isEsri = false, isGrab = true)
                }
            }

            ivRouteDataProvider.setOnClickListener {
                findNavController().popBackStack()
            }

            llEsri.setOnClickListener {
                if (dataProvider == resources.getString(R.string.grab)) {
                    showRestartDialog(isHere = false, isGrab = false)
                } else {
                    changeDataProviderEsri()
                }
            }

            llHere.setOnClickListener {
                if (dataProvider == resources.getString(R.string.grab)) {
                    showRestartDialog(isHere = true, isGrab = false)
                } else {
                    changeDataProviderHere()
                }
            }

            llGrab?.setOnClickListener {
                if (dataProvider == resources.getString(R.string.esri) || dataProvider == resources.getString(R.string.here)) {
                    showRestartDialog(isHere = false, isGrab = true)
                } else {
                    changeDataProviderGrab()
                }
            }
        }
    }

    private fun changeDataProviderEsri() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.esri)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE, properties)
        changeDataProvider(isEsri = true, isGrab = false)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )

        if (mapStyle != getString(R.string.map_light) ||
            mapStyle != getString(R.string.map_streets) ||
            mapStyle != getString(R.string.map_navigation) ||
            mapStyle != getString(R.string.map_dark_gray) ||
            mapStyle != getString(R.string.map_light_gray) ||
            mapStyle != getString(R.string.map_imagery)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_light)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.esri))
    }

    private fun changeDataProviderHere() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.here)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE, properties)
        changeDataProvider(isEsri = false, isGrab = false)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )
        if (mapStyle != getString(R.string.map_contrast) ||
            mapStyle != getString(R.string.map_explore) ||
            mapStyle != getString(R.string.map_explore_truck) ||
            /*mapStyle != getString(R.string.map_hybrid) ||*/
            mapStyle != getString(R.string.map_raster)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_explore)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.here))
    }

    private fun showRestartDialog(isHere: Boolean, isGrab: Boolean) {
        if (isGrab) {
            val shouldShowGrabDialog = !mPreferenceManager.getValue(KEY_GRAB_DONT_ASK, false)
            if (shouldShowGrabDialog) {
                activity?.restartAppMapStyleDialog(object : MapStyleRestartInterface {
                    override fun onOkClick(dialog: DialogInterface, dontAskAgain: Boolean) {
                        mPreferenceManager.setValue(KEY_GRAB_DONT_ASK, dontAskAgain)
                        changeDataProviderGrab()
                        lifecycleScope.launch {
                            if (!isRunningTest) {
                                delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                                activity?.restartApplication()
                            }
                        }
                    }

                    override fun onLearnMoreClick(dialog: DialogInterface) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(BuildConfig.GRAB_LEARN_MORE)
                            )
                        )
                    }
                })
            } else {
                changeDataProviderGrab()
                lifecycleScope.launch {
                    if (!isRunningTest) {
                        delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                        activity?.restartApplication()
                    }
                }
            }
        } else {
            if (isHere) {
                changeDataProviderHere()
            } else {
                changeDataProviderEsri()
            }
            lifecycleScope.launch {
                if (!isRunningTest) {
                    delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                    activity?.restartApplication()
                }
            }
        }
    }
    private fun changeDataProviderGrab() {
        val properties = listOf(
            Pair(AnalyticsAttribute.PROVIDER, resources.getString(R.string.grab)),
            Pair(AnalyticsAttribute.TRIGGERED_BY, AnalyticsAttributeValue.SETTINGS)
        )
        (activity as MainActivity).analyticsHelper?.recordEvent(
            EventType.MAP_PROVIDER_CHANGE, properties)
        changeDataProvider(isEsri = false, isGrab = true)
        val mapStyle = mPreferenceManager.getValue(
            KEY_MAP_STYLE_NAME,
            resources.getString(R.string.map_light)
        )
        if (mapStyle != getString(R.string.map_grab_light) ||
            mapStyle != getString(R.string.map_grab_dark)
        ) {
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                resources.getString(R.string.map_grab_light)
            )
        }
        mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.grab))
    }

    private fun changeDataProvider(isEsri: Boolean = false, isGrab: Boolean = false) {
        mBinding.apply {
            if (isGrab) {
                ivGrab.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.icon_checkmark
                    )
                )
                ivHere.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
                ivEsri.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
                return
            }
            if (isEsri) {
                ivEsri.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.icon_checkmark
                    )
                )
                ivHere.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
                ivGrab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
            } else {
                ivEsri.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
                ivHere.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.icon_checkmark
                    )
                )
                ivGrab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_radio_button_unchecked
                    )
                )
            }
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
