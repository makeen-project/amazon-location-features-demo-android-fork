package com.aws.amazonlocation.utils

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
object KeyBoardUtils {
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var mIsKeyboardOpen = false

    fun attachKeyboardListeners(view: View, mKeyBoardInterface: KeyBoardInterface) {
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int = view.rootView.height - (r.bottom - r.top)
            if (heightDiff > KEY_BOARD_HEIGHT) {
                if (!mIsKeyboardOpen) {
                    mKeyBoardInterface.showKeyBoard()
                    mIsKeyboardOpen = true
                }
            } else {
                if (mIsKeyboardOpen) {
                    mKeyBoardInterface.hideKeyBoard()
                    mIsKeyboardOpen = false
                }
            }
        }

        view.viewTreeObserver?.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun detachKeyboardListeners(view: View) {
        mIsKeyboardOpen = false
        view.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
    }
    interface KeyBoardInterface {
        fun showKeyBoard()
        fun hideKeyBoard()
    }
}
