package com.example.android_fm_example

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.rollout.android.Rox
import io.rollout.android.RoxConfiguration
import io.rollout.android.client.RoxOptions
import io.rollout.configuration.RoxContainer
import io.rollout.flags.RoxFlag
import io.rollout.flags.RoxString
import io.rollout.flags.RoxInt
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults

class MainActivity : ComponentActivity() {
    companion object {
        private val firstSdkKey = "e07c0a49-4b1d-4c92-bce6-254ecf4ee1a6"
        private val secondSdkKey = "0886c21c-123f-4976-b58c-284ee4e4e1fb"
    }

    private val flags = Flags()
    private val secondFlags = SecondFlags()
    private lateinit var firstConfiguration: RoxConfiguration
    private lateinit var secondConfiguration: RoxConfiguration

    class Flags : RoxContainer {
        // These flags should auto-register on the dashboard if they don't exist
        val message5 = RoxString("Hello from first instance!")
        val showMessage5 = RoxFlag(true)
        val titleColor5 = RoxString("Blue")
        val titleSize5 = RoxString("16")
        val maxRetries5 = RoxInt(3)
    }

    class SecondFlags : RoxContainer {
        // These flags should auto-register on the dashboard if they don't exist
        val secondMessage5 = RoxString("Hello from second instance!")
        val showSecondMessage5 = RoxFlag(true)
        val secondTitleColor5 = RoxString("Green")
        val secondTitleSize5 = RoxString("18")
        val secondMaxRetries5 = RoxInt(5)
    }
    private lateinit var instancesRecyclerView: RecyclerView
    private lateinit var instanceAdapter: InstanceAdapter
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        instancesRecyclerView = findViewById(R.id.instancesRecyclerView)
        instanceAdapter = InstanceAdapter()
        instancesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = instanceAdapter
        }

        // Initialize with loading state
        instanceAdapter.updateInstances(listOf(
            InstanceItem(maskSdkKey(firstSdkKey), "Loading first instance..."),
            InstanceItem(maskSdkKey(secondSdkKey), "Loading second instance...")
        ))

        // Create RoxOptions for first instance
        val firstOptions = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withVerboseLevel(RoxOptions.VerboseLevel.VERBOSE_LEVEL_DEBUG)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        android.util.Log.d("RoxTest", "First instance configuration fetched")
                        checkFlagsValue()
                    }
                }
            })
            .build()

        // Create RoxOptions for second instance
        val secondOptions = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withVerboseLevel(RoxOptions.VerboseLevel.VERBOSE_LEVEL_DEBUG)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        android.util.Log.d("RoxTest", "Second instance configuration fetched")
                        checkFlagsValue()
                    }
                }
            })
            .build()

        try {
            android.util.Log.d("RoxTest", "Starting initialization of first configuration")
            // Create first configuration using new object-based API
            firstConfiguration = Rox.add(firstSdkKey)
            firstConfiguration.setup(application, firstOptions)
            android.util.Log.d("RoxTest", "First configuration setup complete")
            
            // Register first flags with "android" namespace
            firstConfiguration.register("android", flags)
            android.util.Log.d("RoxTest", "First configuration registered flags")
            
            // Set custom properties for first SDK instance
            firstConfiguration.setCustomStringProperty("user_tier", "premium")
            firstConfiguration.setCustomStringProperty("app_version", "1.2.0")
            firstConfiguration.setCustomBooleanProperty("is_beta_user", true)
            firstConfiguration.setCustomIntProperty("user_level", 42)
            android.util.Log.d("RoxTest", "First configuration custom properties set")
            
            firstConfiguration.fetch()

            android.util.Log.d("RoxTest", "Starting initialization of second configuration")
            // Create second configuration using new object-based API
            secondConfiguration = Rox.add(secondSdkKey)
            secondConfiguration.setup(application, secondOptions)
            android.util.Log.d("RoxTest", "Second configuration setup complete")
            
            // Register second flags with empty namespace
            secondConfiguration.register("", secondFlags)
            
            // Set custom properties for second SDK instance (different values to test isolation)
            secondConfiguration.setCustomStringProperty("user_tier", "basic")
            secondConfiguration.setCustomStringProperty("app_version", "1.1.5")
            secondConfiguration.setCustomBooleanProperty("is_beta_user", false)
            secondConfiguration.setCustomIntProperty("user_level", 15)
            android.util.Log.d("RoxTest", "Second configuration custom properties set")
            
            android.util.Log.d("RoxTest", "Second configuration registered flags")
            secondConfiguration.fetch()
        } catch (e: Exception) {
            runOnUiThread {
                val errorInstances = listOf(
                    InstanceItem(
                        sdkKey = maskSdkKey(firstSdkKey),
                        value = "Error initializing first instance: ${e.message}",
                        isVisible = true
                    ),
                    InstanceItem(
                        sdkKey = maskSdkKey(secondSdkKey),
                        value = "Error initializing second instance: ${e.message}",
                        isVisible = true
                    )
                )
                instanceAdapter.updateInstances(errorInstances)
            }
        }

        // Initial UI update
        checkFlagsValue()

        // Setup network callback
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread { checkFlagsValue() }
            }
            override fun onLost(network: Network) {
                super.onLost(network)
            }
        }

    }



    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun updateInstancesUI() {
        val updatedInstances = listOf(
            try {
                InstanceItem(
                    sdkKey = maskSdkKey(firstSdkKey),
                    value = "${flags.message5.value} | Color: ${flags.titleColor5.value} (Size: ${flags.titleSize5.value}) | Retries: ${flags.maxRetries5.value}",
                    isVisible = flags.showMessage5.isEnabled
                )
            } catch (e: Exception) {
                InstanceItem(maskSdkKey(firstSdkKey), "Error: ${e.message}", false)
            },
            try {
                InstanceItem(
                    sdkKey = maskSdkKey(secondSdkKey),
                    value = "${secondFlags.secondMessage5.value} | Color: ${secondFlags.secondTitleColor5.value} (Size: ${secondFlags.secondTitleSize5.value}) | Retries: ${secondFlags.secondMaxRetries5.value}",
                    isVisible = secondFlags.showSecondMessage5.isEnabled
                )
            } catch (e: Exception) {
                InstanceItem(maskSdkKey(secondSdkKey), "Error: ${e.message}", false)
            }
        )
        runOnUiThread {
            instanceAdapter.updateInstances(updatedInstances)
        }
    }

    private fun maskSdkKey(key: String): String {
        return if (key.length > 4) key.take(4) + "****" else key
    }

    private fun checkFlagsValue() {
        updateInstancesUI()
    }

    private fun getColorFromFlag(color: String): Int {
        return when (color.lowercase()) {
            "red" -> Color.RED
            "blue" -> Color.BLUE
            "green" -> Color.GREEN
            "yellow" -> Color.YELLOW
            else -> Color.BLACK
        }
    }
}
