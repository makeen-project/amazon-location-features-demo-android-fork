package com.aws.amazonlocation.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R
import kotlin.math.roundToInt

class DotLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var dotCount = 4
        set(value) {
            field = value
            invalidate()
        }
    private var isMirror = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint()

    private fun setupCanvasComponent() {
        paint.color = ContextCompat.getColor(context, R.color.color_dotted_line)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val dotSpacing = (height * 2 - width * dotCount) / (dotCount + 1)
        val dotCount = (dotCount.toFloat() / 2).roundToInt()
        setupCanvasComponent()
        for (index in 0 until dotCount) {
            val cx = width / 2f
            val cy: Float = if (isMirror) {
                height - width * index - dotSpacing * (index + 1) - width / 2
            } else {
                width * index + dotSpacing * (index + 1) + width / 2
            }
            val radius = width / 2f
            if (index == 0) {
                canvas.drawCircle(cx, cy - 2, radius, paint)
            } else {
                canvas.drawCircle(cx, cy, radius, paint)
            }
        }
    }
}
