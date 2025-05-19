package com.bachelorthesis.beekeeper // Your package name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ActionHandlerActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ActionHandlerActivity"
        const val EXTRA_LOG_TEXT = "log_text_param" // Must match the key in shortcuts.xml
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Activity created. Intent: ${intent?.action}")

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) { // Intent here is non-nullable
        super.onNewIntent(intent)
        Log.i(TAG, "New Intent received: ${intent.action}, Extras: ${intent.extras?.keySet()?.joinToString()}")
        setIntent(intent) // Update the activity's intent
        handleIntent(intent) // Handle the new intent
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            Log.w(TAG, "Intent is null. Finishing.")
            finishAndRemoveTask()
            return
        }

        Log.d(TAG, "Handling intent action: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras?.keySet()?.joinToString()}")

        when (intent.action) {
            "actions.intent.CREATE_NOTE" -> {
                val logText = intent.getStringExtra(EXTRA_LOG_TEXT)
                if (!logText.isNullOrBlank()) {
                    Log.i(TAG, "Received log text: '$logText'")
                    LogManager.saveLastLog(applicationContext, logText) // Use LogManager
                    android.widget.Toast.makeText(this, "Log saved: $logText", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    Log.w(TAG, "No text found for CREATE_NOTE. Param key expected: $EXTRA_LOG_TEXT")
                    android.widget.Toast.makeText(this, "No log text received", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Log.w(TAG, "Unknown or unhandled intent action: ${intent.action}")
            }
        }
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Activity destroyed.")
    }
}