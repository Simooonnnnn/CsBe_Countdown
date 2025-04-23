package com.example.csbecountdown.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.csbecountdown.notifications.CountdownNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationManager = CountdownNotificationManager(application)
    private val preferenceManager = NotificationPreferenceManager(application)

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            // Load saved preferences
            _notificationsEnabled.value = preferenceManager.getNotificationsEnabled()
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            preferenceManager.setNotificationsEnabled(enabled)

            if (enabled) {
                // Schedule notifications if newly enabled
                notificationManager.scheduleNotifications()
            } else {
                // Cancel all scheduled notifications if disabled
                notificationManager.cancelAllNotifications()
            }
        }
    }
}

/**
 * Helper class to manage notification preferences
 */
class NotificationPreferenceManager(private val application: Application) {

    companion object {
        private const val PREFS_NAME = "countdown_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, 0)

    fun getNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
}