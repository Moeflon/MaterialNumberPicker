package com.michaelflisar.materialnumberpicker.internal

import android.util.Log
import android.view.View
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker

internal object L {

    internal fun d(tag: String, view: View, info: () -> String) {
        MaterialNumberPicker.LOGGER?.let {
            val id = view.context.resources.getResourceName(view.id).substringAfterLast(":id/")
            it(Log.DEBUG, tag, "[$id] ${info()}")
        }
    }

    internal fun d(tag: String, info: () -> String) {
        MaterialNumberPicker.LOGGER?.let {
            it(Log.DEBUG, tag, info())
        }
    }

    internal fun e(tag: String, exception: Exception) {
        MaterialNumberPicker.LOGGER?.let {
            it(Log.ERROR, tag, exception.toString() + "\n" + Log.getStackTraceString(exception))
        }
    }
}