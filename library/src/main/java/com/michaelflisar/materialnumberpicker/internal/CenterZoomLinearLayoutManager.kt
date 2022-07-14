package com.michaelflisar.materialnumberpicker.internal

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.abs
import kotlin.math.min

internal class CenterZoomLinearLayoutManager(
    private val recyclerView: RecyclerView,
    orientation: Int,
    reverseLayout: Boolean
) : LinearLayoutManager(
    recyclerView.context,
    orientation,
    reverseLayout
) {

    private val mShrinkAmount = 0.5f
    private val mShrinkDistance = 0.9f

    interface ScalableViewHolder {
        fun scaleOnScroll(factor: Float, isCenter: Boolean)
    }

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        scaleChildren()
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler?, state: RecyclerView.State?): Int {
        return if (orientation == VERTICAL) {
            super.scrollVerticallyBy(dy, recycler, state).also { scaleChildren() }
        } else {
            0
        }
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return if (orientation == HORIZONTAL) {
            super.scrollHorizontallyBy(dx, recycler, state).also { scaleChildren() }
        } else {
            0
        }
    }

    private fun scaleChildren() {

        val midpoint = if (orientation == HORIZONTAL) {
            width / 2f
        } else {
            height / 2f
        }

        val itemDatas =
            (0 until childCount).map { ItemData.create(getChildAt(it) as View, it, this) }
        val mostCenteredView = itemDatas.minByOrNull { abs(midpoint - it.centerOffset) }!!

        val d1 = mShrinkDistance * midpoint
        itemDatas.forEach {
            val d = min(d1, abs(midpoint - (it.decoration) / 2f))
            val scale = 1f - mShrinkAmount * d / d1
            val vh = recyclerView.getChildViewHolder(it.child) as ScalableViewHolder?
            vh?.scaleOnScroll(scale, it.index == mostCenteredView.index)
        }
    }

    private class ItemData(
        val child: View,
        val index: Int,
        val decoration: Int,
        val centerOffset: Int
    ) {
        companion object {
            fun create(child: View, index: Int, lm: CenterZoomLinearLayoutManager): ItemData {
                val decoration = if (lm.orientation == HORIZONTAL) {
                    lm.getDecoratedRight(child) + lm.getDecoratedLeft(child)
                } else {
                    lm.getDecoratedTop(child) + lm.getDecoratedBottom(child)
                }
                val centerOffset = if (lm.orientation == HORIZONTAL) {
                    lm.getDecoratedLeft(child) + child.width / 2
                } else {
                    lm.getDecoratedTop(child) + child.height / 2
                }
                return ItemData(child, index, decoration, centerOffset)
            }
        }
    }
}