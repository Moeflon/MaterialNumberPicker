package com.michaelflisar.materialnumberpicker.setup

import android.util.Log
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.internal.minus
import com.michaelflisar.materialnumberpicker.internal.plus

class NumberPickerSetupMinMax<T>(
    override val type: MaterialNumberPicker.DataType,
    override val defaultValue: T,
    val min: T,
    val max: T,
    val stepSize: T,
    val stepSizeSecondary: T = stepSize,
    override val formatter: (value: T) -> String,
    override val parser: (value: String) -> T?,
    override val scrollerVisibleOffsetItems: Int
) : INumberPickerSetup<T>,
    INumberPickerSetup.ButtonProvider<T>,
    INumberPickerSetup.SecondaryButtonProvider<T> where T : Number, T : Comparable<T> {

    override fun isValueAllowed(value: T?) = value != null && value in min..max

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