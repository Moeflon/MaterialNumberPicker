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

    override val styleAttr = R.styleable.MaterialNumberPickerInteger
    override val styleAttr_longPressRepeatClicks =
        R.styleable.MaterialNumberPickerInteger_mnp_longPressRepeatClicks
    override val styleAttr_buttonWidth = R.styleable.MaterialNumberPickerInteger_mnp_buttonWidth
    override val styleAttr_style = R.styleable.MaterialNumberPickerInteger_mnp_style
    override val styleAttr_android_background =
        R.styleable.MaterialNumberPickerInteger_android_background
    override val styleAttr_android_orientation =
        R.styleable.MaterialNumberPickerInteger_android_orientation
    override val styleAttr_icon_up = R.styleable.MaterialNumberPickerInteger_mnp_iconUp
    override val styleAttr_icon_down = R.styleable.MaterialNumberPickerInteger_mnp_iconDown
    override val styleAttr_icon_up_large =
        R.styleable.MaterialNumberPickerInteger_mnp_iconUpLarge
    override val styleAttr_icon_down_large =
        R.styleable.MaterialNumberPickerInteger_mnp_iconDownLarge
    override val styleAttr_editTextStyle =
        R.styleable.MaterialNumberPickerInteger_mnp_editTextStyle

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
        val scrollerVisibleOffsetItems = array.getInt(R.styleable.MaterialNumberPickerInteger_mnp_scrollerVisibleOffsetItems, MaterialNumberPicker.DEFAULT_OFFSET_ITEMS)

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