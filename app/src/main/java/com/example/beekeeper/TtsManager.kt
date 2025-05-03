package com.example.beekeeper

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(context: Context, private val onDoneCallback: (() -> Unit)? = null) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val appContext = context.applicationContext // Use application context

    init {
        try {
            tts = TextToSpeech(appContext, this)
            Log.i("TtsManager", "TTS Engine initialization requested.")
        } catch (e: Exception) {
            Log.e("TtsManager", "Error initializing TTS", e)
        }
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language (optional, defaults to device locale)
            val result = tts?.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "TTS language not supported or missing data.")
                // Optionally try Locale.US or other fallback
                // tts?.language = Locale.US
            } else {
                isInitialized = true
                Log.i("TtsManager", "TTS Initialized successfully.")
            }

            // Set a listener to know when speaking is done
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("TtsManager", "TTS Started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("TtsManager", "TTS Done: $utteranceId")
                    // IMPORTANT: Execute callback on the main thread if it interacts with UI/Activity lifecycle
                    // For now, direct call is okay as we don't have UI interaction in the callback yet.
                    onDoneCallback?.invoke()
                }

                @Deprecated("Deprecated potentially in API levels")
                override fun onError(utteranceId: String?) {
                    Log.e("TtsManager", "TTS Error: $utteranceId")
                    onDoneCallback?.invoke() // Still call callback on error to allow cleanup/finish
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e("TtsManager", "TTS Error: $utteranceId, Code: $errorCode")
                    onDoneCallback?.invoke() // Still call callback on error
                }
            })

        } else {
            Log.e("TtsManager", "TTS Initialization failed with status: $status")
        }
    }

    fun speak(text: String, utteranceId: String = "UniqueID") {
        if (isInitialized && tts != null) {
            Log.i("TtsManager", "Attempting to speak: '$text'")
            // Use QUEUE_FLUSH to interrupt previous speech, QUEUE_ADD to append.
            // Pass utteranceId to track completion in the listener.
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            Log.w("TtsManager", "TTS not initialized, cannot speak.")
            // Fallback or error notification? For now, just log.
            // If not initialized, the callback should also be triggered to allow cleanup
            onDoneCallback?.invoke()
        }
    }

    // Call this when the component using TTS is destroyed (e.g., Activity's onDestroy)
    fun shutdown() {
        Log.i("TtsManager", "Shutting down TTS.")
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
        tts = null
    }

}