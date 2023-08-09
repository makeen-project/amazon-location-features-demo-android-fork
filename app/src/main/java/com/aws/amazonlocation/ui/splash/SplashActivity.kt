package com.aws.amazonlocation.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.databinding.ActivitySplashBinding
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.DELAY_1000
import com.aws.amazonlocation.utils.KEY_NEAREST_REGION
import com.aws.amazonlocation.utils.LatencyChecker
import com.aws.amazonlocation.utils.PreferenceManager
import com.aws.amazonlocation.utils.regionList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySplashBinding.inflate(layoutInflater).root)
        GlobalScope.launch(Dispatchers.Default) {
            val preferenceManager = PreferenceManager(applicationContext)
            if (preferenceManager.getValue(KEY_NEAREST_REGION, "") == "") {
                val latencyChecker = LatencyChecker()
                val urls = arrayListOf<String>()
                regionList.forEach {
                    urls.add(String.format(BuildConfig.AWS_NEAREST_REGION_CHECK_URL, it))
                }

                val (fastestUrl, _) = latencyChecker.checkLatencyForUrls(urls)
                regionList.forEach {
                    if (fastestUrl != null) {
                        if (fastestUrl.contains(it)) {
                            preferenceManager.setValue(KEY_NEAREST_REGION, it)
                        }
                    }
                }
            }
            delay(DELAY_1000)
            withContext(Dispatchers.Main) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
