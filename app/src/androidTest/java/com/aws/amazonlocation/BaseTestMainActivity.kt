package com.aws.amazonlocation

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.retry_rule.RetryTestRule
import com.aws.amazonlocation.utils.screenshot_rule.ScreenshotTakingRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain

abstract class BaseTestMainActivity {

    private val instance: BaseTestMainActivity
        get() {
            return getClassInstance()
        }

    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java,
        true,
        false
    )

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(instance)

    @JvmField
    @Rule(order = 1)
    val ruleChain: RuleChain = RuleChain
        .outerRule(mActivityRule)
        .around(ScreenshotTakingRule())

    @get:Rule(order = 2)
    val retryRule = RetryTestRule()

    @Before
    open fun before() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        mActivityRule.launchActivity(null)
    }

    @After
    open fun after() {
        try {
            mActivityRule.finishActivity()
        } catch (_: Exception) {
            // do nothing
        }
    }

    private fun getClassInstance(): BaseTestMainActivity {
        return this
    }
}
