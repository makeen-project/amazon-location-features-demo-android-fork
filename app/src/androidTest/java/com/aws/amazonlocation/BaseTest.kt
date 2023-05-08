package com.aws.amazonlocation

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before

abstract class BaseTest {

    @Before
    fun before() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }
}
