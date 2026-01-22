package com.example.android_fm_example

import android.util.Log
import io.rollout.android.RoxInstance
import io.rollout.client.DynamicAPI
import io.rollout.context.Context

/**
 * Target Groups Validation Tests for Android SDK
 *
 * This class validates different approaches to using target groups with Dynamic API:
 *
 * 1. setCustomProperty + getValue (instance-level properties)
 * 2. getValue with Context parameter (call-level context)
 * 3. setGlobalContext + getValue (global context - if available)
 *
 * ## How to Test:
 *
 * 1. Create a flag in CloudBees dashboard with target groups:
 *    - Flag name: "target_group_test_flag"
 *    - Default value: "default"
 *    - Create a target group with condition: platformVersion == "26.0.1"
 *    - Set value for that target group: "android_26_value"
 *
 * 2. Call the validation methods in MainActivity after setup:
 *    ```kotlin
 *    Handler(Looper.getMainLooper()).postDelayed({
 *        TargetGroupsValidation.validateAllApproaches(firstConfiguration)
 *    }, 3000)
 *    ```
 *
 * 3. Check Logcat (filter: RoxTargetGroups) to see which approaches work
 */
object TargetGroupsValidation {

    private const val TAG = "RoxTargetGroups"
    private const val TEST_FLAG_NAME = "target_group_test_flag"
    private const val DEFAULT_VALUE = "default"
    private const val EXPECTED_VALUE = "android_26_value"
    private const val PLATFORM_VERSION = "26.0.1"

    /**
     * Test Scenario 1: Using setCustomProperty + getValue
     * Expected behavior: Should evaluate target groups using custom properties
     *
     * RECOMMENDED APPROACH: This is the standard way to set instance-level properties
     * that persist across all flag evaluations.
     */
    fun testCustomProperty(instance: RoxInstance?): String {
        Log.d(TAG, "\n========================================")
        Log.d(TAG, "TEST 1: setCustomProperty + getValue")
        Log.d(TAG, "========================================")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return "error"
        }

        // Set custom property on the instance
        instance.setCustomStringProperty("platformVersion", PLATFORM_VERSION)
        Log.d(TAG, "✅ Set custom property platformVersion: $PLATFORM_VERSION")

        // Get flag value using Dynamic API WITHOUT context
        val dynamicAPI = instance.getDynamicAPI()
        val flagValue = dynamicAPI.getValue(TEST_FLAG_NAME, DEFAULT_VALUE)

        Log.d(TAG, "📊 Flag value: $flagValue")
        Log.d(TAG, "Expected: '$EXPECTED_VALUE' if target group works")
        Log.d(TAG, "Actual: '$flagValue'")

        if (flagValue == EXPECTED_VALUE) {
            Log.d(TAG, "✅ WORKING: Custom property properly evaluated")
        } else {
            Log.e(TAG, "❌ NOT WORKING: Expected '$EXPECTED_VALUE', got '$flagValue'")
        }

