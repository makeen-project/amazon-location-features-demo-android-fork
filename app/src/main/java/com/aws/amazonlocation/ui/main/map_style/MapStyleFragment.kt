package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.isInternetAvailable

class MapStyleFragment : BaseFragment() {

    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var mAdapter: EsriMapStyleAdapter? = null
    private var mHereAdapter: EsriMapStyleAdapter? = null
    private var isMapClickEnable = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapStyle =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))

        mViewModel.setEsriMapListData(requireContext())
        mViewModel.setHereMapListData(requireContext())
        if (mapName == resources.getString(R.string.esri)) {
            for (i in 0 until mViewModel.esriList.size) {
                mViewModel.esriList[i].isSelected = mViewModel.esriList[i].mapName == mapStyle
            }
        } else if (mapName == resources.getString(R.string.here)) {
            for (i in 0 until mViewModel.hereList.size) {
                mViewModel.hereList[i].isSelected = mViewModel.hereList[i].mapName == mapStyle
            }
        }

        setEsriMapStyleAdapter()
        setHereMapStyleAdapter()
        backPress()
        clickListener()
    }

    private fun setHereMapStyleAdapter() {
        val mLayoutManager = GridLayoutManager(this.context, 3)
        mBinding.apply {
            rvHere.layoutManager = mLayoutManager
            mHereAdapter = EsriMapStyleAdapter(
                mViewModel.hereList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            if (isMapClickEnable) {
                                isMapClickEnable = false
                                changeStyle(position, true)
                            }
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvHere.adapter = mHereAdapter
        }
    }

    private fun setEsriMapStyleAdapter() {
        val mLayoutManager = GridLayoutManager(this.context, 3)
        mBinding.apply {
            rvEsri.layoutManager = mLayoutManager
            mAdapter = EsriMapStyleAdapter(
                mViewModel.esriList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            if (isMapClickEnable) {
                                isMapClickEnable = false
                                changeStyle(position, false)
                            }
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvEsri.adapter = mAdapter
        }
    }

    fun changeStyle(position: Int, isHere: Boolean) {
        if (position != -1) {
            if (isHere) {
                mAdapter?.deselectAll()
                mHereAdapter?.singeSelection(position)
                mViewModel.hereList[position].mapName?.let { it1 ->
                    mPreferenceManager.setValue(
                        KEY_MAP_STYLE_NAME,
                        it1
                    )
                }
                mViewModel.hereList[position].mMapName?.let {
                    mViewModel.hereList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    resources.getString(R.string.here)
                )
            } else {
                mHereAdapter?.deselectAll()
                mAdapter?.singeSelection(position)
                mViewModel.esriList[position].mapName?.let { it1 ->
                    mPreferenceManager.setValue(
                        KEY_MAP_STYLE_NAME,
                        it1
                    )
                }
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    resources.getString(R.string.map_esri)
                )
                mViewModel.esriList[position].mMapName?.let {
                    mViewModel.esriList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
            }
        }
        isMapClickEnable = true
    }

    private fun clickListener() {
        mBinding.ivMapStyleBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
