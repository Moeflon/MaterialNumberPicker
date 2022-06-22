package com.michaelflisar.materialnumberpicker

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import kotlin.math.abs
import kotlin.math.max

class MaterialNumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pickerStyle,
    defStyleRes: Int = R.style.MaterialNumberPicker_Horizontal_Filled
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val FOCUSED_STATE_ARRAY = intArrayOf(android.R.attr.state_focused)
        val UNFOCUSED_STATE_ARRAY = intArrayOf(0, -android.R.attr.state_focused)
    }

    // -------------------
    // classes
    // -------------------

    // ordinal must match the array resource array values!
    enum class DataType(val inputType: kotlin.Int) {
        /* 0 */ Int(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED),
        /* 1 */ Float(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL)
    }

    private class RepeatTouchListener(
        private val view: View,
        private val picker: MaterialNumberPicker,
        private val onEvent: (view: View) -> Boolean
    ) : OnTouchListener {

        private lateinit var handlerRunnable: Runnable

        init {
            handlerRunnable = Runnable {
                if (view.isEnabled) {
                    val success = onEvent(view)
                    if (success) {
                        view.handler.postDelayed(handlerRunnable, picker.repeatClicksConsecutiveDelay.toLong())
                    } else {
                        stop()
                    }
                } else {
                    stop()
                }
            }
        }

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.handler.removeCallbacks(handlerRunnable)
                    view.handler.postDelayed(handlerRunnable, picker.repeatClicksFirstDelay.toLong())
                    view.isPressed = true
                    onEvent(view)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stop()
                    return true
                }
            }
            return false
        }

        private fun stop() {
            view.handler.removeCallbacks(handlerRunnable)
            view.isPressed = false
        }
    }

    private data class State(
        val prefix: String = "",
        val suffix: String = "",
        val min: Float,
        val max: Float,
        val value: Float,
        val stepSize: Float,
        val stepSizeLarge: Float,
        val type: DataType
    ) {
        init {
            if (min > max || value < min || value > max) {
                throw RuntimeException("You must provide a valid value and min/max values, where the min value is smaller than the max value and the value is inside its range!")
            }
        }

        val supportsLargeButtons = stepSize != stepSizeLarge
        val digits = max(
            abs(max).toString().length,
            abs(min).toString().length
        )

        val charsText = prefix.length + suffix.length

        fun getDisplayValue(): String {
            return when (type) {
                DataType.Int -> getDisplayValue(value.toInt())
                DataType.Float -> getDisplayValue(value)
            }
        }

        fun isValueAllowed(value: Number): Boolean {
            return when (type) {
                DataType.Int -> value.toInt() in (min.toInt())..(max.toInt())
                DataType.Float -> value.toFloat() in min..max
            }
        }

        fun calcNewValue(adjustment: Number): Number {
            return when (type) {
                DataType.Int -> value.toInt() + adjustment.toInt()
                DataType.Float -> value + adjustment as Float
            }
        }

        fun calcButtonResult(buttonId: Int): Float {
            val adjustment = when (buttonId) {
                R.id.number_picker_button_up -> stepSize
                R.id.number_picker_button_up_large -> stepSizeLarge
                R.id.number_picker_button_down -> stepSize
                R.id.number_picker_button_down_large -> stepSizeLarge
                else -> throw RuntimeException("Unhandled button view!")
            }
            val increase = when (buttonId) {
                R.id.number_picker_button_up,
                R.id.number_picker_button_up_large -> true
                R.id.number_picker_button_down,
                R.id.number_picker_button_down_large -> false
                else -> throw RuntimeException("Unhandled button view!")
            }
            return when (type) {
                DataType.Int -> value.toInt() + adjustment.toInt() * if (increase) 1 else -1
                DataType.Float -> value + adjustment * if (increase) 1f else -1f
            }.toFloat()
        }

        // ---------------------
        // format functions for all possible data types
        // ---------------------

        fun getDisplayValue(value: Number): String {
            return when (value) {
                is Int -> getDisplayValue(value)
                is Float -> getDisplayValue(value)
                else -> throw RuntimeException("Class ${value.javaClass} not handled!")
            }
        }

        fun getDisplayValue(value: Int): String {
            val valueAsString = value.toString()
            return prefix + valueAsString + suffix
        }

        fun getDisplayValue(value: Float): String {
            val valueAsString = if (value.toInt().toFloat() == value)
                value.toInt().toString()
            else
                value.toString()

            return prefix + valueAsString + suffix
        }

        @Suppress("UNCHECKED_CAST")
        fun parseValue(editText: EditText): Float? {
            val possibleValueAsString =
                editText.text.toString().replace(prefix, "").replace(suffix, "")
            val newValue = when (type) {
                DataType.Int -> possibleValueAsString.toIntOrNull()?.toFloat()
                DataType.Float -> possibleValueAsString.toFloatOrNull()
            }
            return newValue
        }
    }

    // -------------------
    // public listeners and settings
    // -------------------

    /*
     * listener that is called whenever this pickers value is changed
     */
    var onValueChangedListener: ((picker: MaterialNumberPicker, value: Number, fromUser: Boolean) -> Unit)? =
        null

    /*
     * listener that is called whenever the user inputs an invalid value in the EditText directly (outside of {@min} and {@max})
     */
    var onInvalidValueSelected: ((invalidValue: Number, fromButton: Boolean) -> Unit)? = null

    /*
     * listener that is called, whenever this pickers internal EditText's focus is changed
     */
    var focusChangedListener: ((focus: Boolean) -> Unit)? = null

    /*
     * if enabled, long pressing a button will trigger it again and again after an initial {@longPressFirstDelay} and further {@longPressConsecutiveDelay}
     */
    var longPressRepeatClicks: Boolean = true

    /*
     * define, how long the duration between a press and triggering further click events is
     */
    var repeatClicksFirstDelay: Int = 300

    /*
     * define, how long the duration between consecutive click events is if the user hold a button and keeps it pressed
    */
    var repeatClicksConsecutiveDelay: Int = 100

    /*
     * define if button clicks should close an eventually opened keyboard or not
     */
    var closeKeyboardOnUpDownClicks: Boolean = true

    /*
     * define if changing the value closes an eventually opened keyboard or not
     */
    var closeKeyboardOnNewValueSet: Boolean = true

    // -------------------
    // getter/setter/update functions
    // -------------------

    val type: DataType
        get() = state.type

    val value: Number
        get() = state.value

    fun setValue(value: Number): Boolean {
        if (!state.isValueAllowed(value))
            return false
        updateValue(value.toFloat(), true)
        return true
    }

    val min: Number
        get() = state.min

    val max: Number
        get() = state.max

    fun setMinMax(min: Number, max: Number, value: Number) {
        state = state.copy(min = min.toFloat(), max = max.toFloat(), value = value.toFloat())
        updateEditTextDisplayValue()
    }

    val stepSize: Number
        get() = state.stepSize

    val stepSizeLarge: Number
        get() = state.stepSizeLarge

    /*
     * set one step size for the buttons - by setting a single step size only, the additional buttons for the large step size won't show up
     */
    fun setSingleStepSize(stepSize: Number) {
        state = state.copy(stepSize = stepSize.toFloat(), stepSizeLarge = stepSize.toFloat())
        onStepSizesChanged()
    }

    /*
     * set two step size for the buttons - by setting them to the same value, the additional buttons for the large step size won't show up
     * use {@setSingleStepSize} for this case though
     */
    fun setStepSizes(stepSize: Number, stepSizeLarge: Number) {
        state = state.copy(stepSize = stepSize.toFloat(), stepSizeLarge = stepSizeLarge.toFloat())
        onStepSizesChanged()
    }

    var prefix: String
        get() = state.prefix
        set(value) {
            state = state.copy(prefix = value)
            onPrefixChanged()
        }

    var suffix: String
        get() = state.suffix
        set(value) {
            state = state.copy(suffix = value)
            onSuffixChanged()
        }

    fun clearInputFocus() {
        editText.clearFocus()
        requestFocus()
    }

    // -------------------
    // private state and variables
    // -------------------

    private lateinit var state: State
    private var editTextStyleId: Int = 0
    private var buttonWidth: Int = 0
    private var iconUp: Int = 0
    private var iconDown: Int = 0
    private var iconUpLarge: Int = 0
    private var iconDownLarge: Int = 0
    private lateinit var editText: EditText

    // -------------------
    // class
    // -------------------

    init {

        setWillNotDraw(false)

        isFocusable = true
        isFocusableInTouchMode = true

        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        val array = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialNumberPicker,
            defStyleAttr,
            defStyleRes
        )

        try {

            val min = array.getFloat(R.styleable.MaterialNumberPicker_mnp_min, 100f)
            val max = array.getFloat(R.styleable.MaterialNumberPicker_mnp_max, 0f)
            val stepSize = array.getFloat(R.styleable.MaterialNumberPicker_mnp_stepSize, 1f)
            val stepSizeLarge =
                array.getFloat(R.styleable.MaterialNumberPicker_mnp_stepSizeLarge, 1f)
            val prefix = array.getString(R.styleable.MaterialNumberPicker_mnp_prefix) ?: ""
            val suffix = array.getString(R.styleable.MaterialNumberPicker_mnp_suffix) ?: ""
            val type = DataType.values()[array.getInteger(
                R.styleable.MaterialNumberPicker_mnp_dataType,
                DataType.Int.ordinal
            )]
            val value = array.getFloat(R.styleable.MaterialNumberPicker_mnp_value, 0f)

            state = when (type) {
                DataType.Int -> State(
                    prefix,
                    suffix,
                    min.toInt().toFloat(),
                    max.toInt().toFloat(),
                    value.toInt().toFloat(),
                    stepSize.toInt().toFloat(),
                    stepSizeLarge.toInt().toFloat(),
                    DataType.Int
                )
                DataType.Float -> State(
                    prefix,
                    suffix,
                    min,
                    max,
                    value,
                    stepSize,
                    stepSizeLarge,
                    DataType.Float
                )
            }

            longPressRepeatClicks =
                array.getBoolean(R.styleable.MaterialNumberPicker_mnp_longPressRepeatClicks, true)

            buttonWidth =
                array.getDimension(R.styleable.MaterialNumberPicker_mnp_buttonWidth, 0f).toInt()

            orientation =
                array.getInteger(R.styleable.MaterialNumberPicker_mnp_orientation, VERTICAL)
            background = array.getDrawable(R.styleable.MaterialNumberPicker_android_background)

            iconUp = array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_up, 0)
            iconDown = array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_down, 0)
            iconUpLarge =
                array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_up_large, 0)
            iconDownLarge =
                array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_down_large, 0)

            editTextStyleId = array.getResourceId(
                R.styleable.MaterialNumberPicker_mnp_editTextStyle,
                R.style.MaterialNumberPicker_EditTextStyle
            )
            inflateChildren()
            editText.setText(state.getDisplayValue())
        } catch (e: Exception) {
            //
        } finally {
            array.recycle()
        }
    }

    private fun inflateChildren() {

        // inner setup
        val useHintTrick = true
        val emsAdjustment = -2

        // EditText
        editText = EditText(ContextThemeWrapper(context, editTextStyleId), null, 0)
        //editText.id = R.id.number_picker_edit_text
        editText.setLines(1)

        if (!useHintTrick) {
            editText.setEms(state.charsText + state.digits + emsAdjustment)
        } else {
            editText.hint = state.prefix + ("0.".repeat(state.digits)) + state.suffix
            editText.setHintTextColor(Color.TRANSPARENT)
        }

        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isClickable = true
        editText.isLongClickable = false
        editText.inputType = state.type.inputType

        editText.setOnFocusChangeListener { _, hasFocus ->
            setBackgroundFocused(hasFocus)

            if (!hasFocus) {
                if (!editText.text.isNullOrEmpty()) {
                    val newValue = getValue(true)
                    if (newValue != state.value) {
                        updateValue(newValue, true)
                    } else {
                        restoreValue()
                    }
                } else {
                    editText.setText(state.getDisplayValue())
                }
            }

            focusChangedListener?.invoke(hasFocus)
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

        // Buttons
        initSubViews(true)
    }

    private fun initSubViews(initial: Boolean) {
        if (!initial) {
            removeAllViews()
        }
        val buttonsDown = ArrayList<Pair<Int, Int>>()
        val buttonsUp = ArrayList<Pair<Int, Int>>()
        buttonsDown.add(Pair(R.id.number_picker_button_down, iconDown))
        buttonsUp.add(Pair(R.id.number_picker_button_up, iconUp))
        if (state.supportsLargeButtons) {
            buttonsDown.add(0, Pair(R.id.number_picker_button_down_large, iconDownLarge))
            buttonsUp.add(Pair(R.id.number_picker_button_up_large, iconUpLarge))
        }
        val createButton = { id: Int, icon: Int ->
            AppCompatImageButton(context).apply {
                this.id = id
                setImageResource(icon)
                setBackgroundResource(R.drawable.mnp_button_background)
                if (longPressRepeatClicks) {
                    setOnTouchListener(
                        RepeatTouchListener(
                            this,
                            this@MaterialNumberPicker
                        ) {
                            onButtonEvent(it.id)
                        })
                } else {
                    setOnClickListener {
                        onButtonEvent(it.id)
                    }
                }
            }
        }

        var btWidth = if (buttonWidth > 0) buttonWidth else ViewGroup.LayoutParams.WRAP_CONTENT
        var btHeight = ViewGroup.LayoutParams.MATCH_PARENT
        if (orientation == VERTICAL) {
            val tmp = btWidth
            btWidth = btHeight
            btHeight = tmp
        }

        val p0 = LayoutParams(btWidth, btHeight)
        p0.weight = 0f
        val p1 = if (orientation == HORIZONTAL) LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) else LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        p1.weight = 1f
        val buttonsBefore = if (orientation == HORIZONTAL) buttonsDown else buttonsUp.asReversed()
        val buttonsAfter = if (orientation == HORIZONTAL) buttonsUp else buttonsDown.asReversed()
        buttonsBefore.forEach {
            addView(createButton(it.first, it.second), p0)
        }
        addView(editText, p1)
        buttonsAfter.forEach {
            addView(createButton(it.first, it.second), p0)
        }
    }

    private fun updateValue(
        newValue: Float,
        fromUser: Boolean
    ) {
        val newDisplayValue = state.getDisplayValue(newValue)
        val valueChanged = newValue != state.value
        val textChanged = editText.text.toString() != newDisplayValue
        if (valueChanged || textChanged) {
            state = state.copy(value = newValue)
            if (textChanged) {
                editText.setText(newDisplayValue)
            }
            onValueChangedListener?.invoke(this, newValue, fromUser)
            if (closeKeyboardOnNewValueSet) {
                editText.clearFocus()
                requestFocus()
                hideKeyboard()
            }
        }
    }

    private fun restoreValue() {
        val displayValue = state.getDisplayValue()
        val text = editText.text.toString()
        if (text != displayValue) {
            editText.setText(displayValue)
        }
    }

    private fun getValue(fromEditText: Boolean, callCallbacks: Boolean = true): Float {
        return if (fromEditText) {
            val oldValue = state.value
            val newValue = state.parseValue(editText)
            if (newValue == null || !state.isValueAllowed(newValue)) {
                if (callCallbacks) {
                    onInvalidValueSelected?.invoke(newValue ?: 0f, false)
                }
                oldValue
            } else newValue
        } else state.value
    }

    // -----------
    // Events
    // -----------

    private fun onPrefixChanged() {
        updateEditTextDisplayValue()
    }

    private fun onSuffixChanged() {
        updateEditTextDisplayValue()
    }

    private fun updateEditTextDisplayValue() {
        val displayValue = state.getDisplayValue()
        editText.setText(displayValue)
    }

    private fun onStepSizesChanged() {
        initSubViews(false)
    }

    // -----------
    // Others
    // -----------

    private fun hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun onButtonEvent(buttonId: Int): Boolean {

        // clearing the focus from the EditText could update the value and call the onValueChangedListener
        // => we don't want this, the button click should only trigger ONE event in this case!
        // => so we get the value from the EditText manually here before we clear the focus
        val v = if (!editText.text.isNullOrEmpty() && editText.hasFocus()) {
            getValue(true, false)
        } else state.value
        state = state.copy(value = v)
        requestFocus()
        editText.clearFocus()

        val newValue = state.calcButtonResult(buttonId)
        val result = if (!state.isValueAllowed(newValue)) {
            onInvalidValueSelected?.invoke(newValue, true)
            false
        } else {
            updateValue(newValue, true)
            true
        }
        if (closeKeyboardOnUpDownClicks) {
            hideKeyboard()
        }
        return result
    }

    private fun setBackgroundFocused(hasFocus: Boolean) {
        if (hasFocus) {
            background?.state = FOCUSED_STATE_ARRAY
        } else {
            background?.state = UNFOCUSED_STATE_ARRAY
        }
    }
}

