package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.MapStyleRestartInterface
import com.aws.amazonlocation.utils.RESTART_DELAY
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isInternetAvailable
import com.aws.amazonlocation.utils.isRunningTest
import com.aws.amazonlocation.utils.restartAppMapStyleDialog
import com.aws.amazonlocation.utils.restartApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil

class MapStyleFragment : BaseFragment() {

    private lateinit var mLayoutManagerEsri: GridLayoutManager
    private lateinit var mLayoutManagerHere: GridLayoutManager
    private lateinit var mLayoutManagerGrab: GridLayoutManager
    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var mAdapter: EsriMapStyleAdapter? = null
    private var mHereAdapter: EsriMapStyleAdapter? = null
    private var mGrabAdapter: EsriMapStyleAdapter? = null
    private var isMapClickEnable = true
    private var isTablet = false
    private var isLargeTablet = false
    private var columnCount = 3
    private var isRestartNeeded = false
    private var isGrabMapEnable = false

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
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        isGrabMapEnable = isGrabMapEnable(mPreferenceManager)
        isLargeTablet = requireContext().resources.getBoolean(R.bool.is_large_tablet)
        if (isTablet) {
            setColumnCount()
        }
        val mapStyle =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))

        mViewModel.setEsriMapListData(requireContext())
        mViewModel.setHereMapListData(requireContext())
        if (isGrabMapEnable) {
            mViewModel.setGrabMapListData(requireContext())
        }
        when (mapName) {
            resources.getString(R.string.esri) -> {
                for (i in 0 until mViewModel.esriList.size) {
                    mViewModel.esriList[i].isSelected = mViewModel.esriList[i].mapName == mapStyle
                }
            }
            resources.getString(R.string.here) -> {
                for (i in 0 until mViewModel.hereList.size) {
                    mViewModel.hereList[i].isSelected = mViewModel.hereList[i].mapName == mapStyle
                }
            }
            resources.getString(R.string.grab) -> {
                for (i in 0 until mViewModel.grabList.size) {
                    mViewModel.grabList[i].isSelected = mViewModel.grabList[i].mapName == mapStyle
                }
            }
        }

        setEsriMapStyleAdapter()
        setHereMapStyleAdapter()
        if (isGrabMapEnable) {
            setGrabMapStyleAdapter()
        }
        backPress()
        clickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        setColumnCount()
        mLayoutManagerHere.spanCount = columnCount
        mLayoutManagerEsri.spanCount = columnCount
        mHereAdapter?.notifyDataSetChanged()
        mAdapter?.notifyDataSetChanged()
        if (isGrabMapEnable) {
            mLayoutManagerGrab.spanCount = columnCount
            mGrabAdapter?.notifyDataSetChanged()
        }
    }

    private fun setColumnCount() {
        columnCount = calculateColumnCount()
    }

    private fun calculateColumnCount(): Int {
        val calculatedColumn: Double = (requireContext().resources.displayMetrics.widthPixels).toDouble() / 650
        return ceil(calculatedColumn).toInt()
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
                                checkRestartNeeded()
                                if (isRestartNeeded) {
                                    showRestartDialog(
                                        isHere = true,
                                        isGrab = false,
                                        position = position
                                    )
                                } else {
                                    isMapClickEnable = false
                                    changeStyle(position, isHere = true, isGrab = false)
                                }
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
                                checkRestartNeeded()
                                if (isRestartNeeded) {
                                    showRestartDialog(
                                        isHere = false,
                                        isGrab = false,
                                        position = position
                                    )
                                } else {
                                    isMapClickEnable = false
                                    changeStyle(position, isHere = false, isGrab = false)
                                }
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

    private fun checkRestartNeeded() {
        when (
            mPreferenceManager.getValue(
                KEY_MAP_NAME,
                getString(R.string.map_esri)
            )
        ) {
            getString(R.string.esri) -> {
                isRestartNeeded = false
            }
            getString(R.string.here) -> {
                isRestartNeeded = false
            }
            getString(R.string.grab) -> {
                isRestartNeeded = true
            }
        }
    }

    private fun setGrabMapStyleAdapter() {
        mLayoutManagerGrab = GridLayoutManager(this.context, columnCount)
        mBinding.apply {
            rvGrab.layoutManager = mLayoutManagerGrab
            mGrabAdapter = EsriMapStyleAdapter(
                mViewModel.grabList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            when (
                                mPreferenceManager.getValue(
                                    KEY_MAP_NAME,
                                    getString(R.string.map_esri)
                                )
                            ) {
                                getString(R.string.esri) -> {
                                    isRestartNeeded = true
                                }
                                getString(R.string.here) -> {
                                    isRestartNeeded = true
                                }
                                getString(R.string.grab) -> {
                                    isRestartNeeded = false
                                }
                            }
                            if (isRestartNeeded) {
                                showRestartDialog(isHere = false, isGrab = true, position = position)
                            } else {
                                changeStyle(position, isHere = false, isGrab = true)
                            }
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvGrab.adapter = mGrabAdapter
        }
    }

    private fun showRestartDialog(isHere: Boolean, isGrab: Boolean, position: Int) {
        activity?.restartAppMapStyleDialog(object : MapStyleRestartInterface {
            override fun onOkClick(dialog: DialogInterface) {
                if (isGrab) {
                    saveGrabData(position)
                } else {
                    if (isHere) {
                        saveHereData(position)
                    } else {
                        saveEsriData(position)
                    }
                }
                lifecycleScope.launch {
                    if (!isRunningTest) {
                        delay(RESTART_DELAY) // Need delay for preference manager to set default config before restarting
                        activity?.restartApplication()
                    }
                }
            }
        })
    }

    fun changeStyle(position: Int, isHere: Boolean, isGrab: Boolean) {
        if (position != -1) {
            if (isGrab) {
                mAdapter?.deselectAll()
                mHereAdapter?.deselectAll()
                mGrabAdapter?.singeSelection(position)
                saveGrabData(position)
                mViewModel.grabList[position].mMapName?.let {
                    mViewModel.grabList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
                return
            }
            if (isHere) {
                mAdapter?.deselectAll()
                mGrabAdapter?.deselectAll()
                mHereAdapter?.singeSelection(position)
                saveHereData(position)
                mViewModel.hereList[position].mMapName?.let {
                    mViewModel.hereList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
            } else {
                mHereAdapter?.deselectAll()
                mGrabAdapter?.deselectAll()
                mAdapter?.singeSelection(position)
                saveEsriData(position)
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

    private fun saveEsriData(position: Int) {
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
    }

    private fun saveHereData(position: Int) {
        mViewModel.hereList[position].mapName?.let { it1 ->
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                it1
            )
        }
        mPreferenceManager.setValue(
            KEY_MAP_NAME,
            resources.getString(R.string.here)
        )
    }

    private fun saveGrabData(position: Int) {
        mViewModel.grabList[position].mapName?.let { it1 ->
            mPreferenceManager.setValue(
                KEY_MAP_STYLE_NAME,
                it1
            )
        }
        mPreferenceManager.setValue(
            KEY_MAP_NAME,
            resources.getString(R.string.grab)
        )
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
