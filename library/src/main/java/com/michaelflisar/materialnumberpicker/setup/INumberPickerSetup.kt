package com.michaelflisar.materialnumberpicker.setup

import com.michaelflisar.materialnumberpicker.MaterialNumberPicker

interface INumberPickerSetup<T> where T : Number, T : Comparable<T> {

    enum class Button {
        Up, Down
    }

    interface ButtonProvider<T> {
        fun calcPrimaryButtonResult(currentValue: T, button: Button): T?
    }

    interface SecondaryButtonProvider<T> {
        fun supportsSecondaryButton() = true
        fun calcSecondaryButtonResult(currentValue: T, button: Button): T?
    }

    val type: MaterialNumberPicker.DataType
    val defaultValue: T
    val formatter: (value: T) -> String
    val parser: (value: String) -> T?
    val scrollerVisibleOffsetItems: Int

    fun isValueAllowed(style: MaterialNumberPicker.Style, value: T?): Boolean

    val longestValue: T
    val allValidValuesSorted: List<T>
}

