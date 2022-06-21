package com.michaelflisar.materialnumberpicker

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
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
    defStyleRes: Int = R.style.MaterialNumberPicker_Filled
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        val FOCUSED_STATE_ARRAY = intArrayOf(android.R.attr.state_focused)
        val UNFOCUSED_STATE_ARRAY = intArrayOf(0, -android.R.attr.state_focused)
    }

    // -------------------
    // classes
    // -------------------

    // ordinal must match the array resource array values!
    internal enum class DataType(val inputType: kotlin.Int) {
        /* 0 */ Int(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED),
        /* 1 */ Float(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL)
    }

    internal class RepeatTouchListener(
        private val view: View,
        private val initialDelay: Int,
        private val consecutiveDelay: Int,
        private val onEvent: (view: View) -> Boolean
    ) : OnTouchListener {

        private lateinit var handlerRunnable: Runnable

        init {
            handlerRunnable = Runnable {
                if (view.isEnabled) {
                    val success = onEvent(view)
                    if (success) {
                        view.handler.postDelayed(handlerRunnable, consecutiveDelay.toLong())
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
                    view.handler.postDelayed(handlerRunnable, initialDelay.toLong())
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

    // -------------------
    // public listeners and settings
    // -------------------

    /*
     * listener that is called whenever this pickers value is changed
     */
    var onValueChangedListener: ((picker: MaterialNumberPicker, value: Float, fromUser: Boolean) -> Unit)? =
        null

    /*
     * listener that is called whenever the user inputs an invalid value in the EditText directly (outside of {@min} and {@max})
     */
    var onInvalidValueSelected: ((invalidValue: Float, fromButton: Boolean) -> Unit)? = null

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
    // public settings and fields
    // -------------------

    /*
     * define a prefix that is displayed before the number
     */
    private var prefix: String = ""

    /*
     * define a suffix that is displayed after the number
     */
    private var suffix: String = ""

    /*
     * the mimimum allowed number
     */
    private var min: Float = 0f

    /*
     * the maximum allowed number
     */
    private var max: Float = 100f

    /*
     * the current value
     */
    private var value: Float = 0f

    /*
     * the step size of the primary up/down buttons
     */
    private var stepSize: Float = 1f

    /*
     * the step size of the secondary up/down buttons
     *
     * if this step size is the same as the primary step size, secondary buttons won't be shown!
     */
    private var stepSizeLarge: Float = 1f

    /*
     * the data type of this picker (Int or Float)
     */
    private var type: DataType = DataType.Int

    /*
     * the style of the inner EditText view
     */
    private var editTextStyleId: Int = 0

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

            min = array.getFloat(R.styleable.MaterialNumberPicker_mnp_min, 100f)
            max = array.getFloat(R.styleable.MaterialNumberPicker_mnp_max, 0f)
            stepSize = array.getFloat(R.styleable.MaterialNumberPicker_mnp_stepSize, 1f)
            stepSizeLarge = array.getFloat(R.styleable.MaterialNumberPicker_mnp_stepSizeLarge, 1f)

            prefix = array.getString(R.styleable.MaterialNumberPicker_mnp_prefix) ?: ""
            suffix = array.getString(R.styleable.MaterialNumberPicker_mnp_suffix) ?: ""

            longPressRepeatClicks =
                array.getBoolean(R.styleable.MaterialNumberPicker_mnp_longPressRepeatClicks, true)

            val buttonWidth =
                array.getDimension(R.styleable.MaterialNumberPicker_mnp_buttonWidth, 0f).toInt()

            orientation =
                array.getInteger(R.styleable.MaterialNumberPicker_mnp_orientation, VERTICAL)
            background = array.getDrawable(R.styleable.MaterialNumberPicker_android_background)

            val iconUp = array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_up, 0)
            val iconDown = array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_down, 0)
            val iconUpLarge =
                array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_up_large, 0)
            val iconDownLarge =
                array.getResourceId(R.styleable.MaterialNumberPicker_mnp_icon_down_large, 0)

            editTextStyleId = array.getResourceId(
                R.styleable.MaterialNumberPicker_mnp_editTextStyle,
                R.style.MaterialNumberPicker_EditTextStyle
            )
            type = DataType.values()[array.getInteger(
                R.styleable.MaterialNumberPicker_mnp_dataType,
                DataType.Int.ordinal
            )]

            inflateChildren(buttonWidth, iconUp, iconDown, iconUpLarge, iconDownLarge)

            value = array.getFloat(R.styleable.MaterialNumberPicker_mnp_value, 0f)
            editText.setText(getDisplayValue())


        } catch (e: Exception) {
            Log.d("MaterialNumberPicker", e.toString())
        } finally {
            array.recycle()
        }
    }

    private fun inflateChildren(
        buttonWidth: Int,
        iconUp: Int,
        iconDown: Int,
        iconUpLarge: Int,
        iconDownLarge: Int
    ) {

        // inner setup
        val useHintTrick = true
        val emsAdjustment = -2

        // Buttons
        val buttonsDown = ArrayList<Pair<Int, Int>>()
        val buttonsUp = ArrayList<Pair<Int, Int>>()
        buttonsDown.add(Pair(R.id.number_picker_button_down, iconDown))
        buttonsUp.add(Pair(R.id.number_picker_button_up, iconUp))
        if (stepSize != stepSizeLarge) {
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
                            repeatClicksFirstDelay,
                            repeatClicksConsecutiveDelay
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

        val digits = max(abs(max).toString().length, abs(min).toString().length)
        val charText = prefix.length + suffix.length

        editText = EditText(ContextThemeWrapper(context, editTextStyleId), null, 0)
        //editText.id = R.id.number_picker_edit_text
        editText.setLines(1)

        if (!useHintTrick) {
            editText.setEms(charText + digits + emsAdjustment)
        } else {
            editText.hint = prefix + ("0.".repeat(digits)) + suffix
            editText.setHintTextColor(Color.TRANSPARENT)
        }

        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isClickable = true
        editText.isLongClickable = false
        editText.inputType = type.inputType

        editText.setOnFocusChangeListener { _, hasFocus ->
            setBackgroundFocused(hasFocus)

            if (!hasFocus) {
                if (!editText.text.isNullOrEmpty()) {
                    val newValue = getValue(true)
                    if (newValue != value) {
                        updateValue(newValue, true)
                    } else {
                        restoreValue()
                    }
                } else {
                    editText.setText(getDisplayValue())
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

        val p0 = LayoutParams(btWidth, btHeight)
        p0.weight = 0f

        val p1 = if (orientation == HORIZONTAL) LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT) else LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0)
        p1.weight = 1f

        val buttonsBefore = if (orientation == HORIZONTAL) buttonsDown else buttonsUp
        val buttonsAfter = if (orientation == HORIZONTAL) buttonsUp else buttonsDown

        buttonsBefore.forEach {
            addView(createButton(it.first, it.second), p0)
        }
        addView(editText, p1)
        buttonsAfter.forEach {
            addView(createButton(it.first, it.second), p0)
        }
    }

    private fun updateValue(newValue: Float, fromUser: Boolean = true) {
        val newDisplayValue = getDisplayValue(newValue)
        val valueChanged = newValue != value
        val textChanged = editText.text.toString() != newDisplayValue
        if (valueChanged || textChanged) {
            value = newValue
            if (textChanged) {
                editText.setText(newDisplayValue)
            }
            onValueChangedListener?.invoke(this, value, fromUser)
            if (closeKeyboardOnNewValueSet) {
                editText.clearFocus()
                requestFocus()
                hideKeyboard()
            }
        }
    }

    private fun restoreValue() {
        val displayValue = getDisplayValue(value)
        val text = editText.text.toString()
        if (text != displayValue) {
            editText.setText(displayValue)
        }
    }

// -----------
// format functions
// -----------

    private fun getDisplayValue(): String {
        return getDisplayValue(value)
    }

    private fun getDisplayValue(value: Float): String {
        if (value.toInt().toFloat() == value) {
            return prefix + value.toInt().toString() + suffix
        }
        return prefix + value.toString() + suffix
    }

    private fun getValue(fromEditText: Boolean, callCallbacks: Boolean = true): Float {
        return if (fromEditText) {
            val newValue =
                editText.text.toString().replace(prefix, "").replace(suffix, "").toFloat()
            if (isValueAllowed(newValue)) {
                newValue
            } else {
                if (callCallbacks) {
                    onInvalidValueSelected?.invoke(newValue, false)
                }
                value
            }
        } else value
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
        } else value
        value = v
        requestFocus()
        editText.clearFocus()

        val adjustment = when (buttonId) {
            R.id.number_picker_button_up -> stepSize
            R.id.number_picker_button_up_large -> stepSizeLarge
            R.id.number_picker_button_down -> stepSize * -1f
            R.id.number_picker_button_down_large -> stepSizeLarge * -1f
            else -> throw RuntimeException("Unhandled button view!")
        }
        val newValue = value + adjustment
        val result = if (isValueAllowed(newValue)) {
            updateValue(newValue)
            true
        } else {
            onInvalidValueSelected?.invoke(newValue, true)
            false
        }
        if (closeKeyboardOnUpDownClicks) {
            hideKeyboard()
        }
        return result
    }

    private fun isValueAllowed(value: Float): Boolean {
        return value in min..max
    }

    private fun setBackgroundFocused(hasFocus: Boolean) {
        if (hasFocus) {
            background?.state = FOCUSED_STATE_ARRAY
        } else {
            background?.state = UNFOCUSED_STATE_ARRAY
        }
    }
}

