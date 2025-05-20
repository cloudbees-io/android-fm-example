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
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_fm_example.ui.theme.AndroidfmexampleTheme
import io.rollout.android.Rox as RoxNonXaaf
import io.rollout.android.client.RoxOptions
import io.rollout.client.FetcherStatus
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults
import io.rollout.sdk.xaaf.android.Rox as RoxXaaf
import io.rollout.sdk.xaaf.android.client.RoxOptions as RoxXaafOptions
import io.rollout.sdk.xaaf.client.FetcherStatus as XaafFetcherStatus
import io.rollout.sdk.xaaf.client.ConfigurationFetchedHandler as XaafConfigurationFetchedHandler
import io.rollout.sdk.xaaf.client.FetcherResults as XaafFetcherResults

class MainActivity : ComponentActivity() {

    private lateinit var flags: Flags
    private lateinit var xaafFlags: XaafFlags
    private lateinit var centerLabel: TextView
    private lateinit var headerLabel: TextView
    private lateinit var bottomLabel: TextView
//    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        headerLabel = findViewById(R.id.headerText)
        bottomLabel = findViewById(R.id.bottomText)
        headerLabel.text = "CloudBees feature management Android sample application"
        bottomLabel.text = "Sign in to the CloudBees platform to modify flag values and see the changes reflacted automatically in this application."
        centerLabel = findViewById(R.id.centerLabel)
        // Initialize Flag container classes
        flags = Flags()
        xaafFlags = XaafFlags()

        // Register the flags containers with CloudBees Feature Management
        RoxNonXaaf.register(flags)
        RoxXaaf.register(xaafFlags)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.i("MainActivity", "Network Available")
                // Setup non-Xaaf SDK
                val options = roxOptions()
                RoxNonXaaf.setup(application, "<Enter Non-Xaaf SDK Key>", options)

                // Setup Xaaf SDK
                val xaafOptions = roxXaafOptions()
                RoxXaaf.setup(application, "<Enter Xaaf SDK Key>", xaafOptions)
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.i("MainActivity", "Network lost")
                // Handle network lost if needed
            }
        }

    }

    private fun roxOptions(): RoxOptions? {
        val options = RoxOptions.Builder()
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : ConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: FetcherResults?) {
                    fetcherResults?.let {
                        val status = it.fetcherStatus
                        // Configuration loaded from network, flags value updated
                        if (status == FetcherStatus.AppliedFromNetwork) {
                            runOnUiThread {
                                checkFlagsValue() // Update the UI with the new flag value
                            }
                        }
                    }
                }
            })
            .build()
        return options
    }

    private fun roxXaafOptions(): RoxXaafOptions? {
        val options = RoxXaafOptions.Builder()
            .withDisableSignatureVerification(true)
            .withConfigurationFetchedHandler(object : XaafConfigurationFetchedHandler {
                override fun onConfigurationFetched(fetcherResults: XaafFetcherResults?) {
                    fetcherResults?.let {
                        val status = it.fetcherStatus
                        // Configuration loaded from network, flags value updated
                        if (status == XaafFetcherStatus.AppliedFromNetwork) {
                            runOnUiThread {
                                checkXaafFlagsValue() // Update the UI with the new flag value
                            }
                        }
                    }
                }
            })
            .build()
        return options
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkFlagsValue() {
        // Boolean flag example
        if (flags.showMessage.isEnabled) {
            // TODO: Put your code here that needs to be gated
        }

        // String flag example
        val fontColor = flags.fontColor.value
        Log.i("MainActivity", "Non-Xaaf Title color is $fontColor")

        // Integer flag example
        val fontSize = flags.fontSize.value
        Log.i("MainActivity", "Non-Xaaf Title size is $fontSize")

        // Double flag example
        val specialNumber = flags.specialNumber.value
        Log.i("MainActivity", "Non-Xaaf Special number is $specialNumber")

        headerLabel.text = flags.message.value
        headerLabel.setTextColor(getColorFromFlag(fontColor))
        headerLabel.textSize = fontSize.toFloat()

        if (flags.showMessage.isEnabled) {
            headerLabel.visibility = View.VISIBLE
        } else {
            headerLabel.visibility = View.GONE
        }
    }

    private fun checkXaafFlagsValue() {
        // Boolean flag example
        if (xaafFlags.showMessage.isEnabled) {
            // TODO: Put your code here that needs to be gated
        }

        // String flag example
        val fontColor = xaafFlags.fontColor.value
        Log.i("MainActivity", "Xaaf Title color is $fontColor")

        // Integer flag example
        val fontSize = xaafFlags.fontSize.value
        Log.i("MainActivity", "Xaaf Title size is $fontSize")

        // Double flag example
        val specialNumber = xaafFlags.specialNumber.value
        Log.i("MainActivity", "Xaaf Special number is $specialNumber")

        centerLabel.text = xaafFlags.message.value
        centerLabel.setTextColor(getColorFromFlag(fontColor))
        centerLabel.textSize = fontSize.toFloat()

        if (xaafFlags.showMessage.isEnabled) {
            centerLabel.visibility = View.VISIBLE
        } else {
            centerLabel.visibility = View.GONE
        }
    }

    private fun getColorFromFlag(colorName: String): Int {
        return when (colorName) {
            "White" -> Color.WHITE
            "Blue" -> Color.BLUE
            "Green" -> Color.GREEN
            "Yellow" -> Color.YELLOW
            "Red" -> Color.RED
            else -> Color.BLACK // Default color
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