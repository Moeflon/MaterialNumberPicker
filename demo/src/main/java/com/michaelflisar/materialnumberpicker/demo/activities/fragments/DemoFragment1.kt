package com.michaelflisar.materialnumberpicker.demo.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.michaelflisar.materialnumberpicker.demo.activities.classes.Util
import com.michaelflisar.materialnumberpicker.demo.activities.classes.WeightNumberPickerSetup
import com.michaelflisar.materialnumberpicker.demo.databinding.Fragment1Binding
import com.michaelflisar.materialnumberpicker.picker.FloatPicker
import com.michaelflisar.materialnumberpicker.picker.IntPicker
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random

class DemoFragment1 : Fragment() {

    private lateinit var binding: Fragment1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Fragment1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickersFloat =
            listOf(binding.mnp2, binding.mnp5, binding.mnp6, binding.mnp12, binding.mnp14)
        val pickersInt =
            listOf(binding.mnp1, binding.mnp3, binding.mnp4, binding.mnp11, binding.mnp13)

        // Float Pickers
        pickersFloat.forEach {
            it.onValueChangedListener =
                { picker: FloatPicker, value: Float, fromUser: Boolean ->
                    Util.showToast(picker, "New float value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: FloatPicker, invalidInput: String?, invalidValue: Float?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    Util.showToast(
                        picker,
                        "Invalid float value: $invalidInput | $invalidValue (button = $fromButton)"
                    )
                }
        }

        // Int Pickers
        pickersInt.forEach {
            it.onValueChangedListener =
                { picker: IntPicker, value: Int, fromUser: Boolean ->
                    Util.showToast(picker, "New int value: $value (user = $fromUser)")
                }
            it.onInvalidValueSelected =
                { picker: IntPicker, invalidInput: String?, invalidValue: Int?, fromButton: Boolean ->
                    // most likely, if fromButton == true, you don't want to handle this, but fir the demo we show a message in every case
                    Util.showToast(
                        picker,
                        "Invalid int value: $invalidInput | $invalidValue (button = $fromButton)"
                    )
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
            { it.toIntOrNull() }
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
            { it.replace("x", "").toIntOrNull() }
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

        val stepsToTryFloat = listOf(0.5f, 1f, 2.5f, 5f)
        val stepsToTryInt = stepsToTryFloat.filter { ceil(it) == floor(it) }.map { it.toInt() }

        val updatePickers = { factor: Int ->
            pickersInt.forEach {
                for (adjustment in stepsToTryInt) {
                    if (it.setValue(it.value + adjustment * factor)) {
                        Util.logInfo(it, "Accepted value: ${it.value}")
                        break
                    }
                }
            }
            pickersFloat.forEach {
                for (adjustment in stepsToTryFloat) {
                    if (it.setValue(it.value + adjustment * factor)) {
                        Util.logInfo(it, "Accepted value: ${it.value}")
                        break
                    }
                }
            }
        }
        val setPickers = { value: Int ->
            pickersInt.forEach {
                it.setValue(value)
            }
            pickersFloat.forEach {
                it.setValue(value.toFloat())
            }
        }

        binding.btIncrease.setOnClickListener {
            updatePickers(1)
        }
        binding.btDecrease.setOnClickListener {
            updatePickers(-1)
        }
        binding.bt10.setOnClickListener {
            setPickers(10)
        }
        binding.bt25.setOnClickListener {
            setPickers(25)
        }
        binding.bt50.setOnClickListener {
            setPickers(50)
        }
        binding.btSetupAndValue.setOnClickListener {
            val randomChar = (65 + Random.nextInt(0, 25)).toChar()
            binding.mnp14.setup = NumberPickerSetupMinMax(
                10f,
                0f,
                100f,
                1f,
                1f,
                { "${it}$randomChar" },
                { it.replace("$randomChar", "").toFloatOrNull() }
            )
            val randomValue = Random.nextInt(0, 100).toFloat()
            Util.logInfo(binding.mnp14, "Random value: $randomValue | randomChar: $randomChar")
            binding.mnp14.setValue(randomValue, false)
        }
    }

}