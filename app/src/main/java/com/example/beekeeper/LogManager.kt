package com.example.beekeeper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object LogManager {
    private const val PREFS_NAME = "VoiceLogPrefs"
    private const val KEY_LAST_LOG = "last_log_entry"

    // Get SharedPreferences instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save the last log entry
    fun saveLastLog(context: Context, log: String) {
        getPrefs(context).edit() {
            putString(KEY_LAST_LOG, log)
        } // Use apply() for asynchronous saving
        android.util.Log.i("LogManager", "Log saved: $log") // Optional logging
    }

    // Retrieve the last log entry
    fun getLastLog(context: Context): String? {
        val log = getPrefs(context).getString(KEY_LAST_LOG, null)
        android.util.Log.i("LogManager", "Log retrieved: $log") // Optional logging
        return log
    }
}