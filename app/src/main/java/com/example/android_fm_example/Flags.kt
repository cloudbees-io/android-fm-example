package com.example.android_fm_example

import io.rollout.configuration.RoxContainer
import io.rollout.flags.RoxFlag
import io.rollout.flags.RoxString
import io.rollout.flags.RoxInt
import io.rollout.flags.RoxDouble
import io.rollout.flags.RoxEnum

// Create Roxflags in the Flags container class
class Flags : RoxContainer {
    // Define the feature flags
    val showMessage = RoxFlag(false)
    val message = RoxString("This is default message; try changing some flag values!")
    val fontColor = RoxString("White", arrayOf("White", "Blue", "Green", "Yellow"))
    val fontSize = RoxInt(14, intArrayOf(14, 18, 24))
    val specialNumber = RoxDouble(3.14, doubleArrayOf(2.71, 0.577))
    val titleColorsEnum = RoxEnum<Color>(Color.WHITE)

    enum class Color {
        WHITE, BLUE, GREEN, YELLOW
    }
}
