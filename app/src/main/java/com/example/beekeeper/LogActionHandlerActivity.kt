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
        // Custom actions (not currently primary for App Actions BIIs but good for direct calls)
        const val ACTION_CREATE_LOG = "com.example.beekeeper.actions.CREATE_LOG"
        const val ACTION_READ_LOG = "com.example.beekeeper.actions.READ_LOG"

        // Key for the log text, used by App Actions (CREATE_NOTE BII) as mapped in shortcuts.xml
        // and potentially for custom actions.
        const val EXTRA_LOG_TEXT = "log_text"
    }

    private var ttsManager: TtsManager? = null
    private val activityScope = CoroutineScope(Dispatchers.Main) // Coroutine scope tied to activity lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("LogActionHandler", "Activity created. Intent: ${intent?.action}, Extras: ${intent?.extras?.keySet()?.joinToString()}")

        // No UI needed for this handler activity
        // setContentView(R.layout.activity_log_action_handler) // Ensure no layout is set

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i("LogActionHandler", "New Intent received: ${intent.action}, Extras: ${intent.extras?.keySet()?.joinToString()}")
        // Handle intent if activity is reused (e.g., already running)
        setIntent(intent) // Update the activity's intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            Log.w("LogActionHandler", "Intent is null. Finishing.")
            finishAndRemoveTask() // Use finishAndRemoveTask for handler activities
            return
        }

        Log.d("LogActionHandler", "Handling intent action: ${intent.action}")

        when (intent.action) {
            // Handling Built-In Intent: actions.intent.CREATE_NOTE
            "actions.intent.CREATE_NOTE" -> {
                // The 'note.text' BII parameter is mapped to EXTRA_LOG_TEXT in shortcuts.xml
                val logText = intent.getStringExtra(EXTRA_LOG_TEXT)
                if (!logText.isNullOrBlank()) {
                    Log.i("LogActionHandler", "Handling BII CREATE_NOTE: '$logText'")
                    LogManager.saveLastLog(applicationContext, logText)
                    // Optionally, provide brief voice feedback for success, e.g., "Log saved."
                    // For now, finish quickly.
                    finishAndRemoveTask()
                } else {
                    Log.w("LogActionHandler", "No text found in CREATE_NOTE intent (expected extra: '$EXTRA_LOG_TEXT'). Extras: ${intent.extras?.keySet()?.joinToString()}")
                    // Optionally speak an error message or provide feedback
                    finishAndRemoveTask()
                }
            }
            // Handling Built-In Intent: actions.intent.GET_NOTE
            "actions.intent.GET_NOTE" -> {
                Log.i("LogActionHandler", "Handling BII GET_NOTE")
                val lastLog = LogManager.getLastLog(applicationContext)
                val messageToSpeak = if (!lastLog.isNullOrBlank()) {
                    lastLog
                } else {
                    "You haven't saved any logs yet."
                }
                Log.d("LogActionHandler", "Message to speak for GET_NOTE: '$messageToSpeak'")
                speakLog(messageToSpeak)
                // DO NOT finish() here - wait for TTS to complete via its callback
            }
            // Handling custom action (if you were to use it directly)
            ACTION_CREATE_LOG -> {
                val logText = intent.getStringExtra(EXTRA_LOG_TEXT)
                if (!logText.isNullOrBlank()) {
                    Log.i("LogActionHandler", "Handling custom ACTION_CREATE_LOG: '$logText'")
                    LogManager.saveLastLog(applicationContext, logText)
                    finishAndRemoveTask()
                } else {
                    Log.w("LogActionHandler", "No log text provided for custom ACTION_CREATE_LOG.")
                    finishAndRemoveTask()
                }
            }
            // Handling custom action (if you were to use it directly)
            ACTION_READ_LOG -> {
                Log.i("LogActionHandler", "Handling custom ACTION_READ_LOG")
                val lastLog = LogManager.getLastLog(applicationContext)
                 val messageToSpeak = if (!lastLog.isNullOrBlank()) {
                    lastLog
                } else {
                    "You haven't saved any logs yet for custom action."
                }
                speakLog(messageToSpeak)
                // DO NOT finish() here - wait for TTS to complete
            }
            else -> {
                Log.w("LogActionHandler", "Unknown or unhandled intent action: ${intent.action}")
                finishAndRemoveTask() // Unknown action, close
            }
        }
    }

    private fun speakLog(textToSpeak: String) {
        if (ttsManager == null) {
            Log.d("LogActionHandler", "Initializing TtsManager for speaking.")
            ttsManager = TtsManager(applicationContext) {
                Log.d("LogActionHandler", "TTS completion callback triggered.")
                activityScope.launch { // Ensure running on Main dispatcher
                    if (!isFinishing && !isDestroyed) {
                        Log.i("LogActionHandler", "Finishing activity after TTS.")
                        finishAndRemoveTask()
                    }
                }
            }
        }

        // The small delay can sometimes help if TTS initialization is slightly slow
        // and speak is called very rapidly after creation. TtsManager itself
        // has a guard, but this provides an extra buffer.
        // A more robust solution might involve a readiness signal from TtsManager.
        activityScope.launch {
            delay(200) // Adjust or test removal if TTS initialization proves consistently fast
            Log.d("LogActionHandler", "Attempting to speak: '$textToSpeak'")
            ttsManager?.speak(textToSpeak) ?: run {
                Log.e("LogActionHandler", "TTS Manager was null or speak command failed when trying to speak.")
                // If speak failed immediately (e.g., TTS init failed or ttsManager is null), finish now.
                if (!isFinishing && !isDestroyed) {
                    Log.i("LogActionHandler", "Finishing activity because TTS could not be initiated for speakLog.")
                    finishAndRemoveTask()
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