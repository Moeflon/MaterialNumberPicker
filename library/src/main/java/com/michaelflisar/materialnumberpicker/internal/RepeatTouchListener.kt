package com.michaelflisar.materialnumberpicker.internal

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker

internal class RepeatTouchListener(
    private val view: View,
    private val picker: AbstractMaterialNumberPicker<*, *>,
    private val onEvent: (view: View) -> Boolean
) : View.OnTouchListener {

    private lateinit var handlerRunnable: Runnable
    private val gestureDetectorCompat: GestureDetectorCompat

    init {
        handlerRunnable = Runnable {
            if (view.isEnabled) {
                val success = onEvent(view)
                if (success) {
                    view.handler.postDelayed(
                        handlerRunnable,
                        picker.repeatClicksConsecutiveDelay.toLong()
                    )
                } else {
                    stop()
                }
            } else {
                stop()
            }
        }
        gestureDetectorCompat =
            GestureDetectorCompat(view.context, object : GestureDetector.OnGestureListener {
                override fun onDown(p0: MotionEvent?): Boolean {
                    return true
                }

                override fun onShowPress(p0: MotionEvent?) {
                }

                override fun onSingleTapUp(p0: MotionEvent?): Boolean {
                    onEvent(view)
                    return true
                }

                override fun onScroll(
                    p0: MotionEvent?,
                    p1: MotionEvent?,
                    p2: Float,
                    p3: Float
                ): Boolean {
                    return false
                }

                override fun onLongPress(p0: MotionEvent?) {
                }

                override fun onFling(
                    p0: MotionEvent?,
                    p1: MotionEvent?,
                    p2: Float,
                    p3: Float
                ): Boolean {
                    return false
                }
            })
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // we don't care about the return value, we handle this outself
        // detector is used to distinct between touches (like they happen on scrolling the container) and real clicks only
        gestureDetectorCompat.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.handler.removeCallbacks(handlerRunnable)
                view.handler.postDelayed(handlerRunnable, picker.repeatClicksFirstDelay.toLong())
                view.isPressed = true
                //onEvent(view)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stop()
                return true
            }
        }
        return false
    }

    private fun stop() {
        view.handler.removeCallbacks(handlerRunnable)
        view.isPressed = false
    }
}