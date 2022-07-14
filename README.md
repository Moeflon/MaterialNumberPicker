### About

[![Release](https://jitpack.io/v/MFlisar/MaterialNumberPicker.svg)](https://jitpack.io/#MFlisar/MaterialNumberPicker)
![License](https://img.shields.io/github/license/MFlisar/MaterialNumberPicker)

### Introduction

This view allows the user to select a number (`int` or `float`) from within a predefined range (min..max). Additionally it has following features:

* looks like a material `TextInputLayout` and uses the same styles
* offer 3 types of styles: outline, filled, none
* allows to show 1 or 2 buttons for in-/decreasing numbers in small or large steps
* allows to show custom prefix/suffix texts
* stylable

Check out the demo app to see it in action and to see how you can set up the view in xml and work with it in kotlin.

Here's a 30s gif, showing some pickers in action:

<img src="https://github.com/MFlisar/MaterialNumberPicker/blob/main/media/demo.gif?raw=true" width="400">

### Example

Here's a list of existing styles:

* Integer Types
	* `style="@style/MaterialNumberPickerInteger.Horizontal.Filled"`
	* `style="@style/MaterialNumberPickerInteger.Horizontal.Outlined"`
	* `style="@style/MaterialNumberPickerInteger.Horizontal.None"`
	* `style="@style/MaterialNumberPickerInteger.Vertical.Filled"`
	* `style="@style/MaterialNumberPickerInteger.Vertical.Outlined"`
	* `style="@style/MaterialNumberPickerInteger.Vertical.None"`
* Float Types
	* `style="@style/MaterialNumberPickerFloat.Horizontal.Filled"`
	* `style="@style/MaterialNumberPickerFloat.Horizontal.Outlined"`
	* `style="@style/MaterialNumberPickerFloat.Horizontal.None"`
	* `style="@style/MaterialNumberPickerFloat.Vertical.Filled"`
	* `style="@style/MaterialNumberPickerFloat.Vertical.Outlined"`
	* `style="@style/MaterialNumberPickerFloat.Vertical.None"`
	
```xml
<com.michaelflisar.materialnumberpicker.picker.IntPicker
                                         
    android:id="@+id/picker"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
	
    // required
    style="@style/MaterialNumberPickerInteger.Horizontal.Outlined" // one of the CORRECT (int in this case) styles mentioned above
	
	// style
	 app:mnp_style="input" // decide if you want a input type or scroller type number picker
	
	// values - must end on Int or Float depending on the picker type => use "app:mnp_*Float" for the FloatPicker!!!
	app:mnp_valueInt="50" // initial value of the picker
    app:mnp_minInt="0" // min allowed value of the picker
    app:mnp_maxInt="9999" // max allowed value of the picker
    app:mnp_stepSizeInt="1" // step size for the up/down button
    app:mnp_stepSizeLargeInt="5" // step size for the secondary up/down buttons for large steps (will be hidden if value is equal to stepSize!)
	
    // optional
    app:mnp_prefix="" // a prefix that will be shown before the number
    app:mnp_suffix="" // a suffix that will be shown after the number
    app:mnp_buttonWidth="0dp" // defines the widths of the buttons, 0 for auto width                                                         
    app:mnp_editTextStyle="@style/MaterialNumberPicker.EditTextStyle" // if desired you can provide your own style for the EditText
    app:mnp_icon_up="@drawable/..." // provide a custom drawable for the increase button
    app:mnp_icon_down="@drawable/..." // provide a custom drawable for the decrease button
    app:mnp_icon_up_large="@drawable/..." // provide a custom drawable for the increase in large steps button
    app:mnp_icon_down_large="@drawable/..." // provide a custom drawable for the decrease  in large steps button
    app:mnp_longPressRepeatClicks="true" // enable/disable the repetitive function of the button if it is hold down
	
	//  only relevant for the float picker!
	app:mnp_commas="2" // used for the display formatter
	
    />
```

```kotlin
// Listeners (example for int pickers, for float pickers replace Int with Float!)
picker.onValueChangedListener = { picker: IntPicker, value: Int, fromUser: Boolean ->
    // listen to value change events
}
picker.onInvalidValueSelected = { picker: IntPicker, invalidInput: String?, invalidValue: Int?, fromButton: Boolean ->
    // listen to invalid values events if the user tries to input an invalid number or one that's outside of the min/max range
	// invalidInput may provide the input (if error comes not from button) the user is trying to use
    // if fromButton is true, it means the user reached the min/max by pressing the button, if desired, you can react on this here as well
}
picker.focusChangedListener = { focus: Boolean -> 
    // the inner EditText got the focus
}

// get values
val currentValue = picker.value
val currentSetup = picker.setup
// by default (from XML) the setup is always a min/max setup => if you change the setup programmatically, be more cautios here than me!
val currentMinMaxSetup = currentSetup as NumberPickerSetupMinMax<Int>
val currentMin = currentMinMaxSetup.min
val currentMax = currentMinMaxSetup.max
val currentStepSize = currentMinMaxSetup.stepSize
val currentStepSizeLarge = currentMinMaxSetup.stepSizeSecondary

// setter/updater
picker.setup = NumberPickerSetupMinMax(
	MaterialNumberPicker.DataType.Int, // make sure this fits the Type!! currently this must be ensured manually
	100,
	100,
	1000,
	10,
	50, // select same value as stepSize to disable secondary buttons
	{ "N=" + it.toString() + "s" }, // custom formatter, in this case it adds prefix and suffix
	{ it.toIntOrNull() },
	2
)

picker.longPressRepeatClicks = true // enabled by default, if disabled, long pressing a button won't repeat its action
picker.repeatClicksFirstDelay = 300 // initial delay after which long presses will trigger the click event 
picker.repeatClicksConsecutiveDelay = 100 // consecutive delay after which long presses will trigger the click event 

picker.closeKeyboardOnUpDownClicks = true // if enabled, an eventually opened keyboard will be closed if one of the buttons is clicked
picker.closeKeyboardOnNewValueSet = true // if enabled, an eventually opened keyboard will be closed if the pickers value is changed (internally or by the user)

// others
picker.clearInputFocus() // remove the focus from the inner EditText

```

### Gradle (via [JitPack.io](https://jitpack.io/))

1. add jitpack to your project's `build.gradle`:
```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```
2. add the compile statement to your module's `build.gradle`:
```groovy
dependencies {
  implementation "com.github.MFlisar:MaterialNumberPicker:<LATEST-VERSION>"
}
```
  
### Credits

Idea is based on following library: https://github.com/sephiroth74/NumberSlidingPicker and styling is copied from there, so thanks a lot to sephiroth74 for his repository.