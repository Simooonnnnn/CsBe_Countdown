package com.example.csbecountdown

import android.app.Activity
import android.os.Build
import android.util.Log
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat

/**
 * Helper class for implementing predictive back gesture on Android 15+
 */
class PredictiveBackHelper {
    companion object {
        private const val TAG = "PredictiveBackHelper"

        /**
         * Register a back handler with the system (Android 15+)
         */
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        fun registerBackHandler(activity: Activity, onBackPressed: () -> Unit): OnBackInvokedCallback? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Log.d(TAG, "Registering predictive back handler")

                // Create callback
                val callback = object : OnBackAnimationCallback { //Removed () here
                    override fun onBackStarted(backEvent: BackEvent) {
                        Log.d(TAG, "Back gesture started with touchX: ${backEvent.touchX}")
                    }

                    override fun onBackProgressed(backEvent: BackEvent) {
                        Log.d(TAG, "Back gesture progressed: ${backEvent.progress}")
                    }

                    override fun onBackCancelled() {
                        Log.d(TAG, "Back gesture cancelled")
                    }

                    override fun onBackInvoked() {
                        Log.d(TAG, "Back gesture completed, invoking callback")
                        onBackPressed()
                    }
                }

                // Register callback
                activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    callback
                )

                return callback
            }
            return null
        }

        /**
         * Unregister a previously registered back handler
         */
        fun unregisterBackHandler(activity: Activity, callback: OnBackInvokedCallback) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
                Log.d(TAG, "Unregistered predictive back handler")
            }
        }

        /**
         * Enable edge-to-edge content display
         */
        fun enableEdgeToEdge(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                Log.d(TAG, "Edge-to-edge enabled")
            }
        }
    }
}