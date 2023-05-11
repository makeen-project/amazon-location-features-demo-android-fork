package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.content.res.Configuration
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

    private lateinit var mLayoutManagerEsri: GridLayoutManager
    private lateinit var mLayoutManagerHere: GridLayoutManager
    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var mAdapter: EsriMapStyleAdapter? = null
    private var mHereAdapter: EsriMapStyleAdapter? = null
    private var isMapClickEnable = true
    private var isTablet = false
    private var isLargeTablet = false
    private var columnCount = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isTablet = requireContext().resources.getBoolean(R.bool.is_tablet)
        isLargeTablet = requireContext().resources.getBoolean(R.bool.is_large_tablet)
        if (isTablet) {
            setColumnCount()
        }
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

    private fun init() {
        setColumnCount()
        mLayoutManagerHere.spanCount = columnCount
        mLayoutManagerEsri.spanCount = columnCount
        mHereAdapter?.notifyDataSetChanged()
        mAdapter?.notifyDataSetChanged()
    }

    private fun setColumnCount() {
        if (isLargeTablet) {
            columnCount = if (requireContext().resources.displayMetrics.widthPixels < 1400) {
                2
            } else if (requireContext().resources.displayMetrics.widthPixels < 2000) {
                3
            } else if (requireContext().resources.displayMetrics.widthPixels < 2400) {
                4
            } else {
                5
            }
        } else {
            columnCount = if (requireContext().resources.displayMetrics.widthPixels < 1250) {
                2
            } else if (requireContext().resources.displayMetrics.widthPixels < 1700) {
                3
            } else {
                4
            }
        }
    }

    private fun setHereMapStyleAdapter() {
        mLayoutManagerHere = GridLayoutManager(this.context, columnCount)
        mBinding.apply {
            rvHere.layoutManager = mLayoutManagerHere
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
        mLayoutManagerEsri = GridLayoutManager(this.context, columnCount)
        mBinding.apply {
            rvEsri.layoutManager = mLayoutManagerEsri
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
