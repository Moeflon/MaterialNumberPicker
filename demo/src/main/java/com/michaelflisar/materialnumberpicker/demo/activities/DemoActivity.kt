package com.michaelflisar.materialnumberpicker.demo.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.michaelflisar.materialnumberpicker.MaterialNumberPicker
import com.michaelflisar.materialnumberpicker.demo.databinding.ActivityDemoBinding
import com.michaelflisar.materialnumberpicker.picker.FloatPicker
import com.michaelflisar.materialnumberpicker.picker.IntPicker
import com.michaelflisar.materialnumberpicker.setup.INumberPickerSetup
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax
import kotlin.math.ceil
import kotlin.math.floor

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

        // Float Pickers
        listOf(binding.mnp2, binding.mnp5, binding.mnp12, binding.mnp14).forEach {
            it.onValueChangedListener =
                { picker: FloatPicker, value: Float, fromUser: Boolean ->
                    showToast("New float value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: FloatPicker, invalidInput: String?, invalidValue: Float?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    showToast("Invalid float value: $invalidInput | $invalidValue (button = $fromButton)")
                }
        }

        // Int Pickers
        listOf(binding.mnp1, binding.mnp3, binding.mnp4, binding.mnp11, binding.mnp13).forEach {
            it.onValueChangedListener =
                { picker: IntPicker, value: Int, fromUser: Boolean ->
                    showToast("New int value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: IntPicker, invalidInput: String?, invalidValue: Int?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    showToast("Invalid int value: $invalidInput | $invalidValue (button = $fromButton)")
                }
        }

        // reading values
        val currentValue = binding.mnp1.value
        val currentSetup = binding.mnp1.setup
        // by default (from XML) the setup is always a min/max setup => if you change the setup programmatically, be more cautios here than me!
        val currentMinMaxSetup = currentSetup as NumberPickerSetupMinMax<Int>
        val currentMin = currentMinMaxSetup.min
        val currentMax = currentMinMaxSetup.max
        val currentStepSize = currentMinMaxSetup.stepSize
        val currentStepSizeLarge = currentMinMaxSetup.stepSizeSecondary

        // updating values
        // mnp4 is of type integer => you can provide any Number but they will be converted to whatever the picker supports!
        binding.mnp4.setup = NumberPickerSetupMinMax(
            100,
            100,
            1000,
            10,
            50, // select same value as stepSize to disable secondary buttons
            { "N=" + it.toString() + "s" }, // custom formatter, in this case it adds prefix and suffix
            { it.toIntOrNull() },
            2
        )

        if (savedInstanceState == null) {
            binding.mnp4.setValue(600) // this triggers the onValueChangedListener!
        }

        // example custom setup
        binding.mnp11.setup = NumberPickerSetupMinMax(
            5,
            0,
            100,
            1,
            2,
            { it.toString() + "x" },
            { it.replace("x", "").toIntOrNull() },
            2
        )
        if (savedInstanceState == null) {
            binding.mnp11.setValue(17)
        }

        // example with custom setup provider including special list of values and suffix
        binding.mnp12.setup = WeightNumberPickerSetup(50f)

        if (savedInstanceState == null) {
            binding.mnp12.setValue(
                70f,
                false /* we want to immediately set the new value and avoid the initial scrolling animation here */
            )
        }

        binding.btIncrease.setOnClickListener {
            binding.mnp11.setValue(binding.mnp11.value + 1)
            binding.mnp12.setValue(binding.mnp12.value + 5)
            binding.mnp13.setValue(binding.mnp13.value + 1)
            binding.mnp14.setValue(binding.mnp14.value + 1f)
        }
        binding.btDecrease.setOnClickListener {
            binding.mnp11.setValue(binding.mnp11.value - 1)
            binding.mnp12.setValue(binding.mnp12.value - 5)
            binding.mnp13.setValue(binding.mnp13.value - 1)
            binding.mnp14.setValue(binding.mnp14.value - 1f)
        }
    }

    private fun showToast(info: String) {
        Log.d("SHOW TOAST", info)
        toast?.cancel()
        toast = Toast.makeText(this, info, Toast.LENGTH_SHORT)
        toast?.show()
    }

    /*
     * Provides weights like following:
     * 0..10kg:     1kg steps
     * 10..20kg:    1kg + 2.5kg steps
     * 20..50kg:    2.5kg steps
     * 50..1000kg   5kg steps
     */
    class WeightNumberPickerSetup(
        override val defaultValue: Float
    ) : INumberPickerSetup<Float>/*,
        INumberPickerSetup.ButtonProvider<Float>,
        INumberPickerSetup.SecondaryButtonProvider<Float> */ {

        override val type = MaterialNumberPicker.DataType.Float
        override val scrollerVisibleOffsetItems = 2

        override val formatter = { value: Float ->
            if (value.toInt().toFloat() == value) "${value.toInt()}kg" else "${value}kg"
        }

        override val parser = { value: String -> value.replace("kg", "").toFloatOrNull() }

        /*
        override fun calcPrimaryButtonResult(
            currentValue: Float,
            button: INumberPickerSetup.Button
        ): Float? {
        }

        override fun calcSecondaryButtonResult(
            currentValue: Float,
            button: INumberPickerSetup.Button
        ): Float? {
        }*/

        override fun isValueAllowed(value: Float?): Boolean {
            if (value == null || value < 0f || value > 1000f)
                return false
            // 0..10kg => all values are allowed, if the are natural numbers
            if (value <= 10f)
                return ceil(value) == floor(value)
            // 10..20 => all values are allowed, if the are natural numbers or divedable by 2.5
            else if (value <= 20f)
                return ceil(value) == floor(value) || (value % 2.5) == 0.0
            // 20..50 => all values are allowed, if they are divedable by 2.5
            else if (value <= 20f)
                return (value % 2.5) == 0.0
            // 50..1000 => all values are allowed, if they are divedable by 5
            else
                return (value % 5.0) == 0.0
        }

        override val longestValue = 5555.5f

        override val allValidValuesSorted by lazy {
            val values = ArrayList<Float>()

            // 0..10
            var value = 0f
            while (value < 10f) {
                values.add(value++)
            }

            // 10..20
            value = 10f
            while (value < 20) {
                if (value == 13f)
                    values.add(12.5f)
                else if (value == 18f)
                    values.add(17.5f)
                values.add(value++)
            }

            // 20..50
            value = 20f
            while (value < 50f) {
                values.add(value)
                value += 2.5f
            }

            // 50..1000
            value = 50f
            while (value <= 1000f) {
                values.add(value)
                value += 5f
            }

            values.sorted()
        }
    }
}