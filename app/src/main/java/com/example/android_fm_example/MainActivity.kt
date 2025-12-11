package com.example.android_fm_example

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.rollout.android.Rox
import io.rollout.android.RoxInstance
import io.rollout.android.client.RoxOptions
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults
import io.rollout.configuration.RoxContainer
import io.rollout.flags.RoxFlag
import io.rollout.flags.RoxInt
import io.rollout.flags.RoxString

/**
 * MainActivity demonstrates multi-instance SDK configuration using CloudBees Feature Management.
 * This activity manages two separate SDK instances with different configurations and custom properties.
 */
class MainActivity : ComponentActivity() {
//
    companion object {
        private const val TAG = "RoxTest"
        private const val FIRST_SDK_KEY = "<FIRST-SDK-KEY>"
        private const val SECOND_SDK_KEY = "<SECOND-SDK-KEY>"
    }

    // Flag containers for each SDK instance
    private val firstFlags = FirstFlags()
    private val secondFlags = SecondFlags()

    // SDK instances
    private lateinit var firstConfiguration: RoxInstance
    private lateinit var secondConfiguration: RoxInstance

    // UI components
    private lateinit var instancesRecyclerView: RecyclerView
    private lateinit var instanceAdapter: InstanceAdapter

    // Network monitoring
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    /**
     * Flag container for the first SDK instance.
     * These flags auto-register on the dashboard if they don't exist.
     */
    class FirstFlags : RoxContainer {
        val message = RoxString("Hello from first instance!")
        val showMessage = RoxFlag(true)
        val titleColor = RoxString("Blue")
        val titleSize = RoxString("16")
        val maxRetries = RoxInt(3)
    }

    /**
     * Flag container for the second SDK instance.
     * These flags auto-register on the dashboard if they don't exist.
     */
    class SecondFlags : RoxContainer {
        val secondMessage = RoxString("Hello from second instance!")
        val showSecondMessage = RoxFlag(true)
        val secondTitleColor = RoxString("Green")
        val secondTitleSize = RoxString("18")
        val secondMaxRetries = RoxInt(5)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        initializeUI()
        initializeSdkInstances()
        setupNetworkMonitoring()
        updateInstancesUI()
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Initializes the RecyclerView and adapter for displaying SDK instances.
     */
    private fun initializeUI() {
        instancesRecyclerView = findViewById(R.id.instancesRecyclerView)
        instanceAdapter = InstanceAdapter()
        instancesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = instanceAdapter
        }

        // Display loading state initially
        instanceAdapter.updateInstances(
            listOf(
                InstanceItem(
                    maskSdkKey(FIRST_SDK_KEY),
                    "Loading first instance...",
                    dynamicApiValues = "Initializing Dynamic API..."
                ),
                InstanceItem(
                    maskSdkKey(SECOND_SDK_KEY),
                    "Loading second instance...",
                    dynamicApiValues = "Initializing Dynamic API..."
                )
            )
        )
    }

    /**
     * Initializes both SDK instances with their respective configurations.
     */
    private fun initializeSdkInstances() {
        try {
            initializeFirstSdkInstance()
            initializeSecondSdkInstance()
        } catch (e: Exception) {
            handleSdkInitializationError(e)
        }
    }

    /**
     * Initializes the first SDK instance with custom properties and configuration.
     */
    private fun initializeFirstSdkInstance() {
        Log.d(TAG, "Starting initialization of first configuration")

        firstConfiguration = Rox.instance(FIRST_SDK_KEY)

        val options = createRoxOptions("First instance")
        firstConfiguration.setup(application, options)
        firstConfiguration.register("android", firstFlags)

        // Set custom properties for first SDK instance
        firstConfiguration.apply {
            setCustomStringProperty("user_version", "premium")
            setCustomStringProperty("app_version", "1.2.0")
            setCustomBooleanProperty("is_beta_user", true)
            setCustomIntProperty("user_level", 42)

            // Add context properties so they appear in dashboard for targeting
            setCustomStringProperty("user_id", "user_12345")
            setCustomStringProperty("subscription_tier", "premium")
            setCustomStringProperty("device_type", "tablet")
        }

        Log.d(TAG, "First configuration custom properties set")
        firstConfiguration.fetch()
    }

    /**
     * Initializes the second SDK instance with custom properties and configuration.
     */
    private fun initializeSecondSdkInstance() {
        Log.d(TAG, "Starting initialization of second configuration")

        secondConfiguration = Rox.instance(SECOND_SDK_KEY)
        secondConfiguration.register(secondFlags)

        // Set custom properties for second SDK instance (different values to test isolation)
        secondConfiguration.apply {
            setCustomStringProperty("user_version", "basic")
            setCustomStringProperty("app_version", "1.1.5")
            setCustomBooleanProperty("is_beta_user", false)
            setCustomIntProperty("user_level", 15)

            // Add context properties with different values to test isolation
            setCustomStringProperty("user_id", "user_67890")
            setCustomStringProperty("subscription_tier", "basic")
            setCustomStringProperty("device_type", "phone")
        }

        Log.d(TAG, "Second configuration custom properties set")

        val options = createRoxOptions("Second instance")
        secondConfiguration.setup(application, options)
    }

