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
import io.rollout.android.RoxInstance
import io.rollout.android.RoxManager
import io.rollout.android.client.RoxOptions
import io.rollout.configuration.RoxContainer
import io.rollout.flags.RoxFlag
import io.rollout.flags.RoxString
import io.rollout.flags.RoxInt
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults

class MainActivity : ComponentActivity() {
    companion object {
        private val firstSdkKey = "d3c0f3d8-30a2-4524-98bd-915823021a8e"
        private val firstInstanceId = "first"
        private val secondSdkKey = "b6f19d8b-c8a2-43f8-8564-467ea15eb1f9"
        private val secondInstanceId = "second"
    }

    private val flags = Flags()
    private val secondFlags = SecondFlags()
    private lateinit var firstInstance: RoxInstance
    private lateinit var secondInstance: RoxInstance

    class Flags : RoxContainer {
        val fontColor = RoxString("blue")
        val fontSize = RoxInt(16)
        val message = RoxString("Hello from first instance!")
        val showMessage1 = RoxFlag(true)
    }

    class SecondFlags : RoxContainer {
        val secondFontColor = RoxString("green")
        val secondFontSize = RoxInt(20)
        val secondMessage = RoxString("Hello from second instance!")
        val showSecondMessage1 = RoxFlag(true)
    }
    private lateinit var instancesRecyclerView: RecyclerView
    private lateinit var instanceAdapter: InstanceAdapter
    private lateinit var connectivityManager: ConnectivityManager

    private val instances = mutableListOf(
        Instance(firstInstanceId, firstSdkKey, flags),
        Instance(secondInstanceId, secondSdkKey, secondFlags)
    )

    data class Instance(
        val id: String,
        val sdkKey: String,
        val flags: RoxContainer,
        var roxInstance: RoxInstance? = null
    )
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    // Note: Instance IDs are defined at the top of the class

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
        instanceAdapter.updateInstances(instances.map {
            InstanceItem(maskSdkKey(it.sdkKey), "Loading ${it.id} instance...")
        })

        // Create RoxOptions for first instance
        val firstOptions = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        runOnUiThread { updateInstancesUI() }
                    }
                }
            })
            .build()

        // Create RoxOptions for second instance
        val secondOptions = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        runOnUiThread { updateInstancesUI() }
                    }
                }
            })
            .build()

        try {
            // Initialize first instance
            firstInstance = RoxManager.setup(firstInstanceId, application, firstSdkKey, firstOptions)!!
            firstInstance.register(flags)
            firstInstance.fetch()

            // Initialize second instance
            secondInstance = RoxManager.setup(secondInstanceId, application, secondSdkKey, secondOptions)!!
            secondInstance.register(secondFlags)
            secondInstance.fetch()
        } catch (e: Exception) {
            runOnUiThread {
                val errorInstances = instances.map { instance ->
                    InstanceItem(
                        sdkKey = maskSdkKey(instance.sdkKey),
                        value = "Error initializing instance: ${e.message}",
                        isVisible = true
                    )
                }
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
        val updatedInstances = instances.map { instance ->
            when (instance.flags) {
                is Flags -> {
                    try {
                        val flags = instance.flags as Flags
                        InstanceItem(
                            sdkKey = maskSdkKey(instance.sdkKey),
                            value = flags.message.value,
                            isVisible = flags.showMessage1.isEnabled,
                            fontColor = flags.fontColor.value,
                            fontSize = flags.fontSize.value
                        )
                    } catch (e: Exception) {
                        InstanceItem(maskSdkKey(instance.sdkKey), "Error: ${e.message}", false)
                    }
                }
                is SecondFlags -> {
                    try {
                        val flags = instance.flags as SecondFlags
                        InstanceItem(
                            sdkKey = maskSdkKey(instance.sdkKey),
                            value = flags.secondMessage.value,
                            isVisible = flags.showSecondMessage1.isEnabled,
                            fontColor = flags.secondFontColor.value,
                            fontSize = flags.secondFontSize.value
                        )
                    } catch (e: Exception) {
                        InstanceItem(maskSdkKey(instance.sdkKey), "Error: ${e.message}", false)
                    }
                }
                else -> InstanceItem(maskSdkKey(instance.sdkKey), "Unknown flag type", false)
            }
        }
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
