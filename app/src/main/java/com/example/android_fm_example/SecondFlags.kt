package com.example.android_fm_example

import io.rollout.configuration.RoxContainer
import io.rollout.flags.RoxFlag
import io.rollout.flags.RoxString
import io.rollout.flags.RoxInt
import io.rollout.flags.RoxDouble
import io.rollout.flags.RoxEnum

// Second Flags container for demonstrating multiple instances
class SecondFlags : RoxContainer {
    init {
        android.util.Log.d("SecondFlags", "Creating SecondFlags container")
    }
    // Define different feature flags for second instance
    val showSecondMessage = RoxFlag(false)
    val secondMessage = RoxString("This is second instance message!")
    val secondFontColor = RoxString("Red", arrayOf("White", "Blue", "Green", "Yellow", "Red"))
    val secondFontSize = RoxInt(18, intArrayOf(14, 18, 24))
    val secondSpecialNumber = RoxDouble(2.71, doubleArrayOf(2.71, 3.14, 0.577))
    val secondTitleColorsEnum = RoxEnum<Color>(Color.RED)

    enum class Color {
        WHITE, BLUE, GREEN, YELLOW, RED
    }
}
