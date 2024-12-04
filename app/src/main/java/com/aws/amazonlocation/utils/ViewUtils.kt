package com.aws.amazonlocation.utils

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun showViews(vararg view: View?) {
    view.forEach {
        it?.show()
    }
}

fun hideViews(vararg view: View?) {
    view.forEach {
        it?.hide()
    }
}

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun getKeyboardHeight(activity: Activity, callback: (Int) -> Unit) {
    val rootView = activity.window.decorView.rootView
    rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        private var previousHeight = 0

        override fun onGlobalLayout() {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val visibleHeight = rect.height()
            val keyboardHeight = screenHeight - visibleHeight

            if (keyboardHeight > 0) {
                if (keyboardHeight != previousHeight) {
                    callback(keyboardHeight)
                    previousHeight = keyboardHeight
                }
            } else {
                previousHeight = 0
            }
        }
    })
}
