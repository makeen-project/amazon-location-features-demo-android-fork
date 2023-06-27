package com.aws.amazonlocation.ui.main.language // ktlint-disable package-name

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentLanguageBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.isGrabMapEnable
import java.util.Locale
import kotlinx.serialization.json.Json.Default.configuration

class LanguageFragment : BaseFragment() {

    private lateinit var mBinding: FragmentLanguageBinding
    private var isGrabMapEnable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentLanguageBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isGrabMapEnable = isGrabMapEnable(mPreferenceManager)
        backPress()
        init()
    }

    private fun init() {
        mBinding.apply {
            rgLanguage.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_german -> {}
                    R.id.rb_spanish -> {}
                    R.id.rb_english -> {}
                    R.id.rb_french -> {}
                    R.id.rb_italian -> {}
                    R.id.rb_brazilian_portuguese -> {}
                    R.id.rb_simplified_chinese -> {}
                    R.id.rb_traditional_chinese -> {}
                    R.id.rb_japanese -> {}
                    R.id.rb_korean -> {}
                    R.id.rb_arabic -> {
                        changeAppLanguage(requireContext(), "ar")
                        restart(requireContext())
                    }
                    R.id.rb_hebrew -> {}
                    R.id.rb_hindi -> {}
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun changeAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config: Configuration = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun restart(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
