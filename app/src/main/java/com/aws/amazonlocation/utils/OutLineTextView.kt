package com.aws.amazonlocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.aws.amazonlocation.R

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class OutLineTextView : FrameLayout {

    private lateinit var mTvMapLabelShadow: AppCompatTextView
    private lateinit var mTvMapLabel: AppCompatTextView

    private var mTextSize: Float = 0f
    private var mTextColor: Int = 0
    private var mMaxLine = 0
    private var mStrokeColor: Int = 0
    private var mStrokeWidth: Float = 0.toFloat()

    private var mDefaultTextSize = 0f
    private var mDefaultMaxLine = 0
    private val mDefaultStrokeWidth = 0F
    private var mDefaultStrokeColor = ContextCompat.getColor(context, R.color.white)
    private var mDefaultTextColor = ContextCompat.getColor(context, R.color.white)

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun initView(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val styleAtr = context.obtainStyledAttributes(attrs, R.styleable.outlineAttrs)
            mTextSize =
                styleAtr.getFloat(R.styleable.outlineAttrs_outlineTextSize, mDefaultTextSize)
            mMaxLine =
                styleAtr.getInt(R.styleable.outlineAttrs_outlineMaxLine, mDefaultMaxLine)
            mTextColor =
                styleAtr.getColor(R.styleable.outlineAttrs_outlineTextColor, mDefaultTextColor)
            mStrokeColor =
                styleAtr.getColor(R.styleable.outlineAttrs_outlineColor, mDefaultStrokeColor)
            mStrokeWidth = styleAtr.getFloat(
                R.styleable.outlineAttrs_outlineWidth,
                mDefaultStrokeWidth
            )
            styleAtr.recycle()
        } else {
            mStrokeColor = mDefaultStrokeColor
            mTextColor = mDefaultTextColor
            mStrokeWidth = mDefaultStrokeWidth
            mMaxLine = mDefaultMaxLine
            mTextSize = mDefaultTextSize
        }

        mTvMapLabelShadow = AppCompatTextView(context)
        TextViewCompat.setTextAppearance(mTvMapLabelShadow, R.style.SP11WhiteRegularTextStyle)

        mTvMapLabel = AppCompatTextView(context)
        TextViewCompat.setTextAppearance(mTvMapLabel, R.style.SP11MediumBlackRegularTextStyle)
        mTvMapLabel.width = resources.getDimension(R.dimen.dp_132).toInt()
        mTvMapLabelShadow.width = resources.getDimension(R.dimen.dp_132).toInt()

        mTvMapLabelShadow.ellipsize = TextUtils.TruncateAt.END
        mTvMapLabel.ellipsize = TextUtils.TruncateAt.END

        setStrokeColor(mStrokeColor)
        setTextColor(mTextColor)
        setStrokeWidth(mStrokeWidth)
        maxLine(mMaxLine)
        setTextSize(mTextSize)

        addView(mTvMapLabelShadow)
        addView(mTvMapLabel)
    }

    private fun setStrokeColor(color: Int) {
        mStrokeColor = color
        mTvMapLabelShadow.setTextColor(mStrokeColor)
    }

    private fun setTextColor(color: Int) {
        mTextColor = color
        mTvMapLabel.setTextColor(color)
    }

    private fun setStrokeWidth(width: Float) {
        mStrokeWidth = width
        mTvMapLabelShadow.paint.strokeWidth = mStrokeWidth
        mTvMapLabelShadow.paint.style = Paint.Style.STROKE
    }

    private fun maxLine(line: Int) {
        mMaxLine = line
        mTvMapLabelShadow.maxLines = mMaxLine
        mTvMapLabel.maxLines = mMaxLine
    }

    private fun setTextSize(tSize: Float) {
        this.mTextSize = tSize
        mTvMapLabelShadow.textSize = this.mTextSize
        mTvMapLabel.textSize = this.mTextSize
    }

    fun setText(text: String) {
        mTvMapLabelShadow.text = text
        mTvMapLabel.text = text
    }
}
