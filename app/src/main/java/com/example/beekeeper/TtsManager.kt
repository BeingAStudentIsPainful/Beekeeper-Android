package com.example.beekeeper

import android.content.Context
import android.os.Bundle
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
            Log.e("TtsManager", "Exception during TextToSpeech constructor", e)
            // If constructor fails, invoke callback immediately as TTS won't work
            onDoneCallback?.invoke()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var languageSet = false
            // Try to set default language
            val defaultResult = tts?.setLanguage(Locale.getDefault())
            if (defaultResult != TextToSpeech.LANG_MISSING_DATA && defaultResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.i("TtsManager", "TTS Initialized successfully with default locale: ${Locale.getDefault()}.")
                languageSet = true
            } else {
                Log.w("TtsManager", "TTS default language (${Locale.getDefault()}) not supported or missing data. Trying US English.")
                // Fallback to US English
                val usResult = tts?.setLanguage(Locale.US)
                if (usResult != TextToSpeech.LANG_MISSING_DATA && usResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.i("TtsManager", "TTS Initialized successfully with US English.")
                    languageSet = true
                } else {
                    Log.e("TtsManager", "TTS US English also not supported or missing data.")
                }
            }

            if (languageSet) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("TtsManager", "TTS Started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("TtsManager", "TTS Done: $utteranceId")
                        onDoneCallback?.invoke()
                    }

                    // Keep for compatibility with older API levels if necessary
                    @Deprecated("Deprecated in API level 21")
                    override fun onError(utteranceId: String?) {
                        Log.e("TtsManager", "TTS Error (deprecated): $utteranceId")
                        onDoneCallback?.invoke()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.e("TtsManager", "TTS Error: $utteranceId, Code: $errorCode")
                        onDoneCallback?.invoke()
                    }
                })
            } else {
                Log.e("TtsManager", "TTS language could not be set. TTS will not function correctly.")
                // Not initialized, invoke callback to allow dependent components to clean up/finish
                onDoneCallback?.invoke()
            }

        } else {
            Log.e("TtsManager", "TTS Initialization failed with status: $status")
            // Not initialized, invoke callback
            onDoneCallback?.invoke()
        }
    }

    fun speak(text: String, utteranceId: String = "Speech_${System.currentTimeMillis()}") {
        if (isInitialized && tts != null) {
            Log.i("TtsManager", "Attempting to speak: '$text' with ID: $utteranceId")
            val params = Bundle() // For future use, e.g. volume, pitch
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            Log.w("TtsManager", "TTS not initialized or instance is null, cannot speak. Invoking onDone callback.")
            // If not initialized or TTS is null, the callback should be triggered to allow cleanup
            onDoneCallback?.invoke()
        }
    }

    fun shutdown() {
        Log.i("TtsManager", "Shutting down TTS.")
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
            Log.d("TtsManager", "TTS engine stopped and shutdown.")
        }
        isInitialized = false
        tts = null
    }
}