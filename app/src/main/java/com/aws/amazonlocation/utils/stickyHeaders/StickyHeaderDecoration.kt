package com.aws.amazonlocation.utils.stickyHeaders

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

private const val NO_HEADER_ID = -1L
private const val ITEM_AT_TOP = 0

/**
 * {@link RecyclerView.ItemDecoration} class to add Sticky Headers to a RecyclerView.
 * Adapter must implement the {@link StickyHeaderAdapter} interface and an instance of this class should
 * be added to the RecyclerView using RecyclerView#addItemDecoration
 */
class StickyHeaderDecoration<T : RecyclerView.ViewHolder>(
    private val stickyHeaderAdapter: StickyHeaderAdapter<T>,
    private val onStickyHeaderChangedListener: OnStickyHeaderChangedListener?,
    private val spannedRowCount: Int = 1
) : RecyclerView.ItemDecoration() {

    private val headerCache = mutableMapOf<Long, RecyclerView.ViewHolder>()
    private var currentHeaderId: Long = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)

        val headerHeight =
            if (position != RecyclerView.NO_POSITION && hasHeader(position) && showHeaderAboveItem(
                    position
                )
            ) {
                getHeaderItemHeight(position, parent)
            } else {
                0
            }

        outRect.set(0, headerHeight, 0, 0)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val count = parent.childCount
        var previousHeaderId = -1L

        for (layoutPos in 0..count) {
            val child = parent.getChildAt(layoutPos)
            val adapterPos = parent.getChildAdapterPosition(child)

            if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
                val headerId = stickyHeaderAdapter.getHeaderId(adapterPos)

                if (headerId != previousHeaderId) {
                    previousHeaderId = headerId
                    val header = getHeader(adapterPos, parent).itemView
                    canvas.withSave {
                        val left = child?.left?.toFloat() ?: 0f
                        val top = calculateTopOffset(parent, child, header, adapterPos, layoutPos)
                        canvas.translate(left, top)

                        header.translationX = left
                        header.translationY = top
                        header.draw(canvas)
                    }
                }
            }
        }
    }

    private fun hasHeader(position: Int) = stickyHeaderAdapter.getHeaderId(position) != NO_HEADER_ID

    private fun showHeaderAboveItem(position: Int): Boolean {
        val doesPreviousItemHeaderMatch =
            stickyHeaderAdapter.getHeaderId(position - 1) != stickyHeaderAdapter.getHeaderId(
                position
            )
        return position < spannedRowCount || position == 0 || doesPreviousItemHeaderMatch
    }

    private fun getHeaderItemHeight(position: Int, parent: RecyclerView): Int =
        getHeader(position, parent).itemView.height

    private fun getHeader(position: Int, parent: RecyclerView): RecyclerView.ViewHolder {
        val key = stickyHeaderAdapter.getHeaderId(position)
        return headerCache[key] ?: createHeaderViewHolder(
            position,
            parent
        ).also { headerCache[key] = it }
    }

    private fun createHeaderViewHolder(
        position: Int,
        parent: RecyclerView
    ): RecyclerView.ViewHolder {
        val holder = stickyHeaderAdapter.onCreateHeaderViewHolder(parent)
        val header = holder.itemView

        stickyHeaderAdapter.onBindHeaderViewHolder(holder, position)

        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingStart + parent.paddingEnd,
            header.layoutParams.width
        )
        val childHeight = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            header.layoutParams.height
        )

        header.measure(childWidth, childHeight)
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)

        return holder
    }

    private fun calculateTopOffset(
        parent: RecyclerView,
        child: View?,
        header: View?,
        adapterPos: Int,
        layoutPos: Int
    ): Float {
        val headerHeight = header?.height ?: 0
        val childY = child?.y ?: 0f

        updateCurrentHeaderId(layoutPos, adapterPos)

        val nextChildPosition = findNextChildHeaderPosition(parent)

        if (layoutPos == ITEM_AT_TOP && nextChildPosition != RecyclerView.NO_POSITION) {
            val nextChild = parent.getChildAt(nextChildPosition)
            val offset = (nextChild?.y ?: 0f) - (
                headerHeight + getHeaderItemHeight(
                    nextChildPosition,
                    parent
                )
                )
            if (offset < 0) return offset
        }

        return 0f.coerceAtLeast(childY - headerHeight)
    }

    private fun updateCurrentHeaderId(layoutPosition: Int, adapterPosition: Int) {
        val newHeaderId = stickyHeaderAdapter.getHeaderId(adapterPosition)
        if (layoutPosition == ITEM_AT_TOP && currentHeaderId != newHeaderId) {
            currentHeaderId = newHeaderId
            onStickyHeaderChangedListener?.onStickyHeaderChanged(currentHeaderId)
        }
    }

    private fun findNextChildHeaderPosition(parent: RecyclerView): Int {
        val count = parent.childCount

        for (i in 1..count) {
            val nextChildAdapterPosition = parent.getChildAdapterPosition(parent.getChildAt(i))
            if (nextChildAdapterPosition != RecyclerView.NO_POSITION && hasHeader(
                    nextChildAdapterPosition
                )
            ) {
                val nextId = stickyHeaderAdapter.getHeaderId(nextChildAdapterPosition)
                if (nextId != currentHeaderId) {
                    return i
                }
            }
        }

        return RecyclerView.NO_POSITION
    }

    /**
     * Wrap the specified [block] in calls to [Canvas.save]
     * and [Canvas.restoreToCount].
     */
    private inline fun Canvas.withSave(block: Canvas.() -> Unit) {
        val checkpoint = save()
        try {
            block()
        } finally {
            restoreToCount(checkpoint)
        }
    }
}
