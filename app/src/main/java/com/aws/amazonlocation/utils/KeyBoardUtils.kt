package com.aws.amazonlocation.utils

import android.graphics.Rect
import android.view.View

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
object KeyBoardUtils {
    private var mIsKeyboardOpen = false

    fun attachKeyboardListeners(view: View, mKeyBoardInterface: KeyBoardInterface) {
        view.viewTreeObserver?.addOnGlobalLayoutListener {
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
    }

    interface KeyBoardInterface {
        fun showKeyBoard()
        fun hideKeyBoard()
    }
}
