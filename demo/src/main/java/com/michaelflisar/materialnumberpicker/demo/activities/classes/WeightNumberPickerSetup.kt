package com.michaelflisar.materialnumberpicker.demo.activities.classes

import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import kotlin.math.ceil
import kotlin.math.floor

/*
 * Provides weights like following:
 * 0..10kg:     1kg steps
 * 10..20kg:    1kg + 2.5kg steps
 * 20..50kg:    2.5kg steps
 * 50..1000kg   5kg steps
 */
class WeightNumberPickerSetup(
    override val defaultValue: Float
) : INumberPickerSetup<Float>/*,
    INumberPickerSetup.ButtonProvider<Float>,
    INumberPickerSetup.SecondaryButtonProvider<Float> */ {

    override val type = MaterialNumberPicker.DataType.Float
    override val scrollerVisibleOffsetItems = 2

    override val formatter = { value: Float ->
        if (value.toInt().toFloat() == value) "${value.toInt()}kg" else "${value}kg"
    }

    override val parser = { value: String -> value.replace("kg", "").toFloatOrNull() }

    /*
    override fun calcPrimaryButtonResult(
        currentValue: Float,
        button: INumberPickerSetup.Button
    ): Float? {
    }

    override fun calcSecondaryButtonResult(
        currentValue: Float,
        button: INumberPickerSetup.Button
    ): Float? {
    }*/

    override fun isValueAllowed(style: MaterialNumberPicker.Style, value: Float?): Boolean {
        if (value == null || value < 0f || value > 1000f)
            return false
        // 0..10kg => all values are allowed, if the are natural numbers
        if (value <= 10f)
            return ceil(value) == floor(value)
        // 10..20 => all values are allowed, if the are natural numbers or divedable by 2.5
        else if (value <= 20f)
            return ceil(value) == floor(value) || (value % 2.5) == 0.0
        // 20..50 => all values are allowed, if they are divedable by 2.5
        else if (value <= 20f)
            return (value % 2.5) == 0.0
        // 50..1000 => all values are allowed, if they are divedable by 5
        else
            return (value % 5.0) == 0.0
    }

    override val longestValue = 5555.5f

    override val allValidValuesSorted by lazy {
        val values = ArrayList<Float>()

        // 0..10
        var value = 0f
        while (value < 10f) {
            values.add(value++)
        }

        // 10..20
        value = 10f
        while (value < 20) {
            if (value == 13f)
                values.add(12.5f)
            else if (value == 18f)
                values.add(17.5f)
            values.add(value++)
        }

        // 20..50
        value = 20f
        while (value < 50f) {
            values.add(value)
            value += 2.5f
        }

        // 50..1000
        value = 50f
        while (value <= 1000f) {
            values.add(value)
            value += 5f
        }

        values.sorted()
    }
}