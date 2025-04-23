package com.example.csbecountdown.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.core.content.ContextCompat

/**
 * Helper for Material You dynamic colors in widgets
 */
object DynamicColorHelper {
    // Light mode colors - minimalist style
    private val LIGHT_BACKGROUND = ComposeColor(0xFFF6F6F6)
    private val LIGHT_ON_BACKGROUND = ComposeColor(0xFF1A1C1E)
    private val LIGHT_PRIMARY = ComposeColor(0xFF006AF5)
    private val LIGHT_PRIMARY_CONTAINER = ComposeColor(0xFFD8E2FF)
    private val LIGHT_ON_PRIMARY_CONTAINER = ComposeColor(0xFF001A41)

    // Dark mode colors
    private val DARK_BACKGROUND = ComposeColor(0xFF121212)
    private val DARK_ON_BACKGROUND = ComposeColor(0xFFE2E2E6)
    private val DARK_PRIMARY = ComposeColor(0xFFADC6FF)
    private val DARK_PRIMARY_CONTAINER = ComposeColor(0xFF004395)
    private val DARK_ON_PRIMARY_CONTAINER = ComposeColor(0xFFD8E2FF)

    /**
     * Check if device is in dark theme
     */
    fun isDarkTheme(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Get background color
     */
    fun getSurface(context: Context): ComposeColor {
        val isDark = isDarkTheme(context)

        // For widgets, we'll try to fetch a system accent color if available
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                // Try to get dynamic color using resource name
                val resourceId = context.resources.getIdentifier(
                    if (isDark) "system_neutral1_900" else "system_neutral1_50",
                    "color", "android"
                )

                if (resourceId != 0) {
                    val dynamicColor = ContextCompat.getColor(context, resourceId)
                    ComposeColor(dynamicColor)
                } else {
                    // Fallback to predefined colors
                    if (isDark) DARK_BACKGROUND else LIGHT_BACKGROUND
                }
            } catch (e: Exception) {
                // Fallback to predefined colors
                if (isDark) DARK_BACKGROUND else LIGHT_BACKGROUND
            }
        } else {
            // For older Android versions
            if (isDark) DARK_BACKGROUND else LIGHT_BACKGROUND
        }
    }

    /**
     * Get text color for main content
     */
    fun getOnSurface(context: Context): ComposeColor {
        return if (isDarkTheme(context)) DARK_ON_BACKGROUND else LIGHT_ON_BACKGROUND
    }

    /**
     * Get container color for elements
     */
    fun getPrimaryContainer(context: Context): ComposeColor {
        val isDark = isDarkTheme(context)

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                // Try to get dynamic color
                val resourceId = context.resources.getIdentifier(
                    if (isDark) "system_accent1_700" else "system_accent1_100",
                    "color", "android"
                )

                if (resourceId != 0) {
                    val dynamicColor = ContextCompat.getColor(context, resourceId)
                    ComposeColor(dynamicColor)
                } else {
                    // Fallback
                    if (isDark) DARK_PRIMARY_CONTAINER else LIGHT_PRIMARY_CONTAINER
                }
            } catch (e: Exception) {
                // Fallback
                if (isDark) DARK_PRIMARY_CONTAINER else LIGHT_PRIMARY_CONTAINER
            }
        } else {
            // For older Android versions
            if (isDark) DARK_PRIMARY_CONTAINER else LIGHT_PRIMARY_CONTAINER
        }
    }

    /**
     * Get text color for container elements
     */
    fun getOnPrimaryContainer(context: Context): ComposeColor {
        val isDark = isDarkTheme(context)

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                // Try to get dynamic color
                val resourceId = context.resources.getIdentifier(
                    if (isDark) "system_accent1_100" else "system_accent1_900",
                    "color", "android"
                )

                if (resourceId != 0) {
                    val dynamicColor = ContextCompat.getColor(context, resourceId)
                    ComposeColor(dynamicColor)
                } else {
                    // Fallback
                    if (isDark) DARK_ON_PRIMARY_CONTAINER else LIGHT_ON_PRIMARY_CONTAINER
                }
            } catch (e: Exception) {
                // Fallback
                if (isDark) DARK_ON_PRIMARY_CONTAINER else LIGHT_ON_PRIMARY_CONTAINER
            }
        } else {
            // For older Android versions
            if (isDark) DARK_ON_PRIMARY_CONTAINER else LIGHT_ON_PRIMARY_CONTAINER
        }
    }
}