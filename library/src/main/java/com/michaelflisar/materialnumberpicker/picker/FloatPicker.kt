package com.michaelflisar.materialnumberpicker.picker

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.R
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax

class FloatPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.pickerStyle,
    defStyleRes: Int = R.style.MaterialNumberPickerFloat_Horizontal_Filled
) : AbstractMaterialNumberPicker<kotlin.Float, FloatPicker>(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    override val styleAttr = R.styleable.MaterialNumberPickerFloat
    override val styleAttr_longPressRepeatClicks =
        R.styleable.MaterialNumberPickerFloat_mnp_longPressRepeatClicks
    override val styleAttr_buttonWidth = R.styleable.MaterialNumberPickerFloat_mnp_buttonWidth
    override val styleAttr_style = R.styleable.MaterialNumberPickerFloat_mnp_style
    override val styleAttr_android_background =
        R.styleable.MaterialNumberPickerFloat_android_background
    override val styleAttr_android_orientation =
        R.styleable.MaterialNumberPickerFloat_android_orientation
    override val styleAttr_icon_up = R.styleable.MaterialNumberPickerFloat_mnp_iconUp
    override val styleAttr_icon_down = R.styleable.MaterialNumberPickerFloat_mnp_iconDown
    override val styleAttr_icon_up_large = R.styleable.MaterialNumberPickerFloat_mnp_iconUpLarge
    override val styleAttr_icon_down_large =
        R.styleable.MaterialNumberPickerFloat_mnp_iconDownLarge
    override val styleAttr_editTextStyle = R.styleable.MaterialNumberPickerFloat_mnp_editTextStyle

    init {
        init()
    }

    override fun initSetup(array: TypedArray): INumberPickerSetup<Float> {

        val min = array.getFloat(R.styleable.MaterialNumberPickerFloat_mnp_minFloat, 100f)
        val max = array.getFloat(R.styleable.MaterialNumberPickerFloat_mnp_maxFloat, 0f)
        val stepSize = array.getFloat(R.styleable.MaterialNumberPickerFloat_mnp_stepSizeFloat, 1f)
        val stepSizeLarge =
            array.getFloat(R.styleable.MaterialNumberPickerFloat_mnp_stepSizeLargeFloat, 1f)
        val prefix = array.getString(R.styleable.MaterialNumberPickerFloat_mnp_prefix) ?: ""
        val suffix = array.getString(R.styleable.MaterialNumberPickerFloat_mnp_suffix) ?: ""
        val commas = array.getInt(R.styleable.MaterialNumberPickerFloat_mnp_commas, -1)
        val scrollerVisibleOffsetItems = array.getInt(R.styleable.MaterialNumberPickerFloat_mnp_scrollerVisibleOffsetItems, 0)

        val formatter = { value: Float ->
            val valueAsString = commas.takeIf { it >= 0 }?.let {
                String.format("%.${it}f", value)
            } ?: value.toString()
            prefix + valueAsString + suffix
        }

        val parser = { value: String ->
            val possibleValueAsString = value.replace(prefix, "").replace(suffix, "")
            possibleValueAsString.toFloatOrNull()
        }

        return NumberPickerSetupMinMax(
            if (0f in min..max) 0f else min,
            min,
            max,
            stepSize,
            stepSizeLarge,
            formatter,
            parser,
            scrollerVisibleOffsetItems
        )
    }

    override fun initValue(array: TypedArray, setup: INumberPickerSetup<Float>): Float {
        val value = array.getFloat(R.styleable.MaterialNumberPickerFloat_mnp_valueFloat, 0f)
        if (!setup.isValueAllowed(style, value))
            return setup.defaultValue
        return value
    }
}