        return flagValue
    }

    /**
     * Test Scenario 2: Using getValue with Context parameter
     * Expected behavior: Should evaluate target groups using the passed context
     *
     * USE CASE: When you need per-call context that may differ between evaluations
     */
    fun testContextParameter(instance: RoxInstance?): String {
        Log.d(TAG, "\n========================================")
        Log.d(TAG, "TEST 2: getValue with Context parameter")
        Log.d(TAG, "========================================")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return "error"
        }

        // Create context with platformVersion
        val context = Context.Builder().from(mapOf(
            "platformVersion" to PLATFORM_VERSION
        ))

        Log.d(TAG, "✅ Created context with platformVersion: $PLATFORM_VERSION")

        // Get flag value using Dynamic API WITH context parameter
        val dynamicAPI = instance.getDynamicAPI()
        val flagValue = dynamicAPI.getValue(TEST_FLAG_NAME, DEFAULT_VALUE, context)

        Log.d(TAG, "📊 Flag value: $flagValue")
        Log.d(TAG, "Expected: '$EXPECTED_VALUE' if target group works")
        Log.d(TAG, "Actual: '$flagValue'")

        if (flagValue == EXPECTED_VALUE) {
            Log.d(TAG, "✅ WORKING: Context parameter properly evaluated")
        } else {
            Log.e(TAG, "❌ NOT WORKING: Expected '$EXPECTED_VALUE', got '$flagValue'")
        }

        return flagValue
    }

    /**
     * Test Scenario 3: Using getValue with empty variations array and Context
     * Expected behavior: Should evaluate target groups even with empty variations
     *
     * IMPORTANT: This tests whether empty variations array affects target group evaluation
     */
    fun testContextWithEmptyVariations(instance: RoxInstance?): String {
        Log.d(TAG, "\n========================================")
        Log.d(TAG, "TEST 3: getValue with empty variations + Context")
        Log.d(TAG, "========================================")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return "error"
        }

        // Create context
        val context = Context.Builder().from(mapOf(
            "platformVersion" to PLATFORM_VERSION
        ))

        Log.d(TAG, "✅ Created context with platformVersion: $PLATFORM_VERSION")

        // Get flag value with EMPTY variations array + context
        val dynamicAPI = instance.getDynamicAPI()
        val flagValue = dynamicAPI.getValue(
            TEST_FLAG_NAME,
            DEFAULT_VALUE,
            emptyArray(), // Empty variations array
            context
        )

        Log.d(TAG, "📊 Flag value: $flagValue")
        Log.d(TAG, "Expected: '$EXPECTED_VALUE' if target group works")
        Log.d(TAG, "Actual: '$flagValue'")

        if (flagValue == EXPECTED_VALUE) {
            Log.d(TAG, "✅ WORKING: Empty variations don't affect target group evaluation")
        } else {
            Log.e(TAG, "❌ NOT WORKING: Expected '$EXPECTED_VALUE', got '$flagValue'")
        }

        return flagValue
    }

    /**
     * Test Scenario 4: Using getValue with populated variations array and Context
     * Expected behavior: Should evaluate target groups with provided variations
     */
    fun testContextWithVariations(instance: RoxInstance?): String {
        Log.d(TAG, "\n========================================")
        Log.d(TAG, "TEST 4: getValue with variations + Context")
        Log.d(TAG, "========================================")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return "error"
        }

        // Create context
        val context = Context.Builder().from(mapOf(
            "platformVersion" to PLATFORM_VERSION
        ))

        Log.d(TAG, "✅ Created context with platformVersion: $PLATFORM_VERSION")

        // Get flag value with variations array + context
        val dynamicAPI = instance.getDynamicAPI()
        val variations = arrayOf(DEFAULT_VALUE, EXPECTED_VALUE, "other_value")
        val flagValue = dynamicAPI.getValue(
            TEST_FLAG_NAME,
            DEFAULT_VALUE,
            variations,
            context
        )

        Log.d(TAG, "📊 Flag value: $flagValue")
        Log.d(TAG, "Variations provided: ${variations.joinToString(", ")}")
        Log.d(TAG, "Expected: '$EXPECTED_VALUE' if target group works")
        Log.d(TAG, "Actual: '$flagValue'")

        if (flagValue == EXPECTED_VALUE) {
            Log.d(TAG, "✅ WORKING: Variations don't interfere with target group evaluation")
        } else {
            Log.e(TAG, "❌ NOT WORKING: Expected '$EXPECTED_VALUE', got '$flagValue'")
        }

        return flagValue
    }

    /**
     * Run all validation tests and provide a summary
     */
    fun validateAllApproaches(instance: RoxInstance?) {
        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║        ANDROID TARGET GROUPS VALIDATION TEST SUITE             ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")

        // Run all tests
        val result1 = testCustomProperty(instance)
        val result2 = testContextParameter(instance)
        val result3 = testContextWithEmptyVariations(instance)
        val result4 = testContextWithVariations(instance)

        // Summary
        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║                      TEST SUMMARY                               ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")
        Log.d(TAG, "Test 1 - setCustomProperty + getValue:        ${if (result1 == EXPECTED_VALUE) "✅ PASS" else "❌ FAIL"}")
        Log.d(TAG, "Test 2 - getValue with context param:         ${if (result2 == EXPECTED_VALUE) "✅ PASS" else "❌ FAIL"}")
        Log.d(TAG, "Test 3 - getValue empty variations + context: ${if (result3 == EXPECTED_VALUE) "✅ PASS" else "❌ FAIL"}")
        Log.d(TAG, "Test 4 - getValue with variations + context:  ${if (result4 == EXPECTED_VALUE) "✅ PASS" else "❌ FAIL"}")

        // Analysis
        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║                       ANALYSIS                                  ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")

        val allPassed = result1 == EXPECTED_VALUE && result2 == EXPECTED_VALUE &&
                result3 == EXPECTED_VALUE && result4 == EXPECTED_VALUE

        if (allPassed) {
            Log.d(TAG, "✅ ALL METHODS WORKING - Target groups are properly evaluated")
            Log.d(TAG, "   All approaches work correctly.")
            Log.d(TAG, "   Empty variations array does NOT affect target group evaluation.")
        } else {
            val workingTests = mutableListOf<String>()
            val failingTests = mutableListOf<String>()

            if (result1 == EXPECTED_VALUE) workingTests.add("setCustomProperty") else failingTests.add("setCustomProperty")
            if (result2 == EXPECTED_VALUE) workingTests.add("Context parameter") else failingTests.add("Context parameter")
            if (result3 == EXPECTED_VALUE) workingTests.add("Empty variations") else failingTests.add("Empty variations")
            if (result4 == EXPECTED_VALUE) workingTests.add("With variations") else failingTests.add("With variations")

            if (workingTests.isNotEmpty()) {
                Log.w(TAG, "⚠️  PARTIAL WORKING - Some approaches work:")
                workingTests.forEach { Log.d(TAG, "   ✅ $it") }
            }

            if (failingTests.isNotEmpty()) {
                Log.e(TAG, "❌ FAILING TESTS:")
                failingTests.forEach { Log.e(TAG, "   ❌ $it") }
            }
        }

        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║                    DASHBOARD SETUP REMINDER                     ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")
        Log.d(TAG, "Ensure you have created in CloudBees Dashboard:")
        Log.d(TAG, "1. Flag: '$TEST_FLAG_NAME'")
        Log.d(TAG, "2. Target Group condition: platformVersion == \"$PLATFORM_VERSION\"")
        Log.d(TAG, "3. Value for target group: '$EXPECTED_VALUE'")
        Log.d(TAG, "4. Default value: '$DEFAULT_VALUE'")
        Log.d(TAG, "\n")
    }

    /**
     * Test with different property types (String, Boolean, Number)
     */
    fun testDifferentPropertyTypes(instance: RoxInstance?) {
        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║           TESTING DIFFERENT PROPERTY TYPES                      ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return
        }

        val dynamicAPI = instance.getDynamicAPI()

        // Test 1: String property with setCustomProperty
        Log.d(TAG, "\n--- Test: String property (setCustomProperty) ---")
        instance.setCustomStringProperty("appVersion", "2.0.0")
        val result1 = dynamicAPI.getValue("version_test_flag", "v1")
        Log.d(TAG, "Result: $result1")

        // Test 2: String property with context
        Log.d(TAG, "\n--- Test: String property (context parameter) ---")
        val stringContext = Context.Builder().from(mapOf("appVersion" to "2.0.0"))
        val result2 = dynamicAPI.getValue("version_test_flag", "v1", stringContext)
        Log.d(TAG, "Result: $result2")

        // Test 3: Boolean property with setCustomProperty
        Log.d(TAG, "\n--- Test: Boolean property (setCustomProperty) ---")
        instance.setCustomBooleanProperty("isPremium", true)
        val result3 = dynamicAPI.isEnabled("premium_test_flag", false)
        Log.d(TAG, "Result: $result3")

        // Test 4: Boolean property with context
        Log.d(TAG, "\n--- Test: Boolean property (context parameter) ---")
        val boolContext = Context.Builder().from(mapOf("isPremium" to true))
        val result4 = dynamicAPI.isEnabled("premium_test_flag", false, boolContext)
        Log.d(TAG, "Result: $result4")

        // Test 5: Numeric property with setCustomProperty
        Log.d(TAG, "\n--- Test: Numeric property (setCustomProperty) ---")
        instance.setCustomIntProperty("userAge", 25)
        val result5 = dynamicAPI.getValue("age_test_flag", "young")
        Log.d(TAG, "Result: $result5")

        // Test 6: Numeric property with context
        Log.d(TAG, "\n--- Test: Numeric property (context parameter) ---")
        val numContext = Context.Builder().from(mapOf("userAge" to 25))
        val result6 = dynamicAPI.getValue("age_test_flag", "young", numContext)
        Log.d(TAG, "Result: $result6")

        // Test 7: Double property with context
        Log.d(TAG, "\n--- Test: Double property (context parameter) ---")
        val doubleContext = Context.Builder().from(mapOf("accountBalance" to 1000.50))
        val result7 = dynamicAPI.getDouble("balance_multiplier", 1.0, doubleContext)
        Log.d(TAG, "Result: $result7")

        Log.d(TAG, "\n✅ All property type tests completed")
    }

    /**
     * Test Context vs Custom Properties behavior
     * Helps understand when to use which approach
     */
    fun testContextVsCustomProperties(instance: RoxInstance?) {
        Log.d(TAG, "\n╔════════════════════════════════════════════════════════════════╗")
        Log.d(TAG, "║           CONTEXT VS CUSTOM PROPERTIES TEST                     ║")
        Log.d(TAG, "╚════════════════════════════════════════════════════════════════╝")

        if (instance == null) {
            Log.e(TAG, "❌ Instance not available")
            return
        }

        val dynamicAPI = instance.getDynamicAPI()

        // Set a custom property
        instance.setCustomStringProperty("userTier", "basic")
        Log.d(TAG, "Set custom property: userTier = basic")

        // Test 1: Get value WITHOUT context (should use custom property)
        val value1 = dynamicAPI.getValue("tier_test_flag", "default")
        Log.d(TAG, "Test 1 - Without context: $value1")

        // Test 2: Get value WITH context (context should override)
        val context = Context.Builder().from(mapOf("userTier" to "premium"))
        val value2 = dynamicAPI.getValue("tier_test_flag", "default", context)
        Log.d(TAG, "Test 2 - With context (userTier=premium): $value2")

        // Test 3: Get value WITHOUT context again (should still use custom property)
        val value3 = dynamicAPI.getValue("tier_test_flag", "default")
        Log.d(TAG, "Test 3 - Without context again: $value3")

        Log.d(TAG, "\n📝 Key Findings:")
        Log.d(TAG, "- Custom Properties: Persist across calls (instance-level)")
        Log.d(TAG, "- Context: Override per call (call-level)")
        Log.d(TAG, "- Context does NOT modify custom properties")
    }
}
