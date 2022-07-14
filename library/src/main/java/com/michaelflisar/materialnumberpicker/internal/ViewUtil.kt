package com.michaelflisar.materialnumberpicker.internal

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.R
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup

internal object ViewUtil {

    fun <T, Picker> initEditTextView(
        picker: Picker,
        editTextStyleId: Int
    ): InputView<T, Picker> where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        val useHintTrick = true
        val emsAdjustment = -2

        // EditText
        val editText = EditText(ContextThemeWrapper(picker.context, editTextStyleId), null, 0)
        editText.setLines(1)

        val longestValue = picker.setup.longestValue
        val formatted = picker.setup.formatter(longestValue)

        if (!useHintTrick) {
            editText.setEms(formatted.length + emsAdjustment)
        } else {
            editText.hint = formatted
            editText.setHintTextColor(Color.TRANSPARENT)
        }

        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isClickable = true
        editText.isLongClickable = false
        editText.inputType = picker.setup.type.inputType

        editText.setOnFocusChangeListener { _, hasFocus ->
            picker.setBackgroundFocused(hasFocus)
            if (!hasFocus) {
                val text = editText.text.toString()
                if (!text.isNullOrEmpty()) {
                    val newValue = picker.setup.parser(text)
                    if (!picker.value.isEqual(newValue)) {
                        if (picker.setup.isValueAllowed(newValue)) {
                            val displayedValue = picker.setup.formatter(newValue!!)

                            // we update the value directly, check is already done and we will call listeners manually
                            picker.value = newValue
                            editText.setText(displayedValue)

                            picker.onValueChangedListener?.invoke(picker, newValue, true)
                        } else {

                            val displayedOldValue = picker.setup.formatter(picker.value)

                            // we just reset the EditText
                            editText.setText(displayedOldValue)

                            picker.onInvalidValueSelected?.invoke(picker, text, newValue, false)
                        }
                    }
                } else {
                    val displayedOldValue = picker.setup.formatter(picker.value)
                    editText.setText(displayedOldValue)
                }
            }
            picker.focusChangedListener?.invoke(hasFocus)
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    editText.clearFocus()
                    true
                }
                else -> false
            }
        }

        val inputView = InputView.Input<T, Picker>(editText)
        initSubViews(picker, inputView, true)
        return inputView
    }

    fun <T, Picker> initSubViews(
        picker: Picker,
        inputView: InputView.Input<T, Picker>,
        initial: Boolean
    ) where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {
        if (!initial) {
            picker.removeAllViews()
        }
        val buttonsDown = ArrayList<Pair<Int, Int>>()
        val buttonsUp = ArrayList<Pair<Int, Int>>()
        buttonsDown.add(Pair(R.id.number_picker_button_down, picker.iconDown))
        buttonsUp.add(Pair(R.id.number_picker_button_up, picker.iconUp))
        val setup = picker.setup
        if (setup is INumberPickerSetup.SecondaryButtonProvider<*> && setup.supportsSecondaryButton()) {
            buttonsDown.add(0, Pair(R.id.number_picker_button_down_large, picker.iconDownLarge))
            buttonsUp.add(Pair(R.id.number_picker_button_up_large, picker.iconUpLarge))
        }
        val createButton = { id: Int, icon: Int ->
            AppCompatImageButton(picker.context).apply {
                this.id = id
                setImageResource(icon)
                setBackgroundResource(R.drawable.mnp_button_background)
                if (picker.longPressRepeatClicks) {
                    setOnTouchListener(
                        RepeatTouchListener(
                            this,
                            picker
                        ) {
                            onButtonEvent(picker, inputView, it.id)
                        })
                } else {
                    setOnClickListener {
                        onButtonEvent(picker, inputView, it.id)
                    }
                }
            }
        }

        var btWidth =
            if (picker.buttonWidth > 0) picker.buttonWidth else ViewGroup.LayoutParams.WRAP_CONTENT
        var btHeight = ViewGroup.LayoutParams.MATCH_PARENT
        if (picker.orientation == LinearLayout.VERTICAL) {
            val tmp = btWidth
            btWidth = btHeight
            btHeight = tmp
        }

        val p0 = LinearLayout.LayoutParams(btWidth, btHeight)
        p0.weight = 0f
        val p1 = if (picker.orientation == LinearLayout.HORIZONTAL) LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) else LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        p1.weight = 1f
        val buttonsBefore =
            if (picker.orientation == LinearLayout.HORIZONTAL) buttonsDown else buttonsUp.asReversed()
        val buttonsAfter =
            if (picker.orientation == LinearLayout.HORIZONTAL) buttonsUp else buttonsDown.asReversed()
        buttonsBefore.forEach {
            picker.addView(createButton(it.first, it.second), p0)
        }
        picker.addView(inputView.editText, p1)
        buttonsAfter.forEach {
            picker.addView(createButton(it.first, it.second), p0)
        }
    }

    private fun <T, Picker> onButtonEvent(
        picker: Picker,
        inputView: InputView.Input<T, Picker>,
        buttonId: Int
    ): Boolean where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        // clearing the focus from the EditText could update the value and call the onValueChangedListener
        // => we don't want this, the button click should only trigger ONE event in this case!
        // => so we get the value from the EditText manually here before we clear the focus
        val v = inputView.getValueFromInput(picker.setup, true) ?: picker.value

        picker.value = v
        picker.requestFocus()

        inputView.clearFocus()

        val buttonType = when (buttonId) {
            R.id.number_picker_button_down -> INumberPickerSetup.Button.Down
            R.id.number_picker_button_down_large -> INumberPickerSetup.Button.Down
            R.id.number_picker_button_up -> INumberPickerSetup.Button.Up
            R.id.number_picker_button_up_large -> INumberPickerSetup.Button.Up
            else -> throw RuntimeException("Unhandled button id!")
        }
        val primaryButton = when (buttonId) {
            R.id.number_picker_button_down,
            R.id.number_picker_button_up -> true
            R.id.number_picker_button_down_large,
            R.id.number_picker_button_up_large -> false
            else -> throw RuntimeException("Unhandled button id!")
        }

        val newValue = if (primaryButton) {
            (picker.setup as INumberPickerSetup.ButtonProvider<T>).calcPrimaryButtonResult(
                picker.value,
                buttonType
            )
        } else {
            (picker.setup as INumberPickerSetup.SecondaryButtonProvider<T>).calcSecondaryButtonResult(
                picker.value,
                buttonType
            )
        }

        val result = if (!picker.setup.isValueAllowed(newValue)) {
            picker.onInvalidValueSelected?.invoke(picker, null, newValue, true)
            false
        } else {
            if (!picker.value.isEqual(newValue)) {
                picker.value = newValue!!
                inputView.updateDisplayedValue(
                    picker,
                    picker.setup,
                    newValue!!,
                    false /* no function for EditText! */
                )
                picker.onValueChangedListener?.invoke(picker, newValue, true)
                true
            } else false
        }
        if (picker.closeKeyboardOnUpDownClicks) {
            picker.hideKeyboard()
        }
        return result
    }

    @SuppressLint("WrongConstant")
    fun <T, Picker> initScrollViews(
        picker: Picker,
        inputView: InputView.Scroller<T, Picker>?
    ): InputView<T, Picker> where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {


        val rv = inputView?.recyclerView ?: RecyclerView(picker.context).apply {
            id = R.id.number_picker_recycler_view
        }
        val helper = inputView?.helper ?: LinearSnapHelper()
        val lm = inputView?.layoutManager ?: CenterZoomLinearLayoutManager(
            rv,
            picker.orientation,
            false
        )
        val adapter = inputView?.adapter ?: NumberPickerAdapter<T, Picker>(
            picker,
            clickListener = { vh, item ->
                val offset = helper.calculateDistanceToFinalSnap(lm, vh.itemView)!!
                if (offset[0] != 0 || offset[1] != 0) {
                    rv.smoothScrollBy(offset[0], offset[1])
                }
            },
            picker.setup.scrollerVisibleOffsetItems
        )

        if (inputView == null) {
            rv.layoutManager = lm
            helper.attachToRecyclerView(rv)
            rv.adapter = adapter
            rv.setHasFixedSize(false)

            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var lastSnapIndex: Int? = null

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        onScrollChanged()
                    }
                }

                private fun onScrollChanged() {
                    val snapView = helper.findSnapView(rv.layoutManager)
                    val snapIndex = snapView?.let { rv.getChildAdapterPosition(it) }
                    if (snapIndex != null && snapIndex != lastSnapIndex) {
                        Log.d(
                            "Scroller",
                            "onScrolled - snapIndex = $snapIndex (lastSnapIndex = $lastSnapIndex)"
                        )

                        lastSnapIndex = snapIndex
                        val item = adapter.getItem(snapIndex)
                        // TODO: state updaten
                        item?.let {
                            picker.setValue(it)
                            // picker.inputView.updateDisplayedValue(picker, picker.setup, it, false)
                        }

                    }
                }
            })
        } else {
            picker.removeView(rv)
            adapter.reload()
        }

        // 2) RecyclerView (re)attachen
        val scrollerItemSize = picker.context.getDimen(R.dimen.mnp_scroller_item_size).toInt()
        //val extraSizeForBoldness = picker.context.getDimen(R.dimen.mnp_scroller_item_extra_size_for_boldness).toInt()
        val lp = if (picker.orientation == LinearLayout.VERTICAL) {
            val itemWidth = getMeasuredMaxItemWidth(
                picker,
                R.layout.mnp_scroller_item_vertical
            )
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = scrollerItemSize * (1 + adapter.visibleOffsetItems * 2)

            rv.minimumWidth = itemWidth + rv.paddingLeft + rv.paddingRight
            LinearLayout.LayoutParams(width, height)
        } else {
            val itemWidth = getMeasuredMaxItemWidth(
                picker,
                R.layout.mnp_scroller_item_horizontal
            )
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val width = itemWidth * (1 + adapter.visibleOffsetItems * 2)

            rv.minimumHeight = scrollerItemSize + rv.paddingTop + rv.paddingBottom
            LinearLayout.LayoutParams(width, height)
        }
        picker.addView(rv, lp)

        return inputView ?: InputView.Scroller(rv, lm, helper, adapter)
    }

    private fun <T, Picker> getMeasuredMaxItemWidth(
        picker: Picker,
        itemLayout: Int
    ): Int where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {
        val longestValue = picker.setup.longestValue

        val maxText = picker.setup.formatter(longestValue)//.map { "W" }.joinToString("")
        Log.d("LONGEST VALUE", "value = $longestValue => $maxText")
        val v = LayoutInflater.from(picker.context).inflate(itemLayout, picker, false)
        v.findViewById<TextView>(R.id.text).apply {
            text = maxText
        }
        v.measure(0, 0)
        return v.measuredWidth + v.marginLeft + v.marginRight
    }
}