package com.aws.amazonlocation.ui.main.map_style

import android.content.Context
import androidx.lifecycle.ViewModel
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.LanguageData
import com.aws.amazonlocation.data.response.MapStyleData
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.data.response.PoliticalData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapStyleViewModel @Inject constructor() : ViewModel() {

    var mStyleList = ArrayList<MapStyleData>()
    var mPoliticalData = ArrayList<PoliticalData>()
    var mPoliticalSearchData = ArrayList<PoliticalData>()
    var mMapLanguageData = ArrayList<LanguageData>()

    fun setMapListData(context: Context) {
        val items =
            arrayListOf(
                MapStyleInnerData(
                    context.getString(R.string.map_standard),
                    false,
                    R.drawable.standard_light,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_monochrome),
                    false,
                    R.drawable.monochrome,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_hybrid),
                    false,
                    R.drawable.hybrid,
                ),
                MapStyleInnerData(
                    context.getString(R.string.map_satellite),
                    false,
                    R.drawable.satellite,
                ),
            )
        mStyleList.clear()

        mStyleList =
            arrayListOf(
                MapStyleData(
                    styleNameDisplay = "",
                    isSelected = false,
                    mapInnerData = items,
                ),
            )
    }

    fun setPoliticalListData(context: Context) {
        val item = arrayListOf(
            PoliticalData(
                countryName = context.getString(R.string.label_no_political_view),
                description = "",
                countryCode = "",
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_arg),
                description = context.getString(R.string.description_arg),
                countryCode = context.getString(R.string.flag_arg),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_egy),
                description = context.getString(R.string.description_egy),
                countryCode = context.getString(R.string.flag_egy),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ind),
                description = context.getString(R.string.description_ind),
                countryCode = context.getString(R.string.flag_ind),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ken),
                description = context.getString(R.string.description_ken),
                countryCode = context.getString(R.string.flag_ken),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_mar),
                description = context.getString(R.string.description_mar),
                countryCode = context.getString(R.string.flag_mar),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_rus),
                description = context.getString(R.string.description_rus),
                countryCode = context.getString(R.string.flag_rus),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sdn),
                description = context.getString(R.string.description_sdn),
                countryCode = context.getString(R.string.flag_sdn),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_srb),
                description = context.getString(R.string.description_srb),
                countryCode = context.getString(R.string.flag_srb),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_sur),
                description = context.getString(R.string.description_sur),
                countryCode = context.getString(R.string.flag_sur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_syr),
                description = context.getString(R.string.description_syr),
                countryCode = context.getString(R.string.flag_syr),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tur),
                description = context.getString(R.string.description_tur),
                countryCode = context.getString(R.string.flag_tur),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_tza),
                description = context.getString(R.string.description_tza),
                countryCode = context.getString(R.string.flag_tza),
            ),
            PoliticalData(
                countryName = context.getString(R.string.label_ury),
                description = context.getString(R.string.description_ury),
                countryCode = context.getString(R.string.flag_ury),
            )
        )
        mPoliticalData.addAll(item)

        mPoliticalSearchData.addAll(item)
    }

    fun searchPoliticalData(query: String): ArrayList<PoliticalData> {
        return ArrayList(mPoliticalSearchData.filter {
            it.countryName.contains(query, ignoreCase = true)
        })
    }

    fun setMapLanguageData(context: Context) {
        mMapLanguageData.clear()

        mMapLanguageData = arrayListOf(
            LanguageData(value = context.getString(R.string.label_no_map_language), label = context.getString(R.string.label_no_map_language), isSelected = false),
            LanguageData(value = "en", label = "English", isSelected = false),
            LanguageData(value = "ar", label = "العربية", isSelected = false),
            LanguageData(value = "as", label = "অসমীয়া", isSelected = false),
            LanguageData(value = "az", label = "Azərbaycan dili", isSelected = false),
            LanguageData(value = "be", label = "Беларуская", isSelected = false),
            LanguageData(value = "bg", label = "Български", isSelected = false),
            LanguageData(value = "bn", label = "বাংলা", isSelected = false),
            LanguageData(value = "bs", label = "Bosanski", isSelected = false),
            LanguageData(value = "ca", label = "Català", isSelected = false),
            LanguageData(value = "cs", label = "Čeština", isSelected = false),
            LanguageData(value = "cy", label = "Cymraeg", isSelected = false),
            LanguageData(value = "da", label = "Dansk", isSelected = false),
            LanguageData(value = "de", label = "Deutsch", isSelected = false),
            LanguageData(value = "el", label = "Ελληνικά", isSelected = false),
            LanguageData(value = "es", label = "Español", isSelected = false),
            LanguageData(value = "et", label = "Eesti", isSelected = false),
            LanguageData(value = "eu", label = "Euskara", isSelected = false),
            LanguageData(value = "fi", label = "Suomi", isSelected = false),
            LanguageData(value = "fo", label = "Føroyskt", isSelected = false),
            LanguageData(value = "fr", label = "Français", isSelected = false),
            LanguageData(value = "ga", label = "Gaeilge", isSelected = false),
            LanguageData(value = "gl", label = "Galego", isSelected = false),
            LanguageData(value = "gn", label = "Avañe'ẽ", isSelected = false),
            LanguageData(value = "gu", label = "ગુજરાતી", isSelected = false),
            LanguageData(value = "he", label = "עברית", isSelected = false),
            LanguageData(value = "hi", label = "हिन्दी", isSelected = false),
            LanguageData(value = "hr", label = "Hrvatski", isSelected = false),
            LanguageData(value = "hu", label = "Magyar", isSelected = false),
            LanguageData(value = "hy", label = "Հայերեն", isSelected = false),
            LanguageData(value = "id", label = "Bahasa Indonesia", isSelected = false),
            LanguageData(value = "is", label = "Íslenska", isSelected = false),
            LanguageData(value = "it", label = "Italiano", isSelected = false),
            LanguageData(value = "ja", label = "日本語", isSelected = false),
            LanguageData(value = "ka", label = "ქართული", isSelected = false),
            LanguageData(value = "kk", label = "Қазақ тілі", isSelected = false),
            LanguageData(value = "km", label = "ខ្មែរ", isSelected = false),
            LanguageData(value = "kn", label = "ಕನ್ನಡ", isSelected = false),
            LanguageData(value = "ko", label = "한국어", isSelected = false),
            LanguageData(value = "ky", label = "Кыргызча", isSelected = false),
            LanguageData(value = "lt", label = "Lietuvių", isSelected = false),
            LanguageData(value = "lv", label = "Latviešu", isSelected = false),
            LanguageData(value = "mk", label = "Македонски", isSelected = false),
            LanguageData(value = "ml", label = "മലയാളം", isSelected = false),
            LanguageData(value = "mr", label = "मराठी", isSelected = false),
            LanguageData(value = "ms", label = "Bahasa Melayu", isSelected = false),
            LanguageData(value = "mt", label = "Malti", isSelected = false),
            LanguageData(value = "my", label = "မြန်မာစာ", isSelected = false),
            LanguageData(value = "nl", label = "Nederlands", isSelected = false),
            LanguageData(value = "no", label = "Norsk", isSelected = false),
            LanguageData(value = "or", label = "ଓଡ଼ିଆ", isSelected = false),
            LanguageData(value = "pa", label = "ਪੰਜਾਬੀ", isSelected = false),
            LanguageData(value = "pl", label = "Polski", isSelected = false),
            LanguageData(value = "pt", label = "Português", isSelected = false),
            LanguageData(value = "ro", label = "Română", isSelected = false),
            LanguageData(value = "ru", label = "Русский", isSelected = false),
            LanguageData(value = "sk", label = "Slovenčina", isSelected = false),
            LanguageData(value = "sl", label = "Slovenščina", isSelected = false),
            LanguageData(value = "sq", label = "Shqip", isSelected = false),
            LanguageData(value = "sr", label = "Српски", isSelected = false),
            LanguageData(value = "sv", label = "Svenska", isSelected = false),
            LanguageData(value = "ta", label = "தமிழ்", isSelected = false),
            LanguageData(value = "te", label = "తెలుగు", isSelected = false),
            LanguageData(value = "th", label = "ไทย", isSelected = false),
            LanguageData(value = "tr", label = "Türkçe", isSelected = false),
            LanguageData(value = "uk", label = "Українська", isSelected = false),
            LanguageData(value = "uz", label = "Oʻzbek", isSelected = false),
            LanguageData(value = "vi", label = "Tiếng Việt", isSelected = false),
            LanguageData(value = "zh", label = "简体中文", isSelected = false),
            LanguageData(value = "zh-Hant", label = "繁體中文", isSelected = false)
        )
    }
}
