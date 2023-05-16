package com.aws.amazonlocation.ui.main.data_provider // ktlint-disable package-name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentDataProviderBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME

class DataProviderFragment : BaseFragment() {

    private lateinit var mBinding: FragmentDataProviderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDataProviderBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        clickListener()
    }

    private fun clickListener() {
        val dataProvider =
            mPreferenceManager.getValue(KEY_MAP_NAME, resources.getString(R.string.esri))
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

            llHere.setOnClickListener {
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
                        resources.getString(R.string.map_contrast)
                    )
                }
                mPreferenceManager.setValue(KEY_MAP_NAME, resources.getString(R.string.here))
            }

            llGrab.setOnClickListener {
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
        }
    }

    private fun changeDataProvider(isEsri: Boolean = false, isGrab: Boolean = false) {
        mBinding.apply {
            if (isGrab) {
                ivGrab.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_check_data_provider
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
                ivGrab.setImageDrawable(
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
                ivGrab.setImageDrawable(
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
