package com.michaelflisar.materialnumberpicker.demo.activities.classes

import android.util.Log
import android.widget.Toast
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker

object Util {

    private var toast: Toast? = null

    fun logInfo(picker: AbstractMaterialNumberPicker<*, *>, info: String) {
        val name = picker.context.resources.getResourceName(picker.id).substringAfterLast(":id/")
        Log.d("LOG INFO", "[$name] $info")
    }

    fun showToast(picker: AbstractMaterialNumberPicker<*, *>, info: String) {
        val name = picker.context.resources.getResourceName(picker.id).substringAfterLast(":id/")
        val fullInfo = "[$name] $info"
        Log.d("SHOW TOAST", fullInfo)
        toast?.cancel()
        toast = Toast.makeText(picker.context, fullInfo, Toast.LENGTH_SHORT)
        toast?.show()
    }

}