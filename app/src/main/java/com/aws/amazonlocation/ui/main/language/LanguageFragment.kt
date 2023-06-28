package com.aws.amazonlocation.ui.main.language // ktlint-disable package-name

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.findNavController
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentLanguageBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.LANGUAGE_CODE_BR_PT
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_CN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_CH_TW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ENGLISH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_FRENCH
import com.aws.amazonlocation.utils.LANGUAGE_CODE_GERMAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HEBREW
import com.aws.amazonlocation.utils.LANGUAGE_CODE_HINDI
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ITALIAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_JAPANESE
import com.aws.amazonlocation.utils.LANGUAGE_CODE_KOREAN
import com.aws.amazonlocation.utils.LANGUAGE_CODE_SPANISH
import com.aws.amazonlocation.utils.getLanguageCode
import com.aws.amazonlocation.utils.isGrabMapEnable
import com.aws.amazonlocation.utils.isRunningTest

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
        val languageCode = getLanguageCode()
        val isRtl = languageCode == LANGUAGE_CODE_ARABIC || languageCode == LANGUAGE_CODE_HEBREW
        setSelectedLanguage(languageCode, isRtl)
        mBinding.apply {
            rgLanguage.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rb_german -> {}
                    R.id.rb_spanish -> {}
                    R.id.rb_english -> {
                        changeAppLanguage(LANGUAGE_CODE_ENGLISH)
                    }
                    R.id.rb_french -> {}
                    R.id.rb_italian -> {}
                    R.id.rb_brazilian_portuguese -> {}
                    R.id.rb_simplified_chinese -> {}
                    R.id.rb_traditional_chinese -> {}
                    R.id.rb_japanese -> {}
                    R.id.rb_korean -> {}
                    R.id.rb_arabic -> {
                        changeAppLanguage(LANGUAGE_CODE_ARABIC)
                    }
                    R.id.rb_hebrew -> {}
                    R.id.rb_hindi -> {
                        changeAppLanguage(LANGUAGE_CODE_HINDI)
                    }
                }
                if (!isRunningTest) {
                    restart(requireContext())
                }
            }
            ivLanguage?.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun setSelectedLanguage(languageCode: String?, isRtl: Boolean) {
        mBinding.apply {
            when (languageCode) {
                LANGUAGE_CODE_GERMAN -> {
                    setRadioButtonIcon(rbGerman, isRtl)
                }
                LANGUAGE_CODE_SPANISH -> {
                    setRadioButtonIcon(rbSpanish, isRtl)
                }
                LANGUAGE_CODE_ENGLISH -> {
                    setRadioButtonIcon(rbEnglish, isRtl)
                }
                LANGUAGE_CODE_FRENCH -> {
                    setRadioButtonIcon(rbFrench, isRtl)
                }
                LANGUAGE_CODE_ITALIAN -> {
                    setRadioButtonIcon(rbItalian, isRtl)
                }
                LANGUAGE_CODE_BR_PT -> {
                    setRadioButtonIcon(rbBrazilianPortuguese, isRtl)
                }
                LANGUAGE_CODE_CH_CN -> {
                    setRadioButtonIcon(rbSimplifiedChinese, isRtl)
                }
                LANGUAGE_CODE_CH_TW -> {
                    setRadioButtonIcon(rbTraditionalChinese, isRtl)
                }
                LANGUAGE_CODE_JAPANESE -> {
                    setRadioButtonIcon(rbJapanese, isRtl)
                }
                LANGUAGE_CODE_KOREAN -> {
                    setRadioButtonIcon(rbKorean, isRtl)
                }
                LANGUAGE_CODE_ARABIC -> {
                    setRadioButtonIcon(rbArabic, isRtl)
                }
                LANGUAGE_CODE_HEBREW -> {
                    setRadioButtonIcon(rbHebrew, isRtl)
                }
                LANGUAGE_CODE_HINDI -> {
                    setRadioButtonIcon(rbHindi, isRtl)
                }
                else -> {
                    setRadioButtonIcon(rbEnglish, isRtl)
                }
            }
        }
    }

    private fun setRadioButtonIcon(
        rb: AppCompatRadioButton,
        isRtl: Boolean
    ) {
        rb.isChecked = true
        if (isRtl) {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.icon_checkmark,
                0,
                0,
                0
            )
        } else {
            rb.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.icon_checkmark,
                0
            )
        }
    }

    private fun changeAppLanguage(languageCode: String) {
        if (!isRunningTest) {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
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
