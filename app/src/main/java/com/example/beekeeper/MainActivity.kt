package com.example.beekeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val APP_ACTION_EXTRA = "app_action_feature_name"
        const val TAG = "MainActivityAppAction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val debugInfoBuilder = StringBuilder()
        debugInfoBuilder.append("Intent Debug Info:\n")
        
        // Log detailed intent information
        logIntentDetails(intent, debugInfoBuilder)
        
        // Display debug info in the TextView
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = debugInfoBuilder.toString()
    }
    
    private fun logIntentDetails(intent: Intent?, debugBuilder: StringBuilder) {
        if (intent == null) {
            Log.w(TAG, "Intent is NULL")
            debugBuilder.append("Intent is NULL\n")
            return
        }
        
        // Log basic intent properties
        val action = intent.action ?: "null"
        val categories = intent.categories?.joinToString() ?: "null"
        val data = intent.dataString ?: "null"
        val type = intent.type ?: "null"
        val flags = intent.flags
        
        Log.i(TAG, "Intent Action: $action")
        Log.i(TAG, "Intent Categories: $categories")
        Log.i(TAG, "Intent Data: $data")
        Log.i(TAG, "Intent Type: $type")
        Log.i(TAG, "Intent Flags: $flags")
        
        debugBuilder.append("Action: $action\n")
        debugBuilder.append("Categories: $categories\n")
        debugBuilder.append("Data: $data\n")
        debugBuilder.append("Type: $type\n")
        debugBuilder.append("Flags: $flags\n\n")
        
        // Log all extras
        val extras = intent.extras
        if (extras != null) {
            debugBuilder.append("Extras:\n")
            Log.i(TAG, "Intent has ${extras.keySet().size} extras")
            
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.i(TAG, "Extra: $key = $value")
                debugBuilder.append("$key = $value\n")
            }
        } else {
            Log.i(TAG, "Intent has NO extras")
            debugBuilder.append("NO extras\n")
        }
        
        // Specifically look for our App Action extra
        val appActionFeature = intent.getStringExtra(APP_ACTION_EXTRA)
        if (appActionFeature != null) {
            Log.i(TAG, "FOUND App Action Feature: $appActionFeature")
            debugBuilder.append("\nFOUND App Action Feature: $appActionFeature\n")
        } else {
            Log.i(TAG, "App Action Feature NOT FOUND")
            debugBuilder.append("\nApp Action Feature NOT FOUND\n")
        }
        
        // Also check for BII extras that might have different names
        val possibleBiiExtras = listOf(
            "feature",            // Direct BII parameter name
            "bii.feature",        // Sometimes prefixed
            "android.intent.extra.feature",  // System-style naming
            "actions.fulfillment.extra.feature"  // Actions-style naming
        )
        
        for (extraName in possibleBiiExtras) {
            val value = intent.getStringExtra(extraName)
            if (value != null) {
                Log.i(TAG, "Found alternative extra: $extraName = $value")
                debugBuilder.append("Alternative extra found: $extraName = $value\n")
            }
        }
    }
}