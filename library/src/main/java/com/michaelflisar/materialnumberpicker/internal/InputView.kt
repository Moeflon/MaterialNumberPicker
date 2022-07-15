package com.michaelflisar.materialnumberpicker.internal

import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup

internal sealed class InputView<T, Picker> where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

    // Update UI (EditText, RecyclerView)
    abstract fun updateDisplayedValue(
        picker: Picker,
        setup: INumberPickerSetup<T>,
        value: T,
        smooth: Boolean
    )

    //abstract fun getDisplayedValue(): T

    abstract fun clearFocus()

    abstract fun onSetupChanged(picker: Picker, setup: INumberPickerSetup<T>)

    class Input<T, Picker>(
        val editText: EditText
    ) : InputView<T, Picker>() where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        override fun updateDisplayedValue(
            picker: Picker,
            setup: INumberPickerSetup<T>,
            value: T,
            smooth: Boolean
        ) {
            editText.setText(setup.formatter(value))
        }

        override fun clearFocus() {
            editText.clearFocus()
        }

        override fun onSetupChanged(picker: Picker, setup: INumberPickerSetup<T>) {
            val displayValue = setup.formatter(picker.value)
            editText.setText(displayValue)
            ViewUtil.initSubViews<T, Picker>(picker, this, false)
        }

        fun getValueFromInput(setup: INumberPickerSetup<T>, onlyIfFocused: Boolean): T? {
            val input = editText.text.toString()
            return if (!input.isNullOrEmpty() && (!onlyIfFocused || editText.hasFocus())) {
                setup.parser(input)
            } else null
        }
    }

    class Scroller<T, Picker>(
        val picker: Picker,
        val recyclerView: RecyclerView,
        val layoutManager: CenterZoomLinearLayoutManager,
        val helper: SnapHelper,
        val adapter: NumberPickerAdapter<T, Picker>
    ) : InputView<T, Picker>() where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        private var scroll: Scroll = Scroll.None
        private var onLayoutListener: View.OnLayoutChangeListener? = null

        sealed class Scroll {
            object None : Scroll() {
                override fun toString() = "None"
            }
            data class ScrollToPosition(val position: Int, val smooth: Boolean) : Scroll()
            data class Scrolled(val position: Int, val programmatically: Boolean) : Scroll()
        }

        private val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scroll == Scroll.None) {
                        scroll = Scroll.Scrolled(-1, false)
                    }
                    onCheckScroll()
                }
            }
        }

        init {
            recyclerView.addOnScrollListener(scrollListener)
        }

        override fun updateDisplayedValue(
            picker: Picker,
            setup: INumberPickerSetup<T>,
            value: T,
            smooth: Boolean
        ) {
            cancelPendingUpdates()

            val index = adapter.getItemIndex(value)
            scroll = Scroll.ScrollToPosition(index, smooth)

            if (recyclerView.isLaidOut) {
                onCheckScroll()
            } else {
                doOnNextLayout {
                    onCheckScroll()
                }
            }
        }

        override fun clearFocus() {
            // nothing to do
        }

        override fun onSetupChanged(picker: Picker, setup: INumberPickerSetup<T>) {
            cancelPendingUpdates()
            ViewUtil.initScrollViews(picker, this)
        }

        private fun cancelPendingUpdates() {
            L.d(
                "Scroller",
                picker
            ) { "Cancel pending updates: $scroll | onLayoutListener = $onLayoutListener" }
            onLayoutListener?.let { recyclerView.cancelOnNextLayout(it) }
            onLayoutListener = null
            scroll = Scroll.None
            recyclerView.stopScroll()
        }

        private fun doOnNextLayout(function: () -> Unit) {
            onLayoutListener?.let { recyclerView.cancelOnNextLayout(it) }
            onLayoutListener = recyclerView.doOnNextLayout {
                function()
            }
        }

        // ------------
        // Scroll Event
        // ------------

        private fun onCheckScroll() {
            val name =
                picker.context.resources.getResourceName(picker.id).substringAfterLast(":id/")

            val scroll = scroll

            L.d("Scroller", picker) {
                if (scroll is Scroll.ScrollToPosition) {
                    "Check Scroll: $scroll | pos = ${scroll.position} | item = ${
                        adapter.getItem(
                            scroll.position
                        )
                    }"
                } else "Check Scroll: $scroll"
            }


            when (scroll) {
                Scroll.None -> {
                    // nothing to do
                }
                is Scroll.ScrollToPosition -> {
                    // programmatically scrolled => check if snap is correct and only finish
                    // event if it is
                    val vh = recyclerView.findViewHolderForLayoutPosition(scroll.position)
                    if (vh != null) {
                        val offset =
                            helper.calculateDistanceToFinalSnap(layoutManager, vh.itemView)!!
                        if (offset[0] != 0 || offset[1] != 0) {
                            if (scroll.smooth) {
                                L.d(
                                    "Scroller",
                                    picker
                                ) { "Smooth scroll with snap helper... (${offset[0]}, ${offset[1]})" }
                                //layoutManager.smoothScrollToCenterPosition(recyclerView, scroll.position)
                                //val offset = if (layoutManager.orientation == RecyclerView.HORIZONTAL) recyclerView.width / 2 else recyclerView.height / 2
                                recyclerView.smoothScrollBy(offset[0], offset[1])
                            } else {
                                L.d(
                                    "Scroller",
                                    picker
                                ) { "Instant scroll with snap helper... (${offset[0]}, ${offset[1]})" }
                                recyclerView.scrollBy(offset[0], offset[1])
                                //doOnNextLayout {
                                onCheckScroll()
                                //}
                            }
                        } else {
                            // we are done
                            L.d("Scroller", picker) { "TARGET POS reached!" }
                            this.scroll = Scroll.None
                        }
                    } else {
                        if (scroll.smooth) {
                            L.d("Scroller", picker) { "Smooth scroll to pos..." }
                            recyclerView.smoothScrollToPosition(scroll.position)
                            //layoutManager.smoothScrollToCenterPosition(recyclerView, scroll.position)
                        } else {
                            L.d("Scroller", picker) { "Instant scroll to pos..." }
                            recyclerView.scrollToPosition(scroll.position)
                            doOnNextLayout {
                                onCheckScroll()
                            }
                        }
                    }
                }
                is Scroll.Scrolled -> {
                    // this is a user scroll => we must check if the selected value is our data value
                    if (!scroll.programmatically) {
                        val snapView = helper.findSnapView(recyclerView.layoutManager)
                        val snapIndex = snapView?.let { recyclerView.getChildAdapterPosition(it) }
                        val item = snapIndex?.let { adapter.getItem(it) }

                        L.d("Scroller", picker) { "Scrolled MANUALLY: $snapIndex - $item" }
                        if (item != null) {
                            // picker does take care to only apply values if they changed, no need to do this here as well
                            picker.setValue(item)
                        }
                    } else {
                        val item = adapter.getItem(scroll.position)
                        L.d(
                            "Scroller",
                            picker
                        ) { "Scrolled PROGRAMMATICALLY... (pos = ${scroll.position}, item = $item)" }
                    }
                    this.scroll = Scroll.None
                }
            }
        }
    }

}