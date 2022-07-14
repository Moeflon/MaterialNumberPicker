package com.michaelflisar.materialnumberpicker.internal

import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup

internal sealed class InputView<T, Picker> where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

    // Update
    // abstract fun updateValue(picker: Picker, value: T)

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
            // TODO:
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
        val recyclerView: RecyclerView,
        val layoutManager: LinearLayoutManager,
        val helper: SnapHelper,
        val adapter: NumberPickerAdapter<T, Picker>
    ) : InputView<T, Picker>() where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        // why?
        // this is used to delay the selection event until the RecyclerView is visible (for view state recreation e.g.)
        // and to always make sure, that only the LAST action is executed (e.g. if multiple events would happen because of view recreation
        //    and/or because of changing the displayed value multipe times in code will the RecyclerView is not (yet) visible)
        private var nextLayoutAction: (() -> Unit)? = null
        private var scrollListener: RecyclerView.OnScrollListener? = null

        override fun updateDisplayedValue(
            picker: Picker,
            setup: INumberPickerSetup<T>,
            value: T,
            smooth: Boolean
        ) {

            //val index = adapter.getItemIndex(value)
            //val pickerId =
            //    recyclerView.resources.getResourceName((recyclerView.parent as ViewGroup).id)
            //        .substringAfterLast(":id/")
            //Log.d(
            //    "Scroller",
            //    "scroll to pos - updateDisplayedValue [$pickerId] | index = $index | value = $value | attached = ${recyclerView.isAttachedToWindow}"
            //)

            nextLayoutAction = {
                val index = adapter.getItemIndex(value)
                scrollListener?.let { recyclerView.removeOnScrollListener(it) }
                val check = {
                    val vh = recyclerView.findViewHolderForLayoutPosition(index)
                    if (vh != null) {
                        val offset =
                            helper.calculateDistanceToFinalSnap(layoutManager, vh.itemView)!!
                        if (offset[0] != 0 || offset[1] != 0) {
                            if (smooth) {
                                recyclerView.smoothScrollBy(offset[0], offset[1])
                            } else {
                                recyclerView.scrollBy(offset[0], offset[1])
                            }
                        }
                        //Log.d(
                        //    "Scroller",
                        //    "scroll to pos [$pickerId] = $index (value = $value) | vh = $vh | offset = ${offset[0]}, ${offset[1]}"
                        //)
                        scrollListener?.let { recyclerView.removeOnScrollListener(it) }
                        scrollListener = null
                    } else {
                        //Log.d(
                        //  "Scroller",
                        //  "scroll to pos [$pickerId] = $index (value = $value) | vh = $vh"
                        ///
                    }
                }
                scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            check()
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        check()
                    }
                }

                recyclerView.scrollToPosition(index)
                check()
                scrollListener?.let { recyclerView.addOnScrollListener(it) }
            }
            if (recyclerView.isLaidOut) {
                nextLayoutAction?.invoke()
                nextLayoutAction = null
            } else {
                recyclerView.doOnNextLayout {
                    nextLayoutAction?.invoke()
                    nextLayoutAction = null
                }
            }
        }

        override fun clearFocus() {
            // nothing to do
        }

        override fun onSetupChanged(picker: Picker, setup: INumberPickerSetup<T>) {
            // TODO: Liste inkl. Werte updaten
            ViewUtil.initScrollViews(picker, this)
        }
    }

}