package com.aws.amazonlocation.ui.main.hosted_ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.AUTHORIZATION_CODE
import com.aws.amazonlocation.utils.KEY_CODE
import com.aws.amazonlocation.utils.KEY_METHOD
import com.aws.amazonlocation.utils.SIGN_IN
import com.aws.amazonlocation.utils.SIGN_OUT

class HostedUIRedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        intent?.data?.let { uri ->
            if (uri.host == SIGN_OUT) {
                val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(KEY_METHOD, SIGN_OUT)
                }
                startActivity(mainActivityIntent)
                finish()
            } else if (uri.host == SIGN_IN) {
                val authorizationCode = uri.getQueryParameter(KEY_CODE)
                val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(KEY_METHOD, SIGN_IN)
                    putExtra(AUTHORIZATION_CODE, authorizationCode)
                }
                startActivity(mainActivityIntent)
                finish()
            }
        }
        finish()
    }
}
