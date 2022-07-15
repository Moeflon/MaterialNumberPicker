package com.michaelflisar.materialnumberpicker.internal

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View

internal operator fun <T> Number.plus(adjustment: T): T {
    return when (adjustment) {
        is Int -> ((this as Int) + adjustment)
        is Float -> ((this as Float) + adjustment)
        else -> throw RuntimeException("Invalid number class!")
    } as T
}

internal operator fun <T> Number.minus(adjustment: T): T {
    return when (adjustment) {
        is Int -> ((this as Int) - adjustment)
        is Float -> ((this as Float) - adjustment)
        else -> throw RuntimeException("Invalid number class!")
    } as T
}

internal fun <T> Number.isEqual(other: T?): Boolean {
    return when (this) {
        is Int -> (this as Int) == other
        is Float -> (this as Float) == other
        else -> throw RuntimeException("Invalid number class!")
    }
}

internal fun Context.getIntAttr(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return TypedValue.complexToDimensionPixelSize(
        typedValue.data,
        resources.displayMetrics
    )
}

internal fun Context.getColorAttr(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

internal fun Context.getDimen(dimen: Int): Float {
    return resources.getDimension(dimen)
}

internal val Int.pxToDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
internal val Int.dpToPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

internal inline fun View.doOnNextLayout(crossinline action: (view: View) -> Unit): View.OnLayoutChangeListener {
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            view.removeOnLayoutChangeListener(this)
            action(view)
        }
    }
    addOnLayoutChangeListener(listener)
    return listener
}

internal fun View.cancelOnNextLayout(listener: View.OnLayoutChangeListener) {
    removeOnLayoutChangeListener(listener)
}