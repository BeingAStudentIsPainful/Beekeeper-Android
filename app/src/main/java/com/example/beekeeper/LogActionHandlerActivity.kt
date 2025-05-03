package com.example.beekeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LogActionHandlerActivity : AppCompatActivity() {
    // Define constants for intent actions (matching actions.xml later)
    companion object {
        const val ACTION_CREATE_LOG = "com.example.beekeeper.actions.CREATE_LOG"
        const val ACTION_READ_LOG = "com.example.beekeeper.actions.READ_LOG"
        const val EXTRA_LOG_TEXT = "log_text" // Parameter name for log content
    }

    private var ttsManager: TtsManager? = null
    private val activityScope = CoroutineScope(Dispatchers.Main) // Coroutine scope tied to activity lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("LogActionHandler", "Activity created. Intent: ${intent?.action}, Extras: ${intent?.extras}")

        // No UI needed
        // setContentView(R.layout.activity_log_action_handler) // Remove this if generated

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("LogActionHandler", "New Intent received: ${intent.action}")
        // Handle intent if activity is reused (e.g., already running)
        setIntent(intent) // Update the activity's intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            Log.w("LogActionHandler", "Intent is null.")
            finish() // Nothing to do
            return
        }

        when (intent.action) {
            ACTION_CREATE_LOG -> {
                val logText = intent.getStringExtra(EXTRA_LOG_TEXT)
                if (!logText.isNullOrBlank()) {
                    Log.i("LogActionHandler", "Handling CREATE_LOG: '$logText'")
                    LogManager.saveLastLog(applicationContext, logText)
                    // Maybe provide brief voice feedback? Optional.
                    // For now, just finish quickly after saving.
                    finish()
                } else {
                    Log.w("LogActionHandler", "No log text provided for CREATE_LOG.")
                    // Optionally speak an error message?
                    finish()
                }
            }
            ACTION_READ_LOG -> {
                Log.i("LogActionHandler", "Handling READ_LOG")
                val lastLog = LogManager.getLastLog(applicationContext)
                if (!lastLog.isNullOrBlank()) {
                    speakLog(lastLog)
                    // DO NOT finish() here - wait for TTS to complete
                } else {
                    Log.i("LogActionHandler", "No log found to read.")
                    speakLog("You haven't saved any logs yet.")
                    // DO NOT finish() here - wait for TTS to complete
                }
            }
            // Handle potential built-in intents if needed (e.g., from CREATE_NOTE)
            "actions.intent.CREATE_NOTE" -> {
                // Standard parameter for CREATE_NOTE text is often 'text'
                val logText = intent.getStringExtra("text")
                if (!logText.isNullOrBlank()) {
                    Log.i("LogActionHandler", "Handling CREATE_NOTE (built-in): '$logText'")
                    LogManager.saveLastLog(applicationContext, logText)
                    finish()
                } else {
                    Log.w("LogActionHandler", "No text found in CREATE_NOTE intent.")
                    finish()
                }
            }
            "actions.intent.GET_NOTE" -> {
                // Standard GET_NOTE might not fit perfectly, map it to reading our last log
                Log.i("LogActionHandler", "Handling GET_NOTE (built-in)")
                val lastLog = LogManager.getLastLog(applicationContext)
                if (!lastLog.isNullOrBlank()) {
                    speakLog(lastLog)
                } else {
                    speakLog("You haven't saved any logs yet.")
                }
            }
            else -> {
                Log.w("LogActionHandler", "Unknown or unhandled intent action: ${intent.action}")
                finish() // Unknown action, close
            }
        }
    }

    private fun speakLog(textToSpeak: String) {
        if (ttsManager == null) {
            // Initialize TTS only when needed
            // Pass a callback lambda to finish the activity when TTS is done
            ttsManager = TtsManager(applicationContext) {
                Log.d("LogActionHandler", "TTS completion callback triggered.")
                // Ensure finish() is called on the main thread safely
                activityScope.launch {
                    if (!isFinishing && !isDestroyed) {
                        Log.i("LogActionHandler", "Finishing activity after TTS.")
                        finish()
                    }
                }
            }
        }
        // Short delay to ensure TTS initialization can potentially complete,
        // especially if called immediately in onCreate. Coroutine helps manage this.
        activityScope.launch {
            delay(200) // Small delay (adjust if needed)
            Log.d("LogActionHandler", "Attempting to speak after delay.")
            ttsManager?.speak(textToSpeak) ?: run {
                Log.e("LogActionHandler", "TTS Manager was null when trying to speak.")
                // If speak failed immediately (e.g., TTS init failed), finish now.
                if (!isFinishing && !isDestroyed) {
                    Log.i("LogActionHandler", "Finishing activity because TTS failed to initialize/speak.")
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        Log.i("LogActionHandler", "Activity destroying.")
        activityScope.cancel() // Cancel coroutines
        ttsManager?.shutdown() // Release TTS resources
        ttsManager = null
        super.onDestroy()
    }
}