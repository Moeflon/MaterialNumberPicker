package com.michaelflisar.materialnumberpicker.demo.activities

import android.app.Application
import android.util.Log
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // logger... works lazyly, if no logger is supplied, library won't do unnecessary work
        MaterialNumberPicker.LOGGER = { level, tag, info ->
            Log.println(level, "MNP-$tag", info)
        }
    }
}