package com.michaelflisar.materialnumberpicker

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.michaelflisar.materialnumberpicker.internal.InputView
import com.michaelflisar.materialnumberpicker.internal.L
import com.michaelflisar.materialnumberpicker.internal.ViewUtil
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import kotlinx.parcelize.Parcelize


abstract class AbstractMaterialNumberPicker<T, Picker> @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int,
    val defStyleRes: Int
) : LinearLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

    companion object {
        val FOCUSED_STATE_ARRAY = intArrayOf(android.R.attr.state_focused)
        val UNFOCUSED_STATE_ARRAY = intArrayOf(0, -android.R.attr.state_focused)
    }

    // -------------------
    // abstract functions
    // -------------------

    internal abstract val styleDefinitions: StyleDefinitions

    abstract fun initSetup(array: TypedArray): INumberPickerSetup<T>
    abstract fun initValue(array: TypedArray, setup: INumberPickerSetup<T>): T

    // -------------------
    // public listeners and settings
    // -------------------

    /*
     * listener that is called whenever this pickers value is changed
     */
    var onValueChangedListener: ((picker: Picker, value: T, fromUser: Boolean) -> Unit)? =
        null

    /*
     * listener that is called whenever the user inputs an invalid value in the EditText directly (outside of {@min} and {@max})
     */
    var onInvalidValueSelected: ((picker: Picker, invalidInput: String?, invalidParsedValue: T?, fromButton: Boolean) -> Unit)? =
        null

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

    private lateinit var _setup: INumberPickerSetup<T>
    var setup: INumberPickerSetup<T>
        get() = _setup
        set(value) {
            _setup = value
            onSetupChanged()
        }

    lateinit var value: T
        internal set

    /*
     * returns true, if value was set (either by changing it or because it is already set), false otherwise
     */
    fun setValue(value: T, smooth: Boolean = true, notifyListener: Boolean = true): Boolean {
        if (!setup.isValueAllowed(style, value))
            return false
        val valueChanged = this.value != value
        if (inputView.lazyUI || valueChanged) {
            this.value = value
            onValueChanged(true, smooth, notifyListener && valueChanged)
            return valueChanged
        }
        return true
    }

    fun clearInputFocus() {
        inputView.clearFocus()
        requestFocus()
    }

    // -------------------
    // private state and variables
    // -------------------

    //internal lateinit var state: IState<T>
    private var editTextStyleId: Int = 0
    internal var buttonWidth: Int = 0
    internal var style: MaterialNumberPicker.Style = MaterialNumberPicker.Style.Input
    internal var iconUp: Int = 0
    internal var iconDown: Int = 0
    internal var iconUpLarge: Int = 0
    internal var iconDownLarge: Int = 0

    // Sub Views
    private lateinit var inputView: InputView<T, Picker>

    // -------------------
    // class
    // -------------------

    init {
        //setWillNotDraw(false)

        isSaveEnabled = true

        isFocusable = true
        isFocusableInTouchMode = true

        orientation = HORIZONTAL
        gravity = Gravity.CENTER
    }

    fun init() {

        val array = context.theme.obtainStyledAttributes(
            attrs,
            styleDefinitions.styleAttr,
            defStyleAttr,
            defStyleRes
        )

        try {

            longPressRepeatClicks = array.getBoolean(styleDefinitions.longPressRepeatClicks, true)
            buttonWidth = array.getDimension(styleDefinitions.buttonWidth, 0f).toInt()

            orientation = array.getInteger(styleDefinitions.android_orientation, HORIZONTAL)
            style = MaterialNumberPicker.Style.values()[array.getInteger(styleDefinitions.style, 0)]
            background = array.getDrawable(styleDefinitions.android_background)

            iconUp = array.getResourceId(styleDefinitions.icon_up, 0)
            iconDown = array.getResourceId(styleDefinitions.icon_down, 0)
            iconUpLarge =
                array.getResourceId(styleDefinitions.icon_up_large, 0)
            iconDownLarge =
                array.getResourceId(styleDefinitions.icon_down_large, 0)

            editTextStyleId = array.getResourceId(
                styleDefinitions.editTextStyle,
                R.style.MaterialNumberPicker_EditTextStyle
            )

            _setup = initSetup(array)
            value = initValue(array, setup)

            inflateChildren()
            inputView.updateDisplayedValue(this as Picker, setup, value, false)

        } catch (e: Exception) {
            L.e("MaterialNumberPicker", e)
        } finally {
            array.recycle()
        }
    }

    private fun inflateChildren() {
        inputView = when (style) {
            MaterialNumberPicker.Style.Input -> ViewUtil.initEditTextView(
                this as Picker,
                editTextStyleId
            )
            MaterialNumberPicker.Style.Scroll -> ViewUtil.initScrollViews(this as Picker, null)
        }
    }

    // -----------
    // Events
    // -----------

    private fun onSetupChanged() {
        inputView.onSetupChanged(this as Picker, setup)
    }

    private fun onValueChanged(fromUser: Boolean, smooth: Boolean, notifyListener: Boolean) {
        inputView.updateDisplayedValue(this as Picker, setup, value, smooth)
        if (notifyListener) {
            onValueChangedListener?.invoke(this as Picker, value, fromUser)
        }
    }

    // -----------
    // Others
    // -----------

    internal fun hideKeyboard() {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    internal fun setBackgroundFocused(hasFocus: Boolean) {
        if (hasFocus) {
            background?.state = FOCUSED_STATE_ARRAY
        } else {
            background?.state = UNFOCUSED_STATE_ARRAY
        }
    }

    // -------------------
    // Save/Restore State Events
    // -------------------

    override fun onSaveInstanceState(): Parcelable? {
        val parcel = super.onSaveInstanceState()
        return SavedState(parcel, value)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState<*>) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        value = (state as SavedState<T>).value
        onValueChanged(false, false, false)
    }

    // -------------------
    // ViewState / Classes
    // -------------------

    @Parcelize
    internal class SavedState<T>(
        val superState: Parcelable?,
        val value: T
    ) : Parcelable where T : Number, T : Comparable<T>

    internal class StyleDefinitions(
        val styleAttr: IntArray,
        val longPressRepeatClicks: Int,
        val buttonWidth: Int,
        val style: Int,
        val android_background: Int,
        val android_orientation: Int,
        val icon_up: Int,
        val icon_down: Int,
        val icon_up_large: Int,
        val icon_down_large: Int,
        val editTextStyle: Int
    )
}

