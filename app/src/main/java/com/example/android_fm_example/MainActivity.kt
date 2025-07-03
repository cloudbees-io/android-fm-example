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
        private val firstSdkKey = "5fff5164-414f-4bdd-b8d0-a42225e42c8b"
        private val firstInstanceId = "default"
        private val secondSdkKey = "f8fa95ff-5a6b-4677-94c1-c00c87afa023"
        private val secondInstanceId = "second1"
    }

    private val flags = Flags()
    private val secondFlags = SecondFlags()
    private lateinit var firstInstance: RoxInstance
    private lateinit var secondInstance: RoxInstance

    class Flags : RoxContainer {
//        val fontColor = RoxString("blue")
//        val fontSize = RoxInt(16)
        val message1 = RoxString("Hello from first instance!")
        val showMessage1 = RoxFlag(true)
    }

    class SecondFlags : RoxContainer {
//        val secondFontColor = RoxString("green")
//        val secondFontSize = RoxInt(20)
        val secondMessage2 = RoxString("Hello from second instance!")
        val showSecondMessage3 = RoxFlag(true)
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
            android.util.Log.d("RoxTest", "Starting initialization of first instance")
            android.util.Log.d("RoxTest", "Starting initialization of first instance")
            firstInstance = RoxManager.setup(firstInstanceId, application, firstSdkKey, firstOptions)!!
            android.util.Log.d("RoxTest", "First instance setup complete")
            
            // Register first flags with empty namespace since this is default instance
            firstInstance.register("", flags)
            android.util.Log.d("RoxTest", "First instance registered flags with empty namespace")
            firstInstance.fetch()

            android.util.Log.d("RoxTest", "Starting initialization of second instance")
            secondInstance = RoxManager.setup(secondInstanceId, application, secondSdkKey, secondOptions)!!
            android.util.Log.d("RoxTest", "Second instance setup complete")
            
            // Register second flags with instance ID as namespace
            secondInstance.register("",secondFlags)
            
            android.util.Log.d("RoxTest", "Second instance registered flags with namespace: $secondInstanceId")
            secondInstance.fetch()
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
                    value = flags.message1.value,
                    isVisible = flags.showMessage1.isEnabled,
//                    fontColor = flags.fontColor.value,
//                    fontSize = flags.fontSize.value
                )
            } catch (e: Exception) {
                InstanceItem(maskSdkKey(firstSdkKey), "Error: ${e.message}", false)
            },
            try {
                InstanceItem(
                    sdkKey = maskSdkKey(secondSdkKey),
                    value = secondFlags.secondMessage2.value,
                    isVisible = secondFlags.showSecondMessage3.isEnabled,
//                    fontColor = secondFlags.secondFontColor.value,
//                    fontSize = secondFlags.secondFontSize.value
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
