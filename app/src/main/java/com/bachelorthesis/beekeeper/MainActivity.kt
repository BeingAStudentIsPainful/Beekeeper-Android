package com.bachelorthesis.beekeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivityAppAction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Assuming you have activity_main.xml

        Log.i(TAG, "MainActivity onCreate. Intent action: ${intent?.action}")
        intent?.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                Log.i(TAG, "Extra: $key = ${bundle.get(key)}")
            }
        }

        val featureParam = intent?.getStringExtra("feature_to_open")
        val textView = findViewById<TextView>(R.id.textView) // Assuming you have a TextView with id 'textView'

        if (featureParam != null) {
            Log.i(TAG, "App Action Feature Received: $featureParam")
            textView.text = "App Action: Feature launched: $featureParam"
        } else {
            Log.i(TAG, "No specific App Action feature parameter found.")
            textView.text = "MainActivity"
        }
        handleIntent(intent) // Call your new handleIntent
    }

    // Add this to handle new intents if the activity is already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "MainActivity onNewIntent. Intent action: ${intent?.action}")
        setIntent(intent) // Important to update the activity's intent
        intent?.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                Log.i(TAG, "Extra in onNewIntent: $key = ${bundle.get(key)}")
            }
        }
        val featureParam = intent?.getStringExtra("feature_to_open")
        val textView = findViewById<TextView>(R.id.textView) // Assuming you have a TextView with id 'textView'

        if (featureParam != null) {
            Log.i(TAG, "App Action Feature Received in onNewIntent: $featureParam")
            textView.text = "App Action (New Intent): $featureParam"
        }
        handleIntent(getIntent()) // Call your new handleIntent
    }


    // You can add a simple handler in MainActivity for this test
    private fun handleIntent(intent: Intent?) {
        val featureName = intent?.getStringExtra("feature_to_open")
        if ("actions.intent.OPEN_APP_FEATURE" == intent?.action && featureName != null) {
            Log.i(TAG, "Handling OPEN_APP_FEATURE: $featureName")
            // You could navigate to a specific part of your app based on featureName
            // For now, just logging is fine.
        }
    }
}