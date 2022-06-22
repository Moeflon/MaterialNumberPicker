package com.michaelflisar.materialnumberpicker.demo.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.demo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoBinding

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        listOf(binding.mnp1, binding.mnp2, binding.mnp3, binding.mnp4, binding.mnp5).forEach {
            it.onValueChangedListener =
                { picker: MaterialNumberPicker, value: Number, fromUser: Boolean ->
                    showToast("New value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected = { invalidValue: Number, fromButton: Boolean ->
                // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                showToast("Invalid value: $invalidValue (button = $fromButton)")
            }
        }

        // reading values
        val currentValue = binding.mnp1.value
        val currentMin = binding.mnp1.min
        val currentMax = binding.mnp1.max

        // updating values
        // mnp4 is of type integer => you can provide any Number but they will be converted to whatever the picker supports!
        binding.mnp4.updateMinMax(100, 1000, 500)
        binding.mnp4.setValue(600) // this triggers the onValueChangedListener!
        binding.mnp4.prefix = "N="
        binding.mnp4.suffix = ""
    }

    private fun showToast(info: String) {
        toast?.cancel()
        toast = Toast.makeText(this, info, Toast.LENGTH_SHORT)
        toast?.show()
    }
}