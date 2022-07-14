package com.michaelflisar.materialnumberpicker.setup

import com.michaelflisar.materialnumberpicker.MaterialNumberPicker

class NumberPickerSetupList<T>(
    override val type: MaterialNumberPicker.DataType,
    override val defaultValue: T,
    val values: List<T>,
    override val formatter: (value: T) -> String,
    override val parser: (value: String) -> T?,
    override val scrollerVisibleOffsetItems: Int
) : INumberPickerSetup<T>,
    INumberPickerSetup.ButtonProvider<T> where T : Number, T : Comparable<T> {

    init {
        if (values.isEmpty() || !values.contains(defaultValue)) {
            throw RuntimeException("The provided list must be not empty and must contain the provided default value!")
        }
    }

    override fun isValueAllowed(value: T?) = value != null && values.contains(value)

    override fun calcPrimaryButtonResult(currentValue: T, button: INumberPickerSetup.Button): T? {
        val index = values.indexOf(currentValue)
        if (index == -1)
            return currentValue
        return when (button) {
            INumberPickerSetup.Button.Up -> if (index == values.size - 1) null else values[index + 1]
            INumberPickerSetup.Button.Down -> if (index == 0) null else values[index - 1]
        }
    }

    override val longestValue: T by lazy {
        allValidValuesSorted.sortedBy { formatter(it).length }.last()
    }

    override val allValidValuesSorted = values
}