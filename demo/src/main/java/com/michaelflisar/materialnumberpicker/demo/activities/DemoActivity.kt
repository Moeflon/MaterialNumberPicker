package com.michaelflisar.materialnumberpicker.demo.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.demo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoBinding

    private val toast by lazy {
        Toast.makeText(this, "", Toast.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        listOf(binding.mnp1, binding.mnp2, binding.mnp3, binding.mnp4).forEach {
            it.onValueChangedListener =
                { picker: MaterialNumberPicker, value: Float, fromUser: Boolean ->
                    showToast("New value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected = { invalidValue: Float, fromButton: Boolean ->
                // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                showToast("Invalid value: $invalidValue (button = $fromButton)")
            }
        }
    }

    private fun showToast(info: String) {
        toast.cancel()
        toast.setText(info)
        toast.show()
    }
}