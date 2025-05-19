package com.bachelorthesis.beekeeper // Your package name

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.util.Log

object LogManager {
    private const val PREFS_NAME = "BeekeeperLogPrefs" // Changed name slightly for clarity
    private const val KEY_LAST_LOG_PREFIX = "last_log_hive_" // We'll make this hive-specific later
    private const val KEY_GENERIC_LAST_LOG = "last_log_entry" // For simple, non-hive specific log

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Save the last log entry (simple version)
    fun saveLastLog(context: Context, log: String) {
        getPrefs(context).edit {
            putString(KEY_GENERIC_LAST_LOG, log)
            apply() // Use apply for asynchronous saving
        }
        Log.i("LogManager", "Log saved (generic): $log")
    }

    // Retrieve the last log entry (simple version)
    fun getLastLog(context: Context): String? {
        val log = getPrefs(context).getString(KEY_GENERIC_LAST_LOG, null)
        Log.i("LogManager", "Log retrieved (generic): $log")
        return log
    }

    // --- Methods for hive-specific logs (we'll use these later) ---
    fun saveLogForHive(context: Context, hiveId: String, log: String) {
        getPrefs(context).edit {
            putString("$KEY_LAST_LOG_PREFIX$hiveId", log)
            apply()
        }
        Log.i("LogManager", "Log saved for Hive $hiveId: $log")
    }

    fun getLastLogForHive(context: Context, hiveId: String): String? {
        val log = getPrefs(context).getString("$KEY_LAST_LOG_PREFIX$hiveId", null)
        Log.i("LogManager", "Log retrieved for Hive $hiveId: $log")
        return log
    }
}