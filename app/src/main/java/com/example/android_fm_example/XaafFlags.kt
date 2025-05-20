package com.example.android_fm_example

import io.rollout.sdk.xaaf.configuration.RoxContainer
import io.rollout.sdk.xaaf.flags.RoxFlagXaaf
import io.rollout.sdk.xaaf.flags.RoxString
import io.rollout.sdk.xaaf.flags.RoxInt
import io.rollout.sdk.xaaf.flags.RoxDouble
import io.rollout.sdk.xaaf.flags.RoxEnum

// Create Roxflags in the XaafFlags container class
class XaafFlags : RoxContainer {
    // Define the feature flags
    val showMessage = RoxFlagXaaf(true)
    val message = RoxString("This is default message from Xaaf; try changing some flag values!")
    val fontColor = RoxString("Red", arrayOf("White", "Blue", "Green", "Yellow", "Red"))
    val fontSize = RoxInt(14, intArrayOf(14, 18, 24))
    val specialNumber = RoxDouble(3.14, doubleArrayOf(2.71, 0.577))
    val titleColorsEnum = RoxEnum<Color>(Color.WHITE)

    enum class Color {
        WHITE, BLUE, GREEN, YELLOW, RED
    }
}
