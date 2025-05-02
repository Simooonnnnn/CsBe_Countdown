package com.example.csbecountdown.notifications

import android.app.Application
import android.content.Context
import android.util.Log

/**
 * Manages the history of sent notifications to prevent duplicates
 */
class NotificationHistoryManager(private val application: Application) {

    companion object {
        private const val PREFS_NAME = "notification_history"
        private const val KEY_PREFIX = "milestone_shown_"
        private const val TAG = "NotificationHistory"
    }

    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if a notification has already been shown for the specified day
     * @param daysLeft The milestone day to check
     * @return True if notification has been shown, false otherwise
     */
    fun isNotificationShown(daysLeft: Int): Boolean {
        val key = buildKey(daysLeft)
        val isShown = sharedPreferences.getBoolean(key, false)
        Log.d(TAG, "Checking if notification for $daysLeft days is shown: $isShown")
        return isShown
    }

    /**
     * Mark a notification as shown for the specified day
     * @param daysLeft The milestone day to mark as shown
     */
    fun markNotificationShown(daysLeft: Int) {
        val key = buildKey(daysLeft)
        sharedPreferences.edit().putBoolean(key, true).apply()
        Log.d(TAG, "Marked notification for $daysLeft days as shown")
    }

    /**
     * Reset all notification history (for testing or when target date changes)
     */
    fun resetAllHistory() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Reset all notification history")
    }

    /**
     * Build the preference key for a specific days milestone
     */
    private fun buildKey(daysLeft: Int): String {
        return KEY_PREFIX + daysLeft
    }
}