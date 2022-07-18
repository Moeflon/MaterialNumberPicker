package com.michaelflisar.materialnumberpicker.picker

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.R
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax

class IntPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pickerStyle,
    defStyleRes: Int = R.style.MaterialNumberPickerInteger_Horizontal_Filled
) : AbstractMaterialNumberPicker<Int, IntPicker>(context, attrs, defStyleAttr, defStyleRes) {

    override val styleDefinitions = StyleDefinitions(
        R.styleable.MaterialNumberPickerInteger,
        R.styleable.MaterialNumberPickerInteger_mnp_longPressRepeatClicks,
        R.styleable.MaterialNumberPickerInteger_mnp_buttonWidth,
        R.styleable.MaterialNumberPickerInteger_mnp_style,
        R.styleable.MaterialNumberPickerInteger_android_background,
        R.styleable.MaterialNumberPickerInteger_android_orientation,
        R.styleable.MaterialNumberPickerInteger_mnp_iconUp,
        R.styleable.MaterialNumberPickerInteger_mnp_iconDown,
        R.styleable.MaterialNumberPickerInteger_mnp_iconUpLarge,
        R.styleable.MaterialNumberPickerInteger_mnp_iconDownLarge,
        R.styleable.MaterialNumberPickerInteger_mnp_editTextStyle
    )

    init {
        init()
    }

    override fun initSetup(array: TypedArray): INumberPickerSetup<Int> {

        val min = array.getInteger(R.styleable.MaterialNumberPickerInteger_mnp_minInt, 100)
        val max = array.getInteger(R.styleable.MaterialNumberPickerInteger_mnp_maxInt, 0)
        val stepSize = array.getInteger(R.styleable.MaterialNumberPickerInteger_mnp_stepSizeInt, 1)
        val stepSizeLarge =
            array.getInteger(R.styleable.MaterialNumberPickerInteger_mnp_stepSizeLargeInt, 1)
        val prefix = array.getString(R.styleable.MaterialNumberPickerInteger_mnp_prefix) ?: ""
        val suffix = array.getString(R.styleable.MaterialNumberPickerInteger_mnp_suffix) ?: ""
        val scrollerVisibleOffsetItems = array.getInt(
            R.styleable.MaterialNumberPickerInteger_mnp_scrollerVisibleOffsetItems,
            MaterialNumberPicker.DEFAULT_OFFSET_ITEMS
        )

        val formatter = { value: Int ->
            prefix + value.toString() + suffix
        }

        val parser = { value: String ->
            val possibleValueAsString = value.replace(prefix, "").replace(suffix, "")
            possibleValueAsString.toIntOrNull()
        }

        return NumberPickerSetupMinMax(
            if (0 in min..max) 0 else min,
            min,
            max,
            stepSize,
            stepSizeLarge,
            formatter,
            parser,
            scrollerVisibleOffsetItems
        )
    }

    override fun initValue(array: TypedArray, setup: INumberPickerSetup<Int>): Int {
        val value = array.getInteger(R.styleable.MaterialNumberPickerInteger_mnp_valueInt, 0)
        if (!setup.isValueAllowed(style, value))
            return setup.defaultValue
        return value
    }
}