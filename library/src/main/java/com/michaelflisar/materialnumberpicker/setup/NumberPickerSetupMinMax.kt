package com.michaelflisar.materialnumberpicker.setup

import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.internal.minus
import com.michaelflisar.materialnumberpicker.internal.plus

class NumberPickerSetupMinMax<T>(
    override val defaultValue: T,
    val min: T,
    val max: T,
    val stepSize: T,
    val stepSizeSecondary: T = stepSize,
    override val formatter: (value: T) -> String,
    override val parser: (value: String) -> T?,
    override val scrollerVisibleOffsetItems: Int = MaterialNumberPicker.DEFAULT_OFFSET_ITEMS
) : INumberPickerSetup<T>,
    INumberPickerSetup.ButtonProvider<T>,
    INumberPickerSetup.SecondaryButtonProvider<T> where T : Number, T : Comparable<T> {

    override val type = MaterialNumberPicker.DataType.fromObject(defaultValue)

    override fun isValueAllowed(style: MaterialNumberPicker.Style, value: T?): Boolean {
        return value != null && when (style) {
            MaterialNumberPicker.Style.Input -> value in min..max
            MaterialNumberPicker.Style.Scroll -> allValidValuesSorted.contains(value)
        }
    }

    override fun supportsSecondaryButton() = stepSize != stepSizeSecondary

    override fun calcPrimaryButtonResult(currentValue: T, button: INumberPickerSetup.Button): T? {
        return when (button) {
            INumberPickerSetup.Button.Up -> currentValue + stepSize
            INumberPickerSetup.Button.Down -> currentValue - stepSize
        }
    }

    override fun calcSecondaryButtonResult(currentValue: T, button: INumberPickerSetup.Button): T? {
        return when (button) {
            INumberPickerSetup.Button.Up -> currentValue + stepSizeSecondary
            INumberPickerSetup.Button.Down -> currentValue - stepSizeSecondary
        }
    }

    override val longestValue: T by lazy {
        allValidValuesSorted.sortedBy { formatter(it).length }.last()
    }

    override val allValidValuesSorted: List<T> by lazy {
        val set = HashSet<T>()
        var value = min
        while (value < max) {
            set.add(value)
            value += stepSize
        }
        value = min
        while (value < max) {
            set.add(value)
            value += stepSizeSecondary
        }
        set.add(max)
        set.toSortedSet().toList()
    }

}