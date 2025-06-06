package com.aws.amazonlocation.utils.screenshotrule

import org.junit.rules.TestWatcher
import org.junit.runner.Description

class ScreenshotTakingRule : TestWatcher() {

    override fun failed(e: Throwable?, description: Description) {
        val parentFolderPath = "failures/${description.className}"
        takeScreenshot(parentFolderPath = parentFolderPath, screenShotName = description.methodName)
    }
}
