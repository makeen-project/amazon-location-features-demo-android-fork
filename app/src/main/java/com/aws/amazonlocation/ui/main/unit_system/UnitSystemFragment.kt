package com.aws.amazonlocation.ui.main.unit_system // ktlint-disable package-name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentUnitSystemBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.KEY_UNIT_SYSTEM

class UnitSystemFragment : BaseFragment() {

    private lateinit var mBinding: FragmentUnitSystemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentUnitSystemBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backPress()
        clickListener()
    }

    private fun clickListener() {
        val unitSystem =
            mPreferenceManager.getValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
        mBinding.apply {
            changeDataProvider(unitSystem)

            ivRouteUnitSystem.setOnClickListener {
                findNavController().popBackStack()
            }

            llAutomatic.setOnClickListener {
                changeDataProvider(resources.getString(R.string.automatic))
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.automatic))
            }

            llMetric.setOnClickListener {
                changeDataProvider(resources.getString(R.string.metric))
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.metric))
            }

            llImperial.setOnClickListener {
                changeDataProvider(resources.getString(R.string.imperial))
                mPreferenceManager.setValue(KEY_UNIT_SYSTEM, resources.getString(R.string.imperial))
            }
        }
    }

    private fun changeDataProvider(dataProvider: String?) {
        mBinding.apply {
            when (dataProvider) {
                resources.getString(R.string.metric) -> {
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark,
                        ),
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                }
                resources.getString(R.string.imperial) -> {
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark,
                        ),
                    )
                }
                else -> {
                    ivAutomatic.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.icon_checkmark,
                        ),
                    )
                    ivMetric.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                    ivImperial.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_radio_button_unchecked,
                        ),
                    )
                }
            }
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