    /**
     * Creates RoxOptions with common configuration settings.
     *
     * @param instanceName Name used for logging purposes
     * @return Configured RoxOptions instance
     */
    private fun createRoxOptions(instanceName: String): RoxOptions {
        return RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withVerboseLevel(RoxOptions.VerboseLevel.VERBOSE_LEVEL_DEBUG)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        Log.d(TAG, "$instanceName configuration fetched - updating UI")
                        updateInstancesUI()  // Auto-update UI when flags are fetched
                    }
                }
            })
            .build()
    }

    /**
     * Handles errors that occur during SDK initialization.
     *
     * @param exception The exception that occurred
     */
    private fun handleSdkInitializationError(exception: Exception) {
        Log.e(TAG, "Error initializing SDK instances", exception)
        runOnUiThread {
            val errorInstances = listOf(
                InstanceItem(
                    sdkKey = maskSdkKey(FIRST_SDK_KEY),
                    value = "Error initializing first instance: ${exception.message}",
                    isVisible = true,
                    dynamicApiValues = "Error: Instance not initialized"
                ),
                InstanceItem(
                    sdkKey = maskSdkKey(SECOND_SDK_KEY),
                    value = "Error initializing second instance: ${exception.message}",
                    isVisible = true,
                    dynamicApiValues = "Error: Instance not initialized"
                )
            )
            instanceAdapter.updateInstances(errorInstances)
        }
    }

    /**
     * Sets up network connectivity monitoring to refresh flags when connection is restored.
     */
    private fun setupNetworkMonitoring() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread { updateInstancesUI() }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Network connection lost")
            }
        }
    }

    /**
     * Updates the UI with current flag values from both SDK instances.
     */
    private fun updateInstancesUI() {
        val updatedInstances = listOf(
            createInstanceItem(
                sdkKey = FIRST_SDK_KEY,
                configuration = firstConfiguration,
                message = firstFlags.message.value,
                color = firstFlags.titleColor.value,
                size = firstFlags.titleSize.value,
                retries = firstFlags.maxRetries.value,
                isVisible = firstFlags.showMessage.isEnabled
            ),
            createInstanceItem(
                sdkKey = SECOND_SDK_KEY,
                configuration = secondConfiguration,
                message = secondFlags.secondMessage.value,
                color = secondFlags.secondTitleColor.value,
                size = secondFlags.secondTitleSize.value,
                retries = secondFlags.secondMaxRetries.value,
                isVisible = secondFlags.showSecondMessage.isEnabled
            )
        )
        runOnUiThread {
            instanceAdapter.updateInstances(updatedInstances)
        }
    }

    /**
     * Creates an InstanceItem with error handling.
     *
     * @param sdkKey The SDK key to display
     * @param configuration The RoxInstance configuration
     * @param message The message flag value
     * @param color The color flag value
     * @param size The size flag value
     * @param retries The retries flag value
     * @param isVisible Whether the instance should be visible
     * @return InstanceItem with flag values or error message
     */
    private fun createInstanceItem(
        sdkKey: String,
        configuration: RoxInstance,
        message: String,
        color: String,
        size: String,
        retries: Int,
        isVisible: Boolean
    ): InstanceItem {
        return try {
            val dynamicApiValues = getDynamicApiValues(configuration, sdkKey)
            InstanceItem(
                sdkKey = maskSdkKey(sdkKey),
                value = "$message | Color: $color (Size: $size) | Retries: $retries",
                isVisible = isVisible,
                dynamicApiValues = dynamicApiValues
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating instance item for $sdkKey", e)
            InstanceItem(
                sdkKey = maskSdkKey(sdkKey),
                value = "Error: ${e.message}",
                isVisible = false,
                dynamicApiValues = "Error loading dynamic API"
            )
        }
    }

    /**
     * Gets Dynamic API flag values for display in UI.
     * Each instance uses different flag names to demonstrate proper isolation.
     *
     * @param configuration The RoxInstance configuration
     * @param sdkKey The SDK key to determine which flags to check
     * @return Formatted string with Dynamic API flag values
     */
    private fun getDynamicApiValues(configuration: RoxInstance, sdkKey: String): String {
        return try {
            val dynamicAPI = configuration.getDynamicAPI()

            // Use instance-specific flag names to demonstrate proper isolation
            val (featureFlagName, welcomeMsgName, defaultMsg) = when (sdkKey) {
                FIRST_SDK_KEY -> Triple(
                    "dynamic_feature_flag_first",
                    "dynamic_welcome_message_first",
                    "First Instance Welcome"
                )
                SECOND_SDK_KEY -> Triple(
                    "dynamic_feature_flag_second",
                    "dynamic_welcome_message_second",
                    "Second Instance Welcome"
                )
                else -> Triple(
                    "dynamic_feature_flag",
                    "dynamic_welcome_message",
                    "Default Welcome"
                )
            }

            // Get Dynamic API values with instance-specific flag names
            val featureEnabled = dynamicAPI.isEnabled(featureFlagName, false)
            val welcomeMsg = dynamicAPI.getValue(welcomeMsgName, defaultMsg)

            buildString {
                append("✓ Feature Flag ($featureFlagName): $featureEnabled\n")
                append("✓ Welcome Message: \"$welcomeMsg\"")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting dynamic API values", e)
            "Error: ${e.message}"
        }
    }

    /**
     * Masks SDK key for display, showing only the first 4 characters.
     *
     * @param key The SDK key to mask
     * @return Masked SDK key
     */
    private fun maskSdkKey(key: String): String {
        return if (key.length > 4) "${key.take(4)}****" else key
    }
}
