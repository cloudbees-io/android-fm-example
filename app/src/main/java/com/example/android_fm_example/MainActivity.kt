package com.example.android_fm_example

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.android_fm_example.ui.theme.AndroidfmexampleTheme
import io.rollout.android.Rox
import io.rollout.android.RoxManager
import io.rollout.android.RoxInstance
import io.rollout.android.client.RoxOptions
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "RoxTest"
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
    private lateinit var centerLabel: TextView
    private lateinit var secondCenterLabel: TextView
    private lateinit var headerLabel: TextView
    private lateinit var bottomLabel: TextView
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    
    // Note: Instance IDs are defined at the top of the class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        headerLabel = findViewById(R.id.headerText)
        bottomLabel = findViewById(R.id.bottomText)
        centerLabel = findViewById(R.id.centerLabel)
        secondCenterLabel = findViewById(R.id.secondCenterLabel)
        
        headerLabel.text = "CloudBees Feature Management - Multiple Instances Demo"
        bottomLabel.text = "Two separate SDK instances with different configurations"
        centerLabel.text = "Loading first instance..."
        secondCenterLabel.text = "Loading second instance..."
        centerLabel.visibility = View.VISIBLE
        secondCenterLabel.visibility = View.VISIBLE
        
        // Initialize both Flag containers
        flags = Flags()
        secondFlags = SecondFlags()

        // Create RoxOptions for first instance
        val firstOptions = RoxOptions.Builder()
            .withVerboseLevel(RoxOptions.VerboseLevel.VERBOSE_LEVEL_DEBUG)
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        Log.i(TAG, "First instance configuration fetched with status: ${it.fetcherStatus}")
                        runOnUiThread { updateFirstInstanceUI() }
                    }
                }
            })
            .build()

        // Create RoxOptions for second instance
        val secondOptions = RoxOptions.Builder()
            .withVerboseLevel(RoxOptions.VerboseLevel.VERBOSE_LEVEL_DEBUG)
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        Log.i(TAG, "Second instance configuration fetched with status: ${it.fetcherStatus}")
                        runOnUiThread { updateSecondInstanceUI() }
                    }
                }
            })
            .build()

        try {
            // Log device and environment info
            Log.i(TAG, "Device: ${android.os.Build.MODEL}, Android ${android.os.Build.VERSION.RELEASE}")
            Log.i(TAG, "App version: ${packageManager.getPackageInfo(packageName, 0).versionName}")
            
            // Log project keys
            Log.i(TAG, "First instance - ID: $firstInstanceId, Key: $firstSdkKey")
            Log.i(TAG, "Second instance - ID: $secondInstanceId, Key: $secondSdkKey")

            // Initialize first instance
            Log.i(TAG, "Setting up first instance")
            firstInstance = RoxManager.setup(firstInstanceId, application, firstSdkKey, firstOptions)
            Log.i(TAG, "First instance setup complete")
            Log.i(TAG, "Registering and fetching first instance")
            firstInstance.register(flags)
            firstInstance.fetch()
            Log.i(TAG, "First instance initialization complete")

            // Initialize second instance
            Log.i(TAG, "Setting up second instance")
            secondInstance = RoxManager.setup(secondInstanceId, application, secondSdkKey, secondOptions)
            Log.i(TAG, "Second instance setup complete")
            Log.i(TAG, "Registering and fetching second instance")
            secondInstance.register(secondFlags)
            secondInstance.fetch()
            Log.i(TAG, "Second instance initialization complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Rox instances", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            runOnUiThread {
                centerLabel.text = "Error initializing first instance: ${e.message}"
                secondCenterLabel.text = "Error initializing second instance: ${e.message}"
            }
        }

        // Initial UI update
        checkFlagsValue()

        // Setup network callback
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.i("MainActivity", "Network Available")
                
                // Update UI
                runOnUiThread { checkFlagsValue() }
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.i("MainActivity", "Network lost")
                // Handle network lost if needed
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
            val specialNumber = flags.specialNumber.value

            runOnUiThread {
                centerLabel.setTextColor(Color.parseColor(fontColor))
                centerLabel.textSize = fontSize.toFloat()
                centerLabel.text = if (showMessage) message else "First instance flag is disabled"
                centerLabel.visibility = View.VISIBLE
            }
            Log.i(TAG, "First Instance Values - Color: $fontColor, Size: $fontSize, Number: $specialNumber, Message: $message, Show: $showMessage")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating first instance UI", e)
        }
    }

    private fun updateSecondInstanceUI() {
        try {
            val secondFontColor = secondFlags.secondFontColor.value
            val secondFontSize = secondFlags.secondFontSize.value
            val secondMessage = secondFlags.secondMessage.value
            val showSecondMessage = secondFlags.showSecondMessage.isEnabled
            val secondSpecialNumber = secondFlags.secondSpecialNumber.value

            runOnUiThread {
                secondCenterLabel.setTextColor(Color.parseColor(secondFontColor))
                secondCenterLabel.textSize = secondFontSize.toFloat()
                secondCenterLabel.text = if (showSecondMessage) secondMessage else "Second instance flag is disabled"
                secondCenterLabel.visibility = View.VISIBLE
            }
            Log.i(TAG, "Second Instance Values - Color: $secondFontColor, Size: $secondFontSize, Number: $secondSpecialNumber, Message: $secondMessage, Show: $showSecondMessage")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating second instance UI", e)
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



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidfmexampleTheme {
        Greeting("Android")
    }
}