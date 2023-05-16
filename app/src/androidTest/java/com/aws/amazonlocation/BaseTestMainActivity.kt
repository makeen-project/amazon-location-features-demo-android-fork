package com.aws.amazonlocation

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.ScreenshotTakingRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain

abstract class BaseTestMainActivity {

    private val defaultPermissions = arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
        WRITE_EXTERNAL_STORAGE,
    )

    private val permission: Array<String>
        get() {
            return enablePermissions().let {
                if (it.isEmpty()) {
                    arrayOf(WRITE_EXTERNAL_STORAGE)
                } else {
                    it.contains(WRITE_EXTERNAL_STORAGE).let { hasWritePermission ->
                        if (hasWritePermission) {
                            it
                        } else {
                            it + WRITE_EXTERNAL_STORAGE
                        }
                    }
                }
            }
        }

    private val instance: BaseTestMainActivity
        get() {
            return getClassInstance()
        }

    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, true, false)

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(instance)

    @get:Rule(order = 1)
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        *permission,
    )

    @JvmField
    @Rule(order = 2)
    val ruleChain: RuleChain = RuleChain
        .outerRule(mActivityRule)
        .around(ScreenshotTakingRule())

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

    open fun enablePermissions(): Array<String> {
        return defaultPermissions
    }

    private fun getClassInstance(): BaseTestMainActivity {
        return this
    }
}
