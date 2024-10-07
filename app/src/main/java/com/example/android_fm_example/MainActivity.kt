package com.example.android_fm_example

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import io.rollout.android.Rox
import io.rollout.android.client.RoxOptions
import io.rollout.client.FetcherStatus
import io.rollout.client.ConfigurationFetchedHandler
import io.rollout.client.FetcherResults

class MainActivity : ComponentActivity() {

    private lateinit var flags: Flags
    private lateinit var centerLabel: TextView
    private lateinit var headerLabel: TextView
    private lateinit var bottomLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        headerLabel = findViewById(R.id.headerText)
        bottomLabel = findViewById(R.id.bottomText)
        headerLabel.text = "CloudBees feature management Android sample application"
        bottomLabel.text = "Sign in to the CloudBees platform to modify flag values and see the changes reflacted automatically in this application."
        centerLabel = findViewById(R.id.centerLabel)
        // Initialize Flag container class that we created earlier
        flags = Flags()

        // Register the flags container with Rollout
        Rox.register(flags)

        // Building options
        val options = RoxOptions.Builder()
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

        // Setup the Rollout key with options
        Rox.setup(application, options)

    }

    private fun checkFlagsValue() {
        // Boolean flag example
        if (flags.showMessage.isEnabled) {
            // TODO: Put your code here that needs to be gated
        }

        // String flag example
        val fontColor = flags.fontColor.value
        Log.i("MainActivity", "Title color is $fontColor")

        // Integer flag example
        val fontSize = flags.fontSize.value
        Log.i("MainActivity", "Title size is $fontSize")

        // Double flag example
        val specialNumber = flags.specialNumber.value
        Log.i("MainActivity", "Special number is $specialNumber")

        centerLabel.text = flags.message.value
        centerLabel.setTextColor(getColorFromFlag(fontColor))
        centerLabel.textSize = fontSize.toFloat()

        if (flags.showMessage.isEnabled) {
            centerLabel.visibility = View.VISIBLE // Show the label
        } else {
            centerLabel.visibility = View.GONE // Hide the label
        }


    }

    private fun getColorFromFlag(colorName: String): Int {
        return when (colorName) {
            "White" -> Color.WHITE
            "Blue" -> Color.BLUE
            "Green" -> Color.GREEN
            "Yellow" -> Color.YELLOW
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