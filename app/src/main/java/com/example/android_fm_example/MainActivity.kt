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
import io.rollout.android.RoxInstance
import io.rollout.android.RoxManager
import io.rollout.android.client.RoxOptions
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults

class MainActivity : ComponentActivity() {
    companion object {
        private val firstSdkKey = "290fb9d0-4c36-4795-67cf-92dd0054e62f"
        private val firstInstanceId = "first"
        private val secondSdkKey = "aad64536-18b4-4056-64ab-04fc76246895"
        private val secondInstanceId = "second"
    }

    private lateinit var flags: Flags
    private lateinit var secondFlags: SecondFlags
    private lateinit var firstInstance: RoxInstance
    private lateinit var secondInstance: RoxInstance

    init {
        flags = Flags()
        secondFlags = SecondFlags()
    }
    private lateinit var box1FlagValue: TextView
    private lateinit var box2FlagValue: TextView
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    
    // Note: Instance IDs are defined at the top of the class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        box1FlagValue = findViewById(R.id.box1FlagValue)
        box2FlagValue = findViewById(R.id.box2FlagValue)
        
        box1FlagValue.text = "Loading first instance..."
        box2FlagValue.text = "Loading second instance..."
        
        // Initialize both Flag containers
        flags = Flags()
        secondFlags = SecondFlags()

        // Create RoxOptions for first instance
        val firstOptions = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        runOnUiThread { updateFirstInstanceUI() }
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
                        runOnUiThread { updateSecondInstanceUI() }
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
                box1FlagValue.text = "Error initializing first instance: ${e.message}"
                box2FlagValue.text = "Error initializing second instance: ${e.message}"
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

    private fun updateFirstInstanceUI() {
        try {
            val fontColor = flags.fontColor.value
            val fontSize = flags.fontSize.value
            val message = flags.message.value
            val showMessage = flags.showMessage.isEnabled

            runOnUiThread {
                box1FlagValue.setTextColor(Color.parseColor(fontColor))
                box1FlagValue.textSize = fontSize.toFloat()
                box1FlagValue.text = message
                box1FlagValue.visibility = if (showMessage) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            box1FlagValue.visibility = View.GONE
        }
    }

    private fun updateSecondInstanceUI() {
        try {
            val secondFontColor = secondFlags.secondFontColor.value
            val secondFontSize = secondFlags.secondFontSize.value
            val secondMessage = secondFlags.secondMessage.value
            val showSecondMessage = secondFlags.showSecondMessage.isEnabled

            runOnUiThread {
                box2FlagValue.setTextColor(Color.parseColor(secondFontColor))
                box2FlagValue.textSize = secondFontSize.toFloat()
                box2FlagValue.text = secondMessage
                box2FlagValue.visibility = if (showSecondMessage) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            box2FlagValue.visibility = View.GONE
        }
    }

    private fun checkFlagsValue() {
        updateFirstInstanceUI()
        updateSecondInstanceUI()
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